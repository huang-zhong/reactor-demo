package com.gwsd.reactor;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

public class MyProcessor2 extends SubmissionPublisher<String> implements Flow.Processor<String, String> {
    private Flow.Subscription subscription;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(String item) {
        // 接受到一个数据, 处理
        System.out.println(Thread.currentThread().getName() + "接受到数据: " + item);
        // 向下游传递数据
        submit(", processed2: " + item);
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        // 处理异常
        System.out.println(Thread.currentThread().getName() + "processor2,处理异常: " + throwable.getMessage());
    }

    @Override
    public void onComplete() {
        // 处理完成
        System.out.println(Thread.currentThread().getName() + "processor2,处理完成");
    }
}
