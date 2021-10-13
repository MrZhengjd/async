package zheng.async;

/**
 * @author zheng
 */
public interface IWorker {
    <T> T work(Object param);

    <T> T defaultValue();
}
