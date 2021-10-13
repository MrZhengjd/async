package zheng.async;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zheng
 */
public class Wrapper {

    public Wrapper() {
    }

    private long v1,v2,v3,v4,v5,v6,v7;

    private volatile WorkResult result = WorkResult.defaultResult();
    public boolean checkIsNullResult() {
        return ResultState.DEFAULT == result.getResultState();
    }
    public WorkResult getResult() {
        return result;
    }

    public void setResult(WorkResult result) {
        this.result = result;
    }

    /**
     * 标记该事件是否已经被处理过了，譬如已经超时返回false了，后续rpc又收到返回值了，则不再二次回调
     * 经试验,volatile并不能保证"同一毫秒"内,多线程对该值的修改和拉取
     * <p>
     * 1-finish, 2-error, 3-working
     */
    private AtomicInteger state = new AtomicInteger(0);
    public WorkResult defaultExResult(Exception ex) {
        result.setResultState(ResultState.EXCEPTION);
        result.setResult(worker.defaultValue());
        result.setEx(ex);
        return result;
    }
    public int getState(){
        return state.get();
    }
    public void resetState(){
        state.compareAndSet(1,0);
    }

    public Wrapper newWrapper(IWorker iWorker, Listener listener){
        return new Wrapper(listener,iWorker);
    }
    /**
     * 这里将会使用的例子是a->b
     * 并行c
     * 然后获取结果
     */
    private Listener listener;
    private IWorker worker;
    private Object param;
    private Wrapper next;
    private Group group;
    private boolean check;


    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public Wrapper(Listener listener, IWorker worker) {
        this.listener = listener;
        this.worker = worker;
    }

    public Wrapper getNext() {
        return next;
    }

    public void setNext(Wrapper next) {
        this.next = next;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void setException(){
        state.set(2);
    }
    public void doWork(){
        state.compareAndSet(0,3);
        Wrapper wrapper = this;

        MyExecutor.executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Object work = worker.work(param);
                    if (group != null && group.getState() == 2){
                        throw new NullPointerException("some one have problems and roke back");
                    }
                    result = new WorkResult(work,ResultState.SUCCESS);

                    listener.listen(result);

                    if (next != null){
                        next.doWork();
                    }else {

//
                        state.compareAndSet(3,1);
                        if (group != null){
//
                            group.put(wrapper,result);
                            Integer finishCount = group.incrementAndGet();
                            if (finishCount >= group.getWrapperCount()){
                                group.setSuccess();
                                if (group.getGroupListener() != null){
                                    group.getGroupListener().groupCall(group);
                                }
                                if (group.getNext() != null){
                                    group.getNext().runGroup();
                                }
                            }
                        }

                    }
                }catch (Exception e){
                    state.set(2);
                    if (group != null){
                        group.fastFail();
                    }
                    result = new WorkResult<>(null,ResultState.EXCEPTION);
                }

            }
        });

    }
    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public IWorker getWorker() {
        return worker;
    }

    public void setWorker(IWorker worker) {
        this.worker = worker;
    }

    public Object getParam() {
        return param;
    }

    public void setParam(Object param) {
        this.param = param;
    }
}
