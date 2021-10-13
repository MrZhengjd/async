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

    public void addWrapper(Wrapper wrapper){
        wrappers.add(wrapper);
        wrapper.setGroup(this);
    }

    /**
     * 当前组完成了操作后的修改操作
     */
    public boolean setSuccess(){
//        System.out.println("set suceess -----------");
        return state.compareAndSet(0,1);
    }

    /**
     * 判断是否全部都完成了，如果有next接着判断next的
     * @return
     */
    public boolean checkDone(){
        if (state.get() != 0){
            if (next != null){
                return next.checkDone();
            }
            return true;
        }

        return false;
    }
    public boolean checkAllDone(){
        boolean isAllDone = true;
        for (Wrapper wrapper : wrappers){
            if (!wrapper.checkDone()){
                isAllDone = false;
                break;
            }
        }
        return isAllDone;
    }
    /**
     * 快速失败操作
     */
    public void fastFail(){
        System.out.println("set failed ------------");
        boolean isAllDone = checkAllDone();
        if (isAllDone){
            System.out.println("is all done --------");
            return;
        }
        int count = 1;
        boolean s = state.compareAndSet(0, 2);
//        System.out.println("here is set failed -----------------");
        if (!s){
            for (int i = 0;i< count ;i++){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (state.get() == 1){
                    return;
                }
                isAllDone = checkAllDone();
                if (isAllDone){
                    return;
                }
            }
        }
//        System.out.println("here is coming "+state.get());
//        state.set(2);
        for (Wrapper wrapper : wrappers){
            wrapper.setException();
            if (wrapper.checkIsNullResult()){
                wrapper.setResult(wrapper.defaultExResult(new TimeoutException("here is timeout ")));
//                wrapper.getListener().listen(wrapper.getResult());
            }
        }
//        System.out.println("here is fast fail coming");
    }

    /**
     * 超时操作，如果超时就快速失败
     * 快速失败不是用future的get方法
     * 而是添加一个线程，多久之后去检测是否完成了操作，完成了就不处理
     * 没完成了就快速失败
     * @param delay
     * @param timeUnit
     */
    public void runWithTimeOut(Long delay, TimeUnit timeUnit){
        runGroup();
        MyExecutor.executorService.schedule(new Runnable() {
            @Override
            public void run() {
                if (state.get() != 1){
//                    System.out.println("call fast failed----------------");
                    fastFail();

                }
            }
        },delay,timeUnit);
    }

    /**
     * 获取当前组内运行状态
     * @return
     */
    public int getState(){
        return state.get();
    }

    /**
     * 运行组内的所有Wrapper对象
     */
    public void runGroup(){
        wrapperCount = wrappers.size();
//        System.out.println("wrpper count "+wrapperCount);
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

    /**
     * 为wrapper生成一个新的group
     * @param wrapper
     */
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
