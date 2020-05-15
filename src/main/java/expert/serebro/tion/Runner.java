package expert.serebro.tion;

import expert.serebro.tion.device.BreezerDevice;
import expert.serebro.tion.topics.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class Runner {

    public static void main(String[] args) throws InterruptedException, MqttException {
        if (args.length < 2) {
            log.error("Usage: <mqtt-server-url> <mac> [mac] [mac]");
        } else {
            String url = args[0];
            List<BreezerDevice> devices = Arrays.stream(args).skip(1).map(BreezerDevice::new).collect(Collectors.toList());
            ScheduledExecutorService scheduler =
                    Executors.newScheduledThreadPool(4);
            try (
                    IMqttClient publisher = new MqttClient(url, UUID.randomUUID().toString());
                    IMqttClient subscriber = new MqttClient(url, UUID.randomUUID().toString())
            ) {
                Runtime.getRuntime().addShutdownHook(new Thread(new BluetoothShutdownHook(devices, List.of(publisher, subscriber))));
                MqttConnectOptions options = new MqttConnectOptions();
                options.setAutomaticReconnect(true);
                options.setCleanSession(true);
                options.setConnectionTimeout(10);
                publisher.connect(options);
                subscriber.connect(options);
                scheduler.scheduleAtFixedRate(new DeviceAvailabilityTopic(devices, publisher), 1, 15, TimeUnit.SECONDS);
                scheduler.scheduleAtFixedRate(new DeviceStateTopic(devices, publisher), 1, 5, TimeUnit.MINUTES);

                for (BreezerDevice device : devices) {
                    subscriber.subscribe(
                            TopicUtils.formatTopic(device, TopicUtils.SPEED_TOPIC),
                            new DeviceSpeedListener(device, publisher)
                    );
                    subscriber.subscribe(
                            TopicUtils.formatTopic(device, TopicUtils.PAIR_TOPIC),
                            new DevicePairListener(device, publisher)
                    );
                }
                log.info(String.format("Started gateway between devices %s and %s",
                        url,
                        devices.stream().map(BreezerDevice::getAddress).collect(Collectors.joining())));
                Thread.currentThread().join();
            }
        }
    }


}
