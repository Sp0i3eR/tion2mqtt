package expert.serebro.tion.topics;

import expert.serebro.tion.command.Command;
import expert.serebro.tion.command.QuerySettingsCommand;
import expert.serebro.tion.command.SendStateToTopicCommand;
import expert.serebro.tion.command.UpdateSettingsCommand;
import expert.serebro.tion.device.BluetoothSession;
import expert.serebro.tion.device.BreezerDevice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class DeviceSpeedListener implements IMqttMessageListener {
    private final BreezerDevice device;
    private final IMqttClient client;

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        log.debug(String.format("Message arrived on %s topic with payload %s", topic, message));
        if (device.isAvailable()) {
            log.debug(String.format("Device %s is available, creating bluetooth session", device.getAddress()));
            new BluetoothSession(device,
                    createSpeedCommandChain(device, client, Integer.parseInt(new String(message.getPayload())))
            ).run();
        }
    }

    private static Collection<Command> createSpeedCommandChain(BreezerDevice device, IMqttClient client, Integer speed) {
        var querySettingsCommand = new QuerySettingsCommand(speed);
        var updateSettingsCommand = new UpdateSettingsCommand(querySettingsCommand::getResponse);
        var sendStateToTopicCommand = new SendStateToTopicCommand(updateSettingsCommand::getResponse, client, TopicUtils.formatTopic(device, TopicUtils.STATE_TOPIC));
        return List.of(querySettingsCommand, updateSettingsCommand, sendStateToTopicCommand);
    }

}
