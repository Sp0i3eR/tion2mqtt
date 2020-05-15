package expert.serebro.tion.device;

import expert.serebro.tion.command.CommandType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LiteDataEncoder {


    private static final int FRAME_SIZE = 19;
    private static final byte[] MAGIC_SUFFIX = {-86, -69};
    private static final byte END_POCKET_ID = -64;
    private static final byte FIRST_POCKET_ID = 0;
    private static final byte MAGIC_NUMBER = 0x3a;
    private static final byte MIDDLE_POCKET_ID = 64;
    private static final byte SINGLE_POCKET_ID = -128;


    public static Collection<byte[]> newGetDeviceInfo() {
        return repackCommandToFrames(
                createCommandFrame(CommandType.GET_DEVICE_INFO, null)
        );
    }

    public static Collection<byte[]> newGetDeviceParams() {
        return repackCommandToFrames(
                createCommandFrame(CommandType.REQUEST_PARAMS, null)
        );
    }

    public static Collection<byte[]> newSetDeviceParams(BreezerSettings device) {
        return repackCommandToFrames(
                createCommandFrame(CommandType.SET_PARAMS, device)
        );
    }

    private static byte[] createCommandFrame(CommandType commandType, BreezerSettings device) {

        byte[] command = commandType.getCommand();
        byte[] commandDataArr = commandType == CommandType.SET_PARAMS ? encode(device) : null;

        int randomSize = commandType == CommandType.GET_DEVICE_INFO ? 4 : 8;

        byte[] random = new byte[randomSize + 1];
        new Random().nextBytes(random);

        int packetLength = 6 + randomSize + command.length;

        if (commandDataArr != null) {
            packetLength += commandDataArr.length;
        }

        byte[] packet = new byte[packetLength];
        int position = 0;
        // Первые 2 байта длинна пакета
        packet[position++] = (byte) packetLength;
        packet[position++] = (byte) (packetLength >>> 8);
        // Магическое число
        packet[position++] = MAGIC_NUMBER;
        // Случайный байт
        packet[position++] = random[0];
        // Команда
        for (int i = command.length - 1; i >= 0; i--) {
            packet[position] = command[i];
            position++;
        }
        // Случайные 4 (или 8 ) байта
        System.arraycopy(random, 1, packet, position, randomSize);
        position += randomSize;
        // Данные команды
        if (commandDataArr != null) {
            System.arraycopy(
                    commandDataArr, 0,
                    packet, position,
                    commandDataArr.length
            );
            position += commandDataArr.length;
        }
        // Типа контрольная сумма
        packet[position++] = MAGIC_SUFFIX[1];
        packet[position] = MAGIC_SUFFIX[0];
        return packet;
    }

    private static byte[] repackCommandToFrame(byte[] command, FrameNumber frameNumber) {
        byte[] frame = new byte[(command.length + 1)];
        switch (frameNumber) {
            case FIRST:
                frame[0] = FIRST_POCKET_ID;
                break;
            case MIDDLE:
                frame[0] = MIDDLE_POCKET_ID;
                break;
            case LAST:
                frame[0] = END_POCKET_ID;
                break;
            default:
                frame[0] = SINGLE_POCKET_ID;
                break;
        }
        System.arraycopy(command, 0, frame, 1, command.length);
        return frame;
    }

    private static List<byte[]> repackCommandToFrames(byte[] command) {
        int totalFrames = command.length / FRAME_SIZE;
        if (command.length % FRAME_SIZE > 0) {
            totalFrames++;
        }
        if (totalFrames == 1) {
            return Collections.singletonList(repackCommandToFrame(command, FrameNumber.SINGLE));
        }
        List<byte[]> result = new ArrayList<>();
        for (int i = 0; i < totalFrames; i++) {
            FrameNumber frameNumber;
            if (i == 0) {
                frameNumber = FrameNumber.FIRST;
            } else if (i == totalFrames - 1) {
                frameNumber = FrameNumber.LAST;
            } else {
                frameNumber = FrameNumber.MIDDLE;
            }
            int frameStart = i * FRAME_SIZE;
            int frameEnd = frameStart + FRAME_SIZE;
            result.add(
                    repackCommandToFrame(
                            Arrays.copyOfRange(
                                    command,
                                    frameStart,
                                    frameEnd <= command.length ? FRAME_SIZE + frameStart : command.length
                            ),
                            frameNumber
                    )
            );
        }
        return result;
    }


    public static BreezerSettings decode(final byte[] data) {
        byte[] result = new byte[data.length - 17];
        System.arraycopy(data, 15, result, 0, result.length);
        return BreezerSettings.builder()
                .enabled(isFlag(result[0], 0))
                .soundIndicationEnabled(isFlag(result[0], 1))
                .lightIndicationEnabled(isFlag(result[0], 2))
                .heaterExist(isFlag(result[0], 7))
                .heaterEnabled(isFlag(result[0], 6))
                .heaterTemp(result[3])
                .fanSpeed(result[4])
                .inputTemp(result[5])
                .filterResourceInSeconds(ByteBuffer.wrap(new byte[]{result[16], result[17], result[18], result[19]}).order(ByteOrder.LITTLE_ENDIAN).getInt())
                .errors(ByteBuffer.wrap(new byte[]{result[24], result[25], result[26], result[27]}).order(ByteOrder.LITTLE_ENDIAN).getInt())
                .firstTempPreset(result[48])
                .secondTempPreset(result[49])
                .thirdTempPreset(result[50])
                .firstFanPreset(result[51])
                .secondFanPreset(result[52])
                .thirdFanPreset(result[53])
                .build();
    }

    private static byte[] encode(BreezerSettings device) {
        byte flags = (byte) (
                toFlag(device.isEnabled(), 0)
                        | toFlag(device.isSoundIndicationEnabled(), 1)
                        | toFlag(device.isLightIndicationEnabled(), 2)
                        | toFlag(device.isHeaterEnabled(), 4)
                // |toFlag(device.factoryReset,6)
        );
        return new byte[]{
                flags,
                0, // resetFilterDays
                (device.getFanSpeed() == 0 || device.getFanSpeed() > 0 || device.getHeaterTemp() > 0) ? (byte) 2 : 1,
                device.getHeaterTemp(),
                device.getFanSpeed() == 0 ? 1 : device.getFanSpeed(),
                device.getFirstTempPreset(),
                device.getSecondTempPreset(),
                device.getThirdTempPreset(),
                device.getFirstFanPreset(),
                device.getSecondFanPreset(),
                device.getThirdFanPreset(),
                0, //filterDays hi
                0, //filterDays lo
                0};
    }

    private static boolean isFlag(byte data, int shift) {
        return ((data >> shift) & 1) == 1;
    }

    private static byte toFlag(boolean data, int shift) {
        return (byte) ((data ? 1 : 0) >> shift);
    }
}
