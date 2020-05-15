package expert.serebro.tion.command;

import java.util.stream.Stream;

public interface BluetoothCommand<T> extends ProcessingCommand<T> {
    Stream<byte[]> stream();

}
