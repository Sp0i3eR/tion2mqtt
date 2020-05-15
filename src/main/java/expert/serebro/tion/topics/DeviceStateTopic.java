package expert.serebro.tion.topics;

import expert.serebro.tion.command.Command;
import expert.serebro.tion.command.QuerySettingsCommand;
import expert.serebro.tion.command.SendStateToTopicCommand;
import expert.serebro.tion.device.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class DeviceStateTopic implements Runnable {
    private final Collection<BreezerDevice> devices;
    private final IMqttClient client;

    @Override
    public void run() {
        if (devices.isEmpty()) {
            return;
        }
        devices.parallelStream()
                .filter(BreezerDevice::isAvailable)
                .map(device -> new BluetoothSession(device, createQueryCommandChain(device, client)))
                .forEach(BluetoothSession::run);
    }

    private static Collection<Command> createQueryCommandChain(BreezerDevice device, IMqttClient client) {
        var querySettingsCommand = new QuerySettingsCommand(null);
        var sendStateToTopicCommand = new SendStateToTopicCommand(querySettingsCommand::getResponse, client, TopicUtils.formatTopic(device, TopicUtils.STATE_TOPIC));
        return List.of(querySettingsCommand, sendStateToTopicCommand);
    }

}
