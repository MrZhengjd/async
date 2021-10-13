package zheng.async;

/**
 * @author zheng
 */
public interface IWorker {
    <T> T work();

    <T> T defaultValue();
}
