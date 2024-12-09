package com.gwsd.reactor;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
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
}
