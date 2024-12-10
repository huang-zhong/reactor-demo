package com.gwsd.reactor;


import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;

public class ReactorTest {

    @Test
    public void test() {

        Subscriber<String> subscriber = new Subscriber<String>() {
            private Subscription subscription;
            @Override
            public void onSubscribe(org.reactivestreams.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(String s) {
                // 接受到一个数据, 处理
                System.out.println("接受到数据: " + s);

                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

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
                System.out.println("Complete");
            }
        };
        String[] arr = {"apple", "banana", "orange"};
        Flux.just(arr).map(String::toUpperCase).subscribe(subscriber);
    }
}
