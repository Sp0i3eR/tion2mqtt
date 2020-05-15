package expert.serebro.tion.topics;

import expert.serebro.tion.device.BreezerDevice;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import tinyb.BluetoothDevice;
import tinyb.BluetoothManager;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class DeviceAvailabilityTopic implements Runnable {
    private final Collection<BreezerDevice> devices;
    private final IMqttClient client;

    @Override
    public void run() {
        if (devices.isEmpty()) {
            return;
        }
        BluetoothManager manager = BluetoothManager.getBluetoothManager();
        Map<String, BreezerDevice> devicesOfInterest = devices.stream()
                .collect(Collectors.toConcurrentMap(
                        d -> d.getAddress().toUpperCase(),
                        Function.identity()
                ));

        Map<String, BluetoothDevice> filteredAvailableDevices = manager.getDevices().stream()
                .filter(device -> devicesOfInterest.containsKey(device.getAddress())).collect(Collectors.toConcurrentMap(
                        BluetoothDevice::getAddress,
                        Function.identity()
                ));

        devicesOfInterest.forEach((addr, breezerDevice) -> {
                    if ((breezerDevice.isAvailable() && !filteredAvailableDevices.containsKey(addr)) ||
                            !breezerDevice.isAvailable() && filteredAvailableDevices.containsKey(addr)) {
                        breezerDevice.setAvailable(filteredAvailableDevices.containsKey(addr));
                        breezerDevice.setDevice(filteredAvailableDevices.get(addr));
                        sendMessage(breezerDevice);
                    }
                }
        );

    }

    @SneakyThrows
    private void sendMessage(BreezerDevice breezerDevice) {
        if (client.isConnected()) {
            MqttMessage msg = new MqttMessage((breezerDevice.isAvailable() ? TopicUtils.AVAILABILITY_ONLINE: TopicUtils.AVAILABILITY_OFFLINE).getBytes());
            msg.setQos(0);
            msg.setRetained(true);
            log.info(String.format("Device %s became %s", breezerDevice.getAddress(), msg));
            client.publish(TopicUtils.formatTopic(breezerDevice, TopicUtils.AVAILABILITY_TOPIC), msg);
        }
    }
}
