package expert.serebro.tion.command;

import expert.serebro.tion.device.BreezerSettings;
import expert.serebro.tion.device.LiteDataEncoder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.function.Supplier;
import java.util.stream.Stream;

@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateSettingsCommand extends AbstractChainedResponseBluetoothCommand<BreezerSettings, BreezerSettings> {
    private final Supplier<BreezerSettings> dataSupplier;

    @Override
    public Stream<byte[]> stream() {
        if (getDataSupplier() == null) {
            throw new IllegalArgumentException("Calling update settings without settings supplier is forbidden");
        }
        return LiteDataEncoder.newSetDeviceParams(getDataSupplier().get()).stream();
    }


    @Override
    public BreezerSettings getResponse() {
        var data = getFullChain();
        if (data == null) {
            throw new IllegalStateException("Trying to get response from empty data");
        }
        return LiteDataEncoder.decode(data);
    }


    @Override
    public String getName() {
        return "Update settings";
    }

    @Override
    long getResponseTimeout() {
        return 10;
    }
}
