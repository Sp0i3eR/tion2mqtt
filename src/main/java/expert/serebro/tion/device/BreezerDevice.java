package expert.serebro.tion.device;

import lombok.Data;
import tinyb.BluetoothDevice;

import java.util.concurrent.locks.ReentrantLock;

@Data
public class BreezerDevice {
    private final String address;
    private BreezerSettings settings;
    private boolean available;
    private BluetoothDevice device;
    private ReentrantLock lock = new ReentrantLock(true);
}
