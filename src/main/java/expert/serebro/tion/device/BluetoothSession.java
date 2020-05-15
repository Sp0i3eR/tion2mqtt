package expert.serebro.tion.device;

import expert.serebro.tion.command.BluetoothCommand;
import expert.serebro.tion.command.Command;
import expert.serebro.tion.command.CommandException;
import expert.serebro.tion.command.ResponseCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tinyb.BluetoothDevice;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
public class BluetoothSession implements Runnable {
    private static final String UART_SERVICE = "98f00001-3788-83ea-453e-f52244709ddb";
    private static final String UART_TX = "98f00002-3788-83ea-453e-f52244709ddb";
    private static final String UART_RX = "98f00003-3788-83ea-453e-f52244709ddb";

    private final BreezerDevice breezerDevice;
    private final Collection<Command> commands;


    @Override
    public void run() {
        BluetoothDevice device = breezerDevice.getDevice();
        try {
            breezerDevice.getLock().lock();
            log.debug(String.format("Session start for device %s", device.getAddress()));
            if (device.connect()) {
                log.debug(String.format("Device %s connected", device.getAddress()));
                var uartService = device.find(UART_SERVICE);
                if (uartService != null) {
                    log.debug(String.format("UART service found on %s", device.getAddress()));
                    var uartRx = uartService.find(UART_RX);
                    var uartTx = uartService.find(UART_TX);
                    if (uartTx == null || uartRx == null) {
                        return;
                    }
                    log.debug(String.format("UART Tx and Rx characteristic found on %s", device.getAddress()));
                    for (var command : commands) {
                        if (command instanceof ResponseCommand<?>) {
                            uartRx.enableValueNotifications(((ResponseCommand<?>) command)::responseCallback);
                            log.debug(String.format("Subscribing command %s for notifications", command.getName()));
                        } else {
                            uartRx.disableValueNotifications();
                            log.debug(String.format("Subscribing notifications %s", command.getName()));
                        }
                        log.debug(String.format("Running process code %s", command.getName()));
                        command.run();
                        if (command instanceof BluetoothCommand<?> && !((BluetoothCommand<?>) command).stream().allMatch(uartTx::writeValue)) {
                            throw new CommandException("Command sending failed", command);
                        }
                        if (command instanceof ResponseCommand<?> && !(((ResponseCommand<?>) command).waitForResponse())) {
                            throw new CommandException("Timed out waiting for response", command);
                        }
                        log.debug(String.format("Command %s processing successful", command.getName()));
                    }
                }
            }
        } catch (CommandException e) {
            log.error("Error executing command chain", e);
        } finally {
            log.debug(String.format("Session end for %s", device.getAddress()));
            device.disconnect();
            log.debug(String.format("Disconnected successfully %s", device.getAddress()));
            breezerDevice.getLock().unlock();
        }
    }
}
