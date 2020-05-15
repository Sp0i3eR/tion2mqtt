package expert.serebro.tion.topics;

import expert.serebro.tion.device.BreezerDevice;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TopicUtils {
    public static final String TOPIC_FORMAT = "tion_breezer/%s/%s";
    public static final String AVAILABILITY_TOPIC = "availability";
    public static final String AVAILABILITY_ONLINE = "online";
    public static final String AVAILABILITY_OFFLINE = "offline";
    public static final String STATE_TOPIC = "state";
    public static final String SPEED_TOPIC = "speed";
    public static final String PAIR_TOPIC = "pair";

    public static String formatTopic(BreezerDevice device, String topicName) {
        return String.format(TOPIC_FORMAT, device.getAddress().toUpperCase(), topicName);
    }
}
