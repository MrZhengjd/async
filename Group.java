package zheng.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zheng
 */
public class Group {
    private static Map<Long,Group> wrapperMap = new HashMap<>();
    private static AtomicLong groupId = new AtomicLong(0);
    private long v1,v2,v3,v4,v5,v6,v7;
    private volatile List<Wrapper> wrappers = new ArrayList<>();
    private Long id;
    private int wrapperCount = 0;
    //* 1-finish, 2-error,
    private AtomicInteger state = new AtomicInteger(0);
    private GroupListener groupListener;
    private Group next;
//    private List<Object> datas = new ArrayList<>();
    private AtomicInteger finishCount = new AtomicInteger(0);
    private Map<Wrapper,Object> wrapperResultMap = new HashMap<>();
    public void put(Wrapper wrapper,Object data){
        wrapperResultMap.put(wrapper,data);
    }
    public <T> T getByWrapper(Wrapper wrapper){
        return (T) wrapperResultMap.get(wrapper);
    }
    public Integer incrementAndGet(){
        return finishCount.incrementAndGet();
    }
//    public List<Object> getDatas() {
//        return datas;
//    }

    public int getWrapperCount() {
        return wrapperCount;
    }

    public Map<Wrapper, Object> getWrapperResultMap() {
        return wrapperResultMap;
    }

    public void setWrapperResultMap(Map<Wrapper, Object> wrapperResultMap) {
        this.wrapperResultMap = wrapperResultMap;
    }

    public void setWrapperCount(int wrapperCount) {
        this.wrapperCount = wrapperCount;
    }

//    public void setDatas(List<Object> datas) {
//        this.datas = datas;
//    }

    public void addWrapper(Wrapper wrapper){
        wrappers.add(wrapper);
        wrapper.setGroup(this);
    }
    public void setSuccess(){
        state.compareAndSet(0,1);
    }
    public void fastFail(){
        state.compareAndSet(0,2);
        for (Wrapper wrapper : wrappers){
            wrapper.setException();
            if (wrapper.checkIsNullResult()){
                wrapper.setResult(wrapper.defaultExResult(new TimeoutException("here is timeout ")));
//                wrapper.getListener().listen(wrapper.getResult());
            }
        }
        System.out.println("here is fast fail coming");
    }
    public void runWithTimeOut(Long delay, TimeUnit timeUnit){
        runGroup();
        MyExecutor.executorService.schedule(new Runnable() {
            @Override
            public void run() {
                if (state.get() != 1){
                    fastFail();

                }
            }
        },delay,timeUnit);
    }
    public int getState(){
        return state.get();
    }
    public void runGroup(){
        wrapperCount = wrappers.size();
//        datas.clear();
        for (Wrapper wrapper : wrappers){
            wrapper.doWork();
        }

    }
    public Group getNext() {
        return next;
    }

    public void setNext(Group next) {
        this.next = next;
    }

    /**
     * 生成新的下个group 是串行的
     * @param wrapper
     */
    public void setNext(Wrapper wrapper){
        Group next = new Group();
        next.generateWrapper(wrapper);
        setNext(next);
    }
    public void generateWrapper(Wrapper wrapper){

        Long id = groupId.getAndIncrement();
        setId(id);
        getWrappers().add(wrapper);
        wrapper.setGroup(this);
        wrapperMap.put(id,this);

    }

    public GroupListener getGroupListener() {
        return groupListener;
    }

    public void setGroupListener(GroupListener groupListener) {
        this.groupListener = groupListener;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Wrapper> getWrappers() {
        return wrappers;
    }

    public void setWrappers(List<Wrapper> wrappers) {
        this.wrappers = wrappers;
    }
}
