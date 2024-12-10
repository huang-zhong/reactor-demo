package com.gwsd.reactor;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MonoTest1 {

    @Test
    public void test1(){
        //Mono.empty().subscribe(System.out::println);
        //Mono.just("Hello, World!").subscribe(System.out::println);
        //Mono.justOrEmpty("Hello, World!").subscribe(System.out::println);
        //Mono.fromSupplier(()->"Hello, World!").subscribe(System.out::println);

        //Publisher<String> publisher = Mono.fromSupplier(()->"fromSupplier!");
        //Mono.from(publisher).subscribe(System.out::println);

        //Mono.firstWithValue(Mono.just("Hello, World!"), Mono.just("fromSupplier!")).subscribe(System.out::println);

    }
    @Test
    public void test2(){
        Mono.defer(() -> Mono.just("Hello, World!")).subscribe(System.out::println);
    }

    @Test
    public void test3(){
        //回调方法，会等线程返回结果才会执行下一步
        Mono.fromCallable(() -> {
            //return Mono.just("Callable");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Hello, World!");
            return "Callable";
        }).subscribe(System.out::println);
    }

    @Test
    public void test4(){
        //会等线程返回结果才会执行下一步
        Mono.fromRunnable(()->{
            //return Mono.just("Thread");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Hello, World!");
        }).subscribe(System.out::println);
    }

    @Test
    public void test5() throws InterruptedException {
        // 不会等线程返回结果才会执行下一步，而是直接执行下一步
        // 怎么拿到返回结果呢？
        Mono.fromFuture( CompletableFuture.supplyAsync(() -> {
            //return Mono.just("Future");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Hello, World!");
            return "Future";
        })).subscribe(System.out::println);

        Thread.sleep(3000);
    }

    @Test
    public void test6() throws InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000); // 模拟长时间的异步处理
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Hello, World!");
            return "Future"; // 返回的结果
        });

        // 将 CompletableFuture 转换为 Mono，并进行订阅以获取返回值
        Mono.fromFuture(future).subscribe(
                result -> System.out.println("Received: " + result), // 处理返回值
                error -> System.err.println("Error: " + error), // 处理错误
                () -> System.out.println("Completed") // 完成时的处理
        );

        Thread.sleep(3000);
    }

    @Test
    public void test7() {
       Mono.fromDirect(Mono.just("Hello, World!")).subscribe(System.out::println);
    }

    @Test
    public void test8() {
        Mono.fromCompletionStage(CompletableFuture.completedFuture("Hello, World!"))
                .subscribe(System.out::println);
    }

}
