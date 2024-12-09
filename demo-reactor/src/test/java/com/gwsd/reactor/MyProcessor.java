package com.gwsd.reactor;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

public class MyProcessor extends SubmissionPublisher<String> implements Flow.Processor<Integer, String> {
    private Flow.Subscription subscription;
    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(Integer item) {
        System.out.println(Thread.currentThread().getName() + "Received: " + item);
        // 过滤掉小于0的, 然后发布出去
        if (item > 0) {
            this.submit(Thread.currentThread().getName() + "转换后的数据:" + item);
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
        // 数据处理完毕(发布者调用了onComplete方法)
        System.out.println(Thread.currentThread().getName() + "Done");
        this.close();
    }
}
