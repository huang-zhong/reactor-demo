package com.gwsd.reactor;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public class MonoTest1 {

    @Test
    public void test1(){
        Mono.empty().subscribe(System.out::println);
        Mono.just("Hello, World!").subscribe(System.out::println);
        Mono.justOrEmpty("Hello, World!").subscribe(System.out::println);
    }
}
