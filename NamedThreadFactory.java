package zheng.async;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private static final AtomicInteger THREAD_NUMBER = new AtomicInteger(1);
    private final AtomicInteger nThreadNum = new AtomicInteger(1);
    private final String prefix;
    private final boolean daemoThread;
    private final ThreadGroup threadGroup;
    public NamedThreadFactory(String prefix, boolean daemoThread) {
        this.prefix = StringUtils.isNotEmpty(prefix)?prefix+"-thread-":"";
        this.daemoThread = daemoThread;
        SecurityManager s = System.getSecurityManager();
        threadGroup = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();

    }

    public NamedThreadFactory() {
        this("rpc-threadpool"+THREAD_NUMBER.getAndIncrement(),false);
    }

    public NamedThreadFactory(String prefix) {
        this(prefix,false);
    }


    @Override
    public Thread newThread(Runnable r) {
        String name = prefix + nThreadNum.getAndIncrement();
        Thread ret = new Thread(threadGroup,r,name,0);
        ret.setDaemon(daemoThread);
        return ret;
    }
}
