package com.gwsd.reactor;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FluxTest1 {

    @Test
    public void test1(){
        Flux.just("apple", "banana", "orange", "grape", "pear").subscribe(System.out::println);
    }

    @Test
    public void test2(){
        String[] fuirts = {"apple", "banana", "orange", "grape", "pear"};
        //Flux.just(fuirts).subscribe(System.out::println);
        Flux.fromArray(fuirts).subscribe(System.out::println);
    }

    @Test
    public void test3(){
        List<String> fuirts = List.of("apple", "banana", "orange", "grape", "pear");
        //Flux.just(fuirts).subscribe(System.out::println);
        Flux.fromIterable(fuirts).subscribe(System.out::println);
    }

    @Test
    public void test4(){
        Stream<String> furits = Stream.of("apple", "banana", "orange", "grape", "pear");
        //Flux.just(furits).subscribe(System.out::println);
        Flux.fromStream(furits).subscribe(System.out::println);
    }

    @Test
    public void test5(){
       //Flux.range(-1, 10).subscribe(System.out::println);
        // 每秒发布⼀个值的Flux，通过interval()⽅法创建的Flux会从0开始发布值，并且后续的条⽬依次递增。
        // 因为interval()⽅法没有指定最⼤值，所以它可能会永远运⾏。我们也可以使⽤take()⽅法将结果限制为前5个条⽬。
        Flux.interval(Duration.ofSeconds(1))
                .take(10)
                .subscribe(System.out::println);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test6(){
       Flux.push((sink) -> {
           sink.next("apple");
           sink.next("banana");
           sink.next("orange");
           sink.next("grape");
           sink.next("pear");
           sink.complete();
       }).subscribe(System.out::println);
    }

    @Test
    public void test7(){
        Flux.generate(sink -> {
            sink.next("apple");
            sink.next("banana");
            sink.next("orange");
            sink.next("grape");
            sink.next("pear");
            sink.complete();
        }).subscribe(System.out::println);
    }


    @Test
    public void test8(){
        Flux.create(sink -> {
            sink.next("apple");
            sink.next("banana");
            sink.next("orange");
            sink.next("grape");
            sink.next("pear");
            sink.complete();
        }).subscribe(System.out::println);
    }

    @Test
    public void test9(){
        Flux.push((Consumer<FluxSink<String>>) fluxSink -> {
            // 从数据库中获取数据，使用fluxSink追加到响应式流中，将命令处理方式转化为响应式处理方式
            fluxSink.next("hello1");
            fluxSink.next("hello2");
            fluxSink.complete(); // 生成onComplete信号
        }).subscribe(System.out::println);
    }

    //使用mergeWith合并过的两个FLux，并没有严格意义上的先后之分，谁产生了数据就接着消费，与同一个无异。
    @Test
    public void test10() throws InterruptedException {
        Flux<String> characterFlux = Flux
                .just("Garfield", "Kojak", "Barbossa")
                .delayElements(Duration.ofMillis(500)); // 每500毫秒发布⼀个数据

        Flux<String> foodFlux = Flux
                .just("Lasagna", "Lollipops", "Apples")
                .delaySubscription(Duration.ofMillis(250)) // 订阅后250毫秒后开始发布数据
                .delayElements(Duration.ofMillis(500)); // 每500毫秒发布⼀个数据

        // 使⽤mergeWith()⽅法，将两个Flux合并，合并过后的Flux数据项发布顺序与源Flux的发布时间⼀致
        // Garfield Lasagna Kojak Lollipops Barbossa Apples
        Flux<String> mergedFlux = characterFlux.mergeWith(foodFlux);

        mergedFlux.subscribe(System.out::println);

        // 阻塞，等待结果
        Thread.sleep(100000);
    }

    @Test
    public void test11() throws InterruptedException {
        Flux<String> characterFlux = Flux
                .just("Garfield", "Kojak", "Barbossa");
        Flux<String> foodFlux = Flux
                .just("Lasagna", "Lollipops", "Apples");
        // 当两个Flux对象压缩在⼀起的时候，它将会产⽣⼀个新的发布元组的Flux，其中每个元组中都包含了来⾃每个源Flux的数据项
        // 这个合并后的Flux发出的每个条⽬都是⼀个Tuple2（⼀个容纳两个其他对象的容器对象）的实例，其中包含了来⾃每个源Flux的数据项，并保持着它们发布的顺序。
        Flux.zip(characterFlux, foodFlux).subscribe(tuple2-> System.out.println(tuple2.getT1() + "|" + tuple2.getT2()));
        //执行结果：
        //Garfield|Lasagna
        //Kojak|Lollipops
        //Barbossa|Apples
    }

    //如果你不想使⽤Tuple2，⽽想要使⽤其他类型，就可以为zip()⽅法提供⼀个合并函数来⽣成你想要的任何对象，合并函数会传⼊这两个数据项。
    @Test
    public void test12() throws InterruptedException {
        Flux<String> characterFlux = Flux
                .just("Garfield", "Kojak", "Barbossa");
        Flux<String> foodFlux = Flux
                .just("Lasagna", "Lollipops", "Apples");
        // 压缩成自定义对象
        Flux.zip(characterFlux, foodFlux, (s1, s2) -> s1 + " eats " + s2).subscribe(System.out::println);
        //执行结果：
        //Garfield eats Lasagna
        //Kojak eats Lollipops
        //Barbossa eats Apples
    }

    //first()操作会在两个Flux对象中选择第⼀个发布值的Flux，并再次发布它的值。
    @Test
    public void test13() throws InterruptedException {
        Flux<String> slowFlux = Flux.just("tortoise", "snail", "sloth").delaySubscription(Duration.ofMillis(100));
        Flux<String> fastFlux = Flux.just("hare", "cheetah", "squirrel");

        Flux.firstWithSignal(slowFlux, fastFlux).subscribe(System.out::println);

    }

    @Test
    public void test14() throws InterruptedException {
        //// 这是skip()操作的另⼀种形式，将会产⽣⼀个新Flux，在发布来⾃源Flux的数据项之前等待指定的⼀段时间
        Flux.just("one", "two", "skip a few", "ninety nine", "one hundred")
                .delayElements(Duration.ofSeconds(1)) // 每1秒一个
                .skip(Duration.ofSeconds(1)) // 跳过4秒
                //.take(Duration.ofSeconds(2)) // 取2秒
                .subscribe(System.out::println);
        Thread.sleep(10000);
    }

    @Test
    public void test15() throws InterruptedException {
        // take操作只发布传⼊Flux中前⾯指定数⽬的数据项，然后将取消订阅
        Flux<String> nationalParkFlux = Flux.just(
                        "Yellowstone", "Yosemite", "Grand Canyon",
                        "Zion", "Grand Teton")
                .take(3);
        nationalParkFlux.subscribe(System.out::println);
        /**
         * 执行结果：
         * Yellowstone
         * Yosemite
         * Grand Canyon
         */
    }

    @Test
    public void test16() throws InterruptedException {
        /// 在订阅之后的前3.5秒发布数据条⽬。
        Flux<String> nationalParkFlux = Flux.just(
                        "Yellowstone", "Yosemite", "Grand Canyon",
                        "Zion", "Grand Teton")
                .delayElements(Duration.ofSeconds(1))
                .take(Duration.ofMillis(3500));
        nationalParkFlux.subscribe(System.out::println);
        // 阻塞，等待结果
        Thread.sleep(100000);
        /**
         * 执行结果：
         * Yellowstone
         * Yosemite
         * Grand Canyon
         */
    }

    @Test
    public void test17() throws InterruptedException {
        Flux<List<String>> fruitFlux = Flux.just("apple", "banana", "orange", "grape", "pear")
                .buffer(3);
        fruitFlux.subscribe(System.out::println);

        fruitFlux.flatMap(x->Flux.fromIterable(x).map(String::toUpperCase).subscribeOn(Schedulers.parallel()))
                .subscribe(x->System.out.println(Thread.currentThread().getName() + " " + x));
    }


    @Test
    public void test18() throws InterruptedException {
        Flux<String> fruitFlux = Flux.just("apple", "banana", "orange", "grape", "pear");
        //使⽤不带参数的buffer()⽅法可以将Flux发布的所有数据项都收集到⼀个List中：
        Flux<List<String>> bufferedFlux = fruitFlux.buffer();
        bufferedFlux.subscribe(System.out::println);

        //collectList操作也可以将所有数据收集到一个List
        Mono<List<String>> fruitListMono = fruitFlux.collectList();
        //fruitListMono.block();
        //System.out.println(fruitListMono.block());
        fruitListMono.subscribe(System.out::println);
    }

    //key相同的，会被覆盖。
    @Test
    public void test19() throws InterruptedException {
        Flux<String> animalFlux = Flux.just(
                "aardvark", "elephant", "koala", "eagle", "lion", "giraffe", "bat", "fox");
        animalFlux.collectMap(a->a).subscribe(System.out::println);

        Thread.sleep(100000);
    }

    //collectMultimap产生自定义Map，key为自定义类型，value为List。
    @Test
    @SuppressWarnings("unchecked")
    public void test20() throws InterruptedException {
        Flux.just(1, 2, 3, 4, 5)
                .collectMultimap(
                        item -> "key:" + item,
                        item -> {
                            List<String> values = new ArrayList<>();
                            for (int i = 0; i < item; i++) {
                                values.add("value:" + i);
                            }
                            return values;
                        },
                        () -> { // 扩充
                            Map map = new HashMap();
                            map.put("other", "other");
                            return map;
                        })
                .subscribe(System.out::println);
    }

    //repeat操作会使⽤指定的次数重复发布数据项。
    @Test
    public void test21() throws InterruptedException {
        Flux.just(1, 2, 3).repeat(1).subscribe(System.out::print);

        //输出：123123
    }

    //all 断言，所有数据项都满足条件，才会输出true。
    @Test
    public void test22() throws InterruptedException {
        Flux.just("aardvark", "elephant", "koala", "eagle", "kangaroo")
                .all(s -> s.length() >= 5).subscribe(System.out::println);
    }

    //any 断言，只要有数据项满足条件，就会输出true。
    @Test
    public void test23() throws InterruptedException {
        Flux.just("aardvark", "elephant", "koala", "eagle", "kangaroo")
              .any(s -> s.length() > 5).subscribe(System.out::println);
    }

    //count 计数。
    @Test
    public void test24() throws InterruptedException {
        Flux.just("aardvark", "elephant", "koala", "eagle", "kangaroo")
              .count().subscribe(System.out::println);
    }

    //reduce 聚合。
    @Test
    public void test25() throws InterruptedException {
        Flux.just(1, 2, 3, 4, 5)
              .reduce((a, b) -> a + b).subscribe(System.out::println);
    }

    //reduceWithSeed 带初始值。
    @Test
    public void test26() throws InterruptedException {
        Flux.just(1, 2, 3, 4, 5)
              .reduceWith(()->0, Integer::sum)
              .subscribe(result -> System.out.println("Result: " + result));
    }

    //自定义Subscriber。
    @Test
    public void test27() throws InterruptedException {
        Flux<String> furits = Flux.just("Apple", "Orange", "Grape", "Banana", "Strawberry");
        furits.subscribe(new Subscriber<String>() {

            // 保存订阅关系, 需要用它来给发布者响应
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println("订阅者开始订阅");
                this.subscription = subscription;
                // 请求一个数据
                this.subscription.request(1);
            }

            @Override
            public void onNext(String item) {
                System.out.println("订阅者开始处理数据" + item);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 处理完调用request再请求一个数据
                this.subscription.request(1);
                // 或者 已经达到了目标, 调用cancel告诉发布者不再接受数据了
                // this.subscription.cancel();
            }

            @Override
            public void onError(Throwable t) {
                // 出现了异常(例如处理数据的时候产生了异常)
                t.printStackTrace();
                // 我们可以告诉发布者, 后面不接受数据了
                this.subscription.cancel();
            }

            @Override
            public void onComplete() {
                // 全部数据处理完了(发布者关闭了)
                System.out.println("订阅者处理完了!");
            }
        });
    }

    @Test
    public void test28() throws InterruptedException {
        //Flux的doOnNext，会添加当Flux发出一个项目时触发的行为(副作用)。
        //Flux<String> stringFlux = Flux.just("Apple", "Orange", "Grape", "Banana", "Strawberry");
        //stringFlux.doOnNext(t -> System.out.println("发布者处理数据：" + t))
        //        .subscribe(t -> System.out.println("订阅者处理数据：" + t));

        //doOnNext可以写多个，顺序执行：
        Flux<String> stringFlux = Flux.just("Apple", "Orange", "Grape", "Banana", "Strawberry");
        stringFlux.doOnNext(t -> System.out.println("发布者1处理数据：" + t))
                .doOnNext(t -> System.out.println("发布者2处理数据：" + t))
                .subscribe(t -> System.out.println("订阅者处理数据：" + t));

        //但是！以下写法是不会触发发布者的doOnNext事件的：只有链式调用，才会触发发布者的doOnNext事件。
        //Flux<String> stringFlux = Flux.just("Apple", "Orange", "Grape", "Banana", "Strawberry");
        //stringFlux.doOnNext(t -> System.out.println("发布者处理数据：" + t));
        //stringFlux.subscribe(t -> System.out.println("订阅者处理数据：" + t));
    }

    //then操作，在Flux完成后，再发布一个数据。
    @Test
    public void test29() throws InterruptedException {
        Flux<String> stringFlux = Flux.just("Apple", "Orange", "Grape", "Banana", "Strawberry");
        //stringFlux.doOnNext(t -> System.out.println("发布者处理数据：" + t))
        //        .then(Mono.defer(() -> Mono.just("我完成了")))
        //        .subscribe(s -> System.out.println("订阅者处理数据：" + s));

        stringFlux.map(String::toUpperCase)
                .then(Mono.defer(() -> Mono.just("我完成了")))
                .subscribe(System.out::println);
    }

    //当前线程（Schedulers.immediate()）；
    //可重用的单线程（Schedulers.single()）。注意，这个方法对所有调用者都提供同一个线程来使用， 直到该调度器被废弃。如果你想使用独占的线程，请使用Schedulers.newSingle()；
    //弹性线程池（Schedulers.elastic()）。它根据需要创建一个线程池，重用空闲线程。线程池如果空闲时间过长 （默认为 60s）就会被废弃。对于 I/O 阻塞的场景比较适用。Schedulers.elastic()能够方便地给一个阻塞 的任务分配它自己的线程，从而不会妨碍其他任务和资源；
    //固定大小线程池（Schedulers.parallel()），所创建线程池的大小与CPU个数等同；
    //自定义线程池（Schedulers.fromExecutorService(ExecutorService)）基于自定义的ExecutorService创建 Scheduler（虽然不太建议，不过你也可以使用Executor来创建）。
    @Test
    public void test30() {
        Flux.just("tom")
                .map(s -> {
                    System.out.println("[map] Thread name: " + Thread.currentThread().getName());
                    return s.concat("@mail.com");
                })
                //.publishOn(Schedulers.newBoundedElastic(Runtime.getRuntime().availableProcessors(), 1000, "thread-publishOn"))
                .publishOn(Schedulers.newParallel("thread-publishOn"))
                .filter(s -> {
                    System.out.println("[filter] Thread name: " + Thread.currentThread().getName());
                    return s.startsWith("t");
                })
                .doOnNext((t) -> {
                    System.out.println("[doOnNext ] Thread name:" + Thread.currentThread().getName());
                })
                //.subscribeOn(Schedulers.newBoundedElastic(Runtime.getRuntime().availableProcessors(), 1000, "thread-subscribeOn"))
                .subscribeOn(Schedulers.newParallel("thread-subscribeOn"))
                .subscribe(s -> {
                    System.out.println("[subscribe] Thread name: " + Thread.currentThread().getName());
                    System.out.println(s);
                });

        //[map] Thread name: thread-subscribeOn-1
        //[filter] Thread name: thread-publishOn-2
        //[doOnNext ] Thread name:thread-publishOn-2
        //[subscribe] Thread name: thread-publishOn-2
        //tom@mail.com

        //可以看到map操作在subscribeOn设置的Schedulers中运行，从publishOn以后都是在publishOn设置的Schedulers中运行，即使是subscribeOn操作后面的操作。
        // 从上面可知subscribeOn从开头开始影响操作所在的线程，从publishOn操作之后所有的操作都在publishOn设置的Schedulers中运行
    }

    @Test
    public void test31() {
        Flux.just("tom")
            .map(s -> {
                System.out.println("[map] Thread name: " + Thread.currentThread().getName());
                return s.concat("@mail.com");
            })
            .publishOn(Schedulers.newBoundedElastic(Runtime.getRuntime().availableProcessors(), 1000,"thread-publishOn"))
            .filter(s -> {
                System.out.println("[filter] Thread name: " + Thread.currentThread().getName());
                return s.startsWith("t");
            })
            .doOnNext((t) -> {
                System.out.println("[ doOnNext ] Thread name:" + Thread.currentThread().getName());
            })
            .subscribeOn(Schedulers.newBoundedElastic(Runtime.getRuntime().availableProcessors(), 1000,"thread-subscribeOn"))
            .doOnNext((t) -> {
                System.out.println("[ doOnNext1 ] Thread name:" + Thread.currentThread().getName());
            })
            .subscribeOn(Schedulers.newBoundedElastic(Runtime.getRuntime().availableProcessors(), 1000,"thread-000"))
            .doOnNext((t) -> {
                System.out.println("[ doOnNext2 ] Thread name:" + Thread.currentThread().getName());
            })
            .subscribe(s -> {
                System.out.println("[subscribe] Thread name: " + Thread.currentThread().getName());
                System.out.println(s);
            });

        //[map] Thread name: thread-subscribeOn-2
        //[filter] Thread name: thread-publishOn-3
        //[ doOnNext ] Thread name:thread-publishOn-3
        //[ doOnNext1 ] Thread name:thread-publishOn-3
        //[ doOnNext2 ] Thread name:thread-publishOn-3
        //[subscribe] Thread name: thread-publishOn-3
        //tom@mail.com


        //第二个subscribeOn不起作用。只有第一个subscribeOn才有作用。
    }

    @Test
    public void test32() {
        Flux.just("tom")
            .publishOn(Schedulers.newBoundedElastic(Runtime.getRuntime().availableProcessors(), 1000,"thread-publishOn"))
            .doOnNext((t) -> {
                System.out.println("[ doOnNext ] Thread name:" + Thread.currentThread().getName());
            })
            .publishOn(Schedulers.newBoundedElastic(Runtime.getRuntime().availableProcessors(), 1000,"thread-publishOn000"))
            .doOnNext((t) -> {
                System.out.println("[ doOnNext1 ] Thread name:" + Thread.currentThread().getName());
            })
            .subscribe(s -> {
                System.out.println("[subscribe] Thread name: " + Thread.currentThread().getName());
                System.out.println(s);
            });
        //[ doOnNext ] Thread name:thread-publishOn-2
        //[ doOnNext1 ] Thread name:thread-publishOn000-1
        //[subscribe] Thread name: thread-publishOn000-1
        //tom

        //第二个publishOn会影响第一个publishOn。
    }

    //背压策略：
    //onBackpressureBuffer操作，在Flux遇到背压时，会缓存数据，直到缓冲区满，才开始处理数据。参数为缓存大小，单位为元素个数
    //onBackpressureDrop操作，在Flux遇到背压时，会丢弃数据。
    //onBackpressureLatest操作，在Flux遇到背压时，会记住最近的元素，然后开始处理数据。
    //onBackpressureError操作，在Flux遇到背压时，会抛出异常。
    //1.onBackPressureBuffer 操作符会请求无界需求并将返回的元素推送到下游。如果下游消费者无法跟上，那么元素将缓冲在队列中。
    //2.onBackPressureDrop 操作符也请求无界需求(Integer.MAX_VALUE)并向下游推送数据。如果下游请求数量不足，那么元素会被丢弃。自定义处理程序可以用来处理已丢弃的元素。
    //3.onBackPressureLast操作符与 onBackPressureDrop 的工作方式类似。只是会记住最近收到的元素，并在需求出现时立即将其推向下游。
    //4.onBackPressureError 操作符在尝试向下游推送数据时请求无界需求。如果下游消费者无法跟上，则操作符会引发错误管理背压的另一种方法是使用速率限制技术。
    @Test
    public void test33() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Flux.range(1, 1000)
                .delayElements(Duration.ofMillis(1))
                .onBackpressureBuffer(6)
                .delayElements(Duration.ofMillis(100))
                .subscribe(System.out::println,
                        ex -> {
                            System.out.println(ex);
                            latch.countDown();
                        },
                        () -> {
                            System.out.println("complete");
                            latch.countDown();
                        });
        latch.await();
        System.out.println("end");
    }
}
