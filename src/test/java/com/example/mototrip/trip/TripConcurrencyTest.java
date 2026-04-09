package com.example.mototrip.trip;

import com.example.mototrip.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TripConcurrencyTest {

    @Autowired
    private TripService tripService;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        tripRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateSeveralUsersInParallel() throws Exception {
        int numberOfUsers = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfUsers);
        CountDownLatch readyLatch = new CountDownLatch(numberOfUsers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfUsers);
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();

        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < numberOfUsers; i++) {
            int index = i;
            tasks.add(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    tripService.createUser("ParallelUser" + index, false);
                }
                catch (Throwable error) {
                    errors.add(error);
                }
                finally {
                    doneLatch.countDown();
                }
            });
        }

        for (Runnable task : tasks) {
            executorService.submit(task);
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(errors).isEmpty();
        assertThat(userRepository.count()).isEqualTo(numberOfUsers);
    }
}
