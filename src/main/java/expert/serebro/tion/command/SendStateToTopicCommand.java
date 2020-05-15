package expert.serebro.tion.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import expert.serebro.tion.device.BreezerSettings;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.function.Supplier;

@Data
@Slf4j
public class SendStateToTopicCommand implements ProcessingCommand<BreezerSettings> {
    private final Supplier<BreezerSettings> dataSupplier;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final IMqttClient client;
    private final String topic;

    @Override
    public String getName() {
        return "Send state to MQTT topic";
    }

    @Override
    public void run() {
        if (getDataSupplier() != null && topic != null && client.isConnected()) {
            try {
                MqttMessage msg = new MqttMessage(OBJECT_MAPPER.writeValueAsBytes(getDataSupplier().get()));
                msg.setQos(0);
                msg.setRetained(true);
                client.publish(topic, msg);
            } catch (JsonProcessingException | MqttException e) {
                log.error(String.format("Error sending message to topic %s", topic), e);
            }
        }
    }
}
