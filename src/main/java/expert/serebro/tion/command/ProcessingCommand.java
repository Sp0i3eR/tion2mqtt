package expert.serebro.tion.command;

import java.util.function.Supplier;

public interface ProcessingCommand<T> extends Command {
    Supplier<T> getDataSupplier();
}
