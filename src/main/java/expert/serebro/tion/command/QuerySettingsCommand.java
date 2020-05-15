package expert.serebro.tion.command;

import expert.serebro.tion.device.BreezerSettings;
import expert.serebro.tion.device.LiteDataEncoder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.function.Supplier;
import java.util.stream.Stream;

@Data
@EqualsAndHashCode(callSuper = true)
public class QuerySettingsCommand extends AbstractChainedResponseBluetoothCommand<Integer, BreezerSettings> {
    private final Integer newSpeed;

    @Override
    public Stream<byte[]> stream() {
        return LiteDataEncoder.newGetDeviceParams().stream();
    }

    private BreezerSettings resetSpeedIfApplicable(BreezerSettings settings) {
        if (getDataSupplier() != null) {
            var speed = getDataSupplier().get();
            if (speed != null && speed >= 0 && speed <= 6) {
                if (speed == 0) {
                    settings.setEnabled(false);
                } else {
                    settings.setEnabled(true);
                    settings.setFanSpeed(speed.byteValue());
                }
            }
        }
        return settings;
    }

    @Override
    public BreezerSettings getResponse() {
        var data = getFullChain();
        if (data == null) {
            throw new IllegalStateException("Trying to get response from empty data");
        }
        return resetSpeedIfApplicable(LiteDataEncoder.decode(data));
    }


    @Override
    public String getName() {
        return "Query settings and set speed";
    }

    @Override
    long getResponseTimeout() {
        return 10;
    }

    @Override
    public Supplier<Integer> getDataSupplier() {
        if (getNewSpeed() == null) {
            return null;
        }
        return this::getNewSpeed;
    }
}
