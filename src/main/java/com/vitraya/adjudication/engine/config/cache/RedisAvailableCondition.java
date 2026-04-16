/*
package com.vitraya.adjudication.engine.config.cache;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RedisAvailableCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String host = context.getEnvironment().getProperty("spring.data.redis.host", "localhost");
        int port = Integer.parseInt(context.getEnvironment().getProperty("spring.data.redis.port", "6379"));

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}*/
