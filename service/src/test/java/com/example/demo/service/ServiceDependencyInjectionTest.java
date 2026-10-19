package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(ServiceDependencyInjectionTest.FixtureConfiguration.class)
class ServiceDependencyInjectionTest {

    @Autowired
    private GreetingService greetingService;

    @Test
    void injectsServiceAndItsConstructorDependency() {
        assertEquals("Hello, Spring", greetingService.greet("Spring"));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FixtureConfiguration {

        @Bean
        GreetingPrefix greetingPrefix() {
            return new GreetingPrefix("Hello, ");
        }

        @Bean
        GreetingService greetingService(GreetingPrefix prefix) {
            return new GreetingService(prefix);
        }
    }

    record GreetingPrefix(String value) {
    }

    record GreetingService(GreetingPrefix prefix) {

        String greet(String name) {
            return prefix.value() + name;
        }
    }
}
