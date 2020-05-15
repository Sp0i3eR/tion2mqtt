package expert.serebro.tion.topics;

import expert.serebro.tion.command.Command;
import expert.serebro.tion.command.QuerySettingsCommand;
import expert.serebro.tion.command.SendStateToTopicCommand;
import expert.serebro.tion.device.BluetoothSession;
import expert.serebro.tion.device.BreezerDevice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import tinyb.BluetoothDevice;
import tinyb.BluetoothManager;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class DevicePairListener implements IMqttMessageListener {
    private final BreezerDevice device;
    private final IMqttClient client;

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        log.debug(String.format("Message arrived on %s topic with payload %s", topic, message));
        BluetoothManager manager = BluetoothManager.getBluetoothManager();
        if (manager.getDiscovering()) {
            log.debug("Discovery already started");
        } else {
            log.debug(String.format("Starting discovery for %s", device.getAddress()));
            manager.startDiscovery();
        }
        for (int i = 0; i < 10; i++) {
            BluetoothDevice bluetoothDevice = manager.getDevices().stream()
                    .filter(b -> b.getAddress().equalsIgnoreCase(device.getAddress())).findAny().orElse(null);
            if (bluetoothDevice != null) {
                device.setDevice(bluetoothDevice);
                device.setAvailable(true);
                break;
            }
            Thread.sleep(5000);
        }
        manager.stopDiscovery();
        if (device.isAvailable()) {
            log.debug(String.format("Device %s is available, creating bluetooth session", device.getAddress()));
            new BluetoothSession(device, createQueryCommandChain(device, client)).run();
        }
    }

    private static Collection<Command> createQueryCommandChain(BreezerDevice device, IMqttClient client) {
        var querySettingsCommand = new QuerySettingsCommand(null);
        var sendStateToTopicCommand = new SendStateToTopicCommand(querySettingsCommand::getResponse, client, TopicUtils.formatTopic(device, TopicUtils.STATE_TOPIC));
        return List.of(querySettingsCommand, sendStateToTopicCommand);
    }

}
