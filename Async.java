package zheng.async;

import java.util.concurrent.TimeUnit;

/**
 * @author zheng
 */
public class Async {

    public Wrapper newWrapper(IWorker iWorker, Listener listener){
        Wrapper wrapper = new Wrapper();
        wrapper.setListener(listener);
        wrapper.setWorker(iWorker);
//        wrapper.doWork();
        return wrapper;
    }

    /**
     * 没有group的时候直接运行Wrapper
     * 串行运行
     * @param iWorker
     * @param listener
     */
    public void runWappersync(IWorker iWorker,Listener listener){
        newWrapper(iWorker,listener).doWork();
    }

    /**
     * 并行运行
     * @param iWorker
     * @param listener
     * @param group
     */
    public void runWrapperAsync(IWorker iWorker,Listener listener,Group group){
        asynRun(generateGroup(iWorker,listener,group));
    }
    public void runSync(IWorker iWorker,Listener listener){
        asynRun(newGroup(iWorker,listener));
    }
    private Group newGroup(IWorker iWorker,Listener listener){
        Wrapper wrapper = newWrapper(iWorker,listener);
        Group tem = new Group();
        tem.generateWrapper(wrapper);
        return tem;
    }
    public void asynRunWithTimeOut(Group group, Long delay, TimeUnit timeUnit){
        group.runWithTimeOut(delay,timeUnit);
    }
    /**
     * 如果是只有一个wrapper 就运行wrapper
     * @param group
     */
    public void asynRun(Group group){
        int size = group.getWrappers().size();
        if (size == 1){
            group.getWrappers().get(0).doWork();
            return;
        }
        group.runGroup();
//

    }
    public Group generateGroup(IWorker iWorker, Listener listener, Group group){
       Wrapper wrapper = newWrapper(iWorker,listener);
       if (group != null){
           group.getWrappers().add(wrapper);
           return group;
       }else {
           Group tem = new Group();
           tem.generateWrapper(wrapper);
           return tem;
       }

    }


}
