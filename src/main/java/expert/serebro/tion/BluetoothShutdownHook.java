package expert.serebro.tion;

import expert.serebro.tion.device.BreezerDevice;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import tinyb.BluetoothDevice;
import tinyb.BluetoothManager;

import java.util.Collection;

@RequiredArgsConstructor
@Slf4j
public class BluetoothShutdownHook implements Runnable {
    private final Collection<BreezerDevice> devices;
    private final Collection<IMqttClient> clients;

    @Override
    @SneakyThrows
    public void run() {
        for (BreezerDevice device : devices) {
            BluetoothDevice deviceDevice = device.getDevice();
            if (deviceDevice != null && deviceDevice.getConnected()) {
                deviceDevice.disconnect();
                log.info(String.format("Disconnecting device on shutdown %s", device.getAddress()));
            }
        }
        if (BluetoothManager.getBluetoothManager().getDiscovering()) {
            BluetoothManager.getBluetoothManager().stopDiscovery();
            log.info("Stopping bluetooth discovery on shutdown");
        }
        for (IMqttClient client : clients) {
            if (client.isConnected()) {
                client.disconnectForcibly();
                log.info(String.format("Disconnecting mqtt client %s on shutdown", client.getClientId()));
            }
        }

    }
}
