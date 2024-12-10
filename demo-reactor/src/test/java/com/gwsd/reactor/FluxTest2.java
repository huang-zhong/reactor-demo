package com.gwsd.reactor;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

public class FluxTest2 {

    class SampleSubscriber<T> extends BaseSubscriber<T> {

        public void hookOnSubscribe(Subscription subscription) {
            System.out.println("----------Subscribed------------");
            request(1);
        }

        public void hookOnNext(T value) {
            System.out.println("value=" + value);
            request(1);
        }

    }

    @Test
    public void test() {
        SampleSubscriber<Integer> ss = new SampleSubscriber<Integer>();

        Flux<Integer> ints = Flux.range(1, 4);

        // 使用lambda
        ints.subscribe(i -> System.out.println("i="+i),
                error -> System.err.println("Error " + error),
                () -> {System.out.println("Done");},
                s -> s.request(10));

        // 使用SampleSubscriber
        ints.subscribe(ss);
    }
}
