package expert.serebro.tion.device;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder(toBuilder = true)
public class BreezerSettings implements Serializable {

    private byte fanSpeed;
    private byte heaterTemp;
    private boolean enabled;
    private boolean heaterExist;
    private boolean heaterEnabled;
    private boolean lightIndicationEnabled;
    private boolean soundIndicationEnabled;
    private int filterResourceInSeconds;
    private int errors;
    private byte inputTemp;
    private byte firstTempPreset;
    private byte secondTempPreset;
    private byte thirdTempPreset;
    private byte firstFanPreset;
    private byte secondFanPreset;
    private byte thirdFanPreset;
    private String versionOfFirmwareHex;

}
