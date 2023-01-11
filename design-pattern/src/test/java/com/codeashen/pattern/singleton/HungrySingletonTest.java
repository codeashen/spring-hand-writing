package com.codeashen.pattern.singleton;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HungrySingletonTest {

    @Test
    void getInstance() throws Exception {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        List<Future<HungrySingleton>> resultList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            resultList.add(threadPool.submit(HungrySingleton::getInstance));
        }
        HungrySingleton instance = null;
        for (Future<HungrySingleton> future : resultList) {
            HungrySingleton hungrySingleton = future.get();
            if (instance == null) {
                instance = hungrySingleton;
            }
            assertEquals(instance, hungrySingleton);
        }
    }
}