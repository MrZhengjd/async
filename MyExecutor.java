package zheng.async;



import java.util.concurrent.*;

public class MyExecutor {
    public static ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 1, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(8),
            new NamedThreadFactory("asyn thread pool", true));
    public static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2,new NamedThreadFactory("schedule thread pool", true));
}
