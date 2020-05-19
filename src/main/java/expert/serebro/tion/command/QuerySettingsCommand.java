package expert.serebro.tion.command;

import expert.serebro.tion.device.BreezerSettings;
import expert.serebro.tion.device.LiteDataEncoder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.function.Supplier;
import java.util.stream.Stream;

@Data
@EqualsAndHashCode(callSuper = true)
public class QuerySettingsCommand extends AbstractChainedResponseBluetoothCommand<String, BreezerSettings> {
    private final String payload;

    @Override
    public Stream<byte[]> stream() {
        return LiteDataEncoder.newGetDeviceParams().stream();
    }

    private BreezerSettings resetSpeedIfApplicable(BreezerSettings settings) {
        if (getDataSupplier() != null) {
            var str = getDataSupplier().get();
            if (!tryParseAndSetSpeed(payload, settings)) {
                if ("false".equalsIgnoreCase(str) || "off".equalsIgnoreCase(str)) {
                    settings.setEnabled(false);
                } else if ("true".equalsIgnoreCase(str) || "on".equalsIgnoreCase(str)) {
                    settings.setEnabled(true);
                }
            }
        }
        return settings;
    }

    public boolean tryParseAndSetSpeed(String payload, BreezerSettings settings) {
        Integer speed = null;
        try {
            speed = Integer.parseInt(payload);
        } catch (NumberFormatException e) {
            // Not int
        }
        if (speed == null) {
            if ("low".equalsIgnoreCase(payload)) {
                settings.setEnabled(true);
                settings.setFanSpeed(settings.getFirstFanPreset());
                return true;
            } else if ("medium".equalsIgnoreCase(payload)) {
                settings.setEnabled(true);
                settings.setFanSpeed(settings.getSecondFanPreset());
                return true;
            } else if ("high".equalsIgnoreCase(payload)) {
                settings.setEnabled(true);
                settings.setFanSpeed(settings.getThirdFanPreset());
                return true;
            }
            return false;
        } else if (speed >= 0 && speed <= 6) {
            if (speed == 0) {
                settings.setEnabled(false);
            } else {
                settings.setEnabled(true);
                settings.setFanSpeed(speed.byteValue());
            }
            return true;
        }
        return false;
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
    public Supplier<String> getDataSupplier() {
        if (getPayload() == null) {
            return null;
        }
        return this::getPayload;
    }
}
