package com.ooooo.demo.config;

import cn.hutool.core.thread.ThreadUtil;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="https://github.com/ooooo-youwillsee">ooooo</a>
 * @since 1.0.0
 */
@Configuration
public class FanoutTestConfiguration {

    private static final String QUEUE_NAME1 = "test-queue1";

    private static final String QUEUE_NAME2 = "test-queue2";

    private static final String EXCHANGE_NAME = "test-exchange";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Bean
    public ApplicationRunner init() {
        declareMQConfig();
        return __ -> {
            new Thread(() -> {
                for (int i = 0; i < 100; i++) {
                    rabbitTemplate.convertAndSend(EXCHANGE_NAME, "", i);
                    ThreadUtil.sleep(1, TimeUnit.SECONDS);
                }
            }).start();
        };
    }

    private void declareMQConfig() {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(rabbitTemplate);
        rabbitAdmin.declareExchange(new FanoutExchange(EXCHANGE_NAME));
        rabbitAdmin.declareQueue(new Queue(QUEUE_NAME1));
        rabbitAdmin.declareBinding(new Binding(QUEUE_NAME1, Binding.DestinationType.QUEUE, EXCHANGE_NAME, "", null));

        rabbitAdmin.declareQueue(new Queue(QUEUE_NAME2));
        rabbitAdmin.declareBinding(new Binding(QUEUE_NAME2, Binding.DestinationType.QUEUE, EXCHANGE_NAME, "", null));
    }

    @RabbitListener(queues = QUEUE_NAME1)
    public void onMessage1(String message) {
        System.out.println(QUEUE_NAME1 + " " + message);
    }

    @RabbitListener(queues = QUEUE_NAME2)
    public void onMessage2(String message) {
        System.out.println(QUEUE_NAME2 + " " + message);
    }

}
