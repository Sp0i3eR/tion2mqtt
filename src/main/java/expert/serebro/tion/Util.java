package expert.serebro.tion;

import java.util.Formatter;

public class Util {

    private Util() {
    }

    public static Appendable hexEncode(byte[] buffer, Appendable sb) {
        try (final Formatter formatter = new Formatter(sb)) {
            for (byte b : buffer) {
                formatter.format("%02x", b);
            }
        }
        return sb;
    }

    public static String hexEncode(byte[] buffer) {
        return hexEncode(buffer, new StringBuilder()).toString();
    }




}
