package zheng.async;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author zheng
 */
public class TestAsync {
    public static void main(String[] args) {
        System.out.println("这里是ab并行完之后C再运行");
        Async async = new Async();

        Wrapper order = async.newWrapper(new IWorker() {
            @Override
            public <T> T defaultValue() {
                return null;
            }

            @Override
            public Order work(Object param) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return new Order("here is order");
            }
        }, new Listener() {
            @Override
            public void listen(Object object) {
                System.out.println("here is a order "+object);
            }
        });
        Wrapper wrapper = async.newWrapper(new IWorker() {
            @Override
            public <T> T defaultValue() {
                return null;
            }

            @Override
            public User work(Object param) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                throw new NullPointerException("don have it");
//                return new User("here is jack");
            }
        }, new Listener() {
            @Override
            public void listen(Object object) {
                System.out.println("here is a user "+object.toString());
            }
        });
        Group group = new Group();
        group.addWrapper(wrapper);
        group.addWrapper(order);
        CountDownLatch latch = new CountDownLatch(1);
//        Tem tem = new Tem((Order) order.getResult(),(User) wrapper.getResult());
        Wrapper next = async.newWrapper(new IWorker() {
            @Override
            public <T> T work(Object param) {
                System.out.println("here is process param "+group.getByWrapper(wrapper));
                System.out.println("here is  another process param "+group.getByWrapper(order));

                return null;
            }

            @Override
            public <T> T defaultValue() {
                return null;
            }
        }, new Listener() {
            @Override
            public void listen(Object object) {

            }
        });
//        next.setParam(tem);
        group.setNext(next);
//        async.asynRun(group);
        group.runWithTimeOut(2610l, TimeUnit.MILLISECONDS);
        while (true){
            if (group.getState() > 0){
                break;
            }
        }
        System.out.println("here is finish process ");
    }

    private static class Order{
        private String orderName;

        public Order(String orderName) {
            this.orderName = orderName;
        }

        public String getOrderName() {
            return orderName;
        }

        public void setOrderName(String orderName) {
            this.orderName = orderName;
        }

        @Override
        public String toString() {
            return "order name "+orderName;
        }
    }
    private static class User{
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public User(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "user name "+name;
        }
    }
}
