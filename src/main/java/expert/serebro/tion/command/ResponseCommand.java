package expert.serebro.tion.command;

public interface ResponseCommand<R> extends Command {
    void responseCallback(byte[] bytes);

    boolean waitForResponse();

    R getResponse();
}
