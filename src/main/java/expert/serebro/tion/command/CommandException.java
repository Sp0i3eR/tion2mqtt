package expert.serebro.tion.command;

public class CommandException extends Exception {
    private final Command command;

    public CommandException(String message, Command command) {
        super(message);
        this.command = command;
    }

    @Override
    public String getMessage() {
        return String.format("%s, command:%s", super.getMessage(), command.getName());
    }
}
