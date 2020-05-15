package expert.serebro.tion.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CommandType {
    SET_PARAMS(new byte[]{0x12, 0x30}),
    REQUEST_PARAMS(new byte[]{0x12, 0x32}),
    GET_DEVICE_INFO(new byte[]{0x40, 0x9});

    private final byte[] command;
}
