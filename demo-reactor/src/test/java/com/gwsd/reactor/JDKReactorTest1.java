package com.gwsd.reactor;

import org.junit.jupiter.api.Test;

import java.util.Observer;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;

// 参考博客：https://blog.csdn.net/A_art_xiang/article/details/129578800
public class JDKReactorTest1 {

    /**
     * JDK9之前基于Observer/Observable接口而实现的观察者模式：
     * 可以添加多个观察者，当被观察者调用setChanged()方法时，会通知所有观察者，并调用update()方法。
     * 但是JDK9中引入了新的Reactive Streams接口，可以更加方便地实现观察者模式。
     */
    @Test
    public void test0(){
        ObserverDemo demo = new ObserverDemo();
        demo.addObserver(new Observer() {
            @Override
            public void update(java.util.Observable o, Object arg) {
                System.out.println("Received: " + arg);
            }
        });
        demo.addObserver(new Observer() {
            @Override
            public void update(java.util.Observable o, Object arg) {
                System.out.println("updated: " + arg);
            }
        });
        demo.setChanged();
        demo.notifyObservers("Hello");
    }

    /**
     * JDK9引入的Reactive Streams接口：
     * SubmissionPublisher：发布者，可以向订阅者发布数据。
     * Subscriber：订阅者，可以订阅发布者。
     * Subscription：订阅，用于控制发布者的发布速度。
     */
    @Test
    public void testNewObserver(){

        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
        Subscriber<String> subscriber = new  Subscriber<>() {

            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                System.out.println("Subscribed");
                subscription.request(1);
            }

            @Override
            public void onNext(String item) {
                System.out.println("Received: " + item);
                subscription.request(1);
                throw new IllegalArgumentException("Error");
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("Completed");
            }
        };
        publisher.subscribe(subscriber);
        publisher.submit("Hello");
        publisher.close();
    }

    @Test
    public void customeProcessor() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " start");
        // 1. 定义发布者, 发布的数据类型是 Integer
        // 直接使用jdk自带的SubmissionPublisher
        SubmissionPublisher<Integer> publiser = new SubmissionPublisher<>();
        // 2. 定义处理器, 对数据进行过滤, 并转换为String类型
        MyProcessor processor = new MyProcessor();
        // 3. 发布者 和 处理器 建立订阅关系
        publiser.subscribe(processor);

        // 定义处理器2, 对数据进行过滤, 并转换为String类型
        MyProcessor2 processor2 = new MyProcessor2();
        // 3. 处理器 和 处理器2 建立订阅关系
        processor.subscribe(processor2);
        // 这里可以继续订阅其他处理器, 形成链式处理
        //.....

        // 4. 定义最终订阅者, 消费 String 类型数据
        Subscriber<String> subscriber = new Subscriber<>() {

            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                // 保存订阅关系, 需要用它来给发布者响应
                this.subscription = subscription;

                // 请求一个数据
                this.subscription.request(1);
            }

            @Override
            public void onNext(String item) {
                // 接受到一个数据, 处理
                System.out.println(Thread.currentThread().getName() + "接受到数据: " + item);

                // 处理完调用request再请求一个数据
                this.subscription.request(1);

                // 或者 已经达到了目标, 调用cancel告诉发布者不再接受数据了
                // this.subscription.cancel();
            }

            @Override
            public void onError(Throwable throwable) {
                // 出现了异常(例如处理数据的时候产生了异常)
                throwable.printStackTrace();

                // 我们可以告诉发布者, 后面不接受数据了
                this.subscription.cancel();
            }

            @Override
            public void onComplete() {
                // 全部数据处理完了(发布者关闭了)
                System.out.println(Thread.currentThread().getName() + "处理完了!");
            }

        };
        // 5. 处理器 和 最终订阅者 建立订阅关系
        processor.subscribe(subscriber);
        // 6. 生产数据, 并发布
        // 这里忽略数据生产过程
        publiser.submit(-111);
        publiser.submit(111);
        // 7. 结束后 关闭发布者
        // 正式环境 应该放 finally 或者使用 try-resouce 确保关闭
        publiser.close();

        System.out.println(Thread.currentThread().getName() + " end");
        // 主线程延迟停止, 否则数据没有消费就退出
        Thread.currentThread().join(1000);
    }

}
