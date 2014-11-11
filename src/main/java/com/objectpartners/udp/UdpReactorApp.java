package com.objectpartners.udp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import reactor.core.Environment;
import reactor.io.encoding.StandardCodecs;
import reactor.net.netty.udp.NettyDatagramServer;
import reactor.net.tcp.support.SocketUtils;
import reactor.net.udp.DatagramServer;
import reactor.net.udp.spec.DatagramServerSpec;
import reactor.spring.context.config.EnableReactor;

import java.util.concurrent.CountDownLatch;

@Configuration
@EnableAutoConfiguration
@EnableReactor
@ComponentScan
public class UdpReactorApp {

    private Log log = LogFactory.getLog(UdpReactorApp.class);

    @Bean
    public DatagramServer<byte[], byte[]> datagramServer(Environment env) throws InterruptedException {

        final DatagramServer<byte[], byte[]> server = new DatagramServerSpec<byte[], byte[]>(NettyDatagramServer.class)
                .env(env)
                .listen(SocketUtils.findAvailableTcpPort())
                .codec(StandardCodecs.BYTE_ARRAY_CODEC)
                .consumeInput(bytes -> log.info("received: " + new String(bytes)))
                .get();

        server.start().await();
        return server;
    }

    @Bean
    public CountDownLatch latch() {
        return new CountDownLatch(1);
    }

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = SpringApplication.run(UdpReactorApp.class);
        CountDownLatch latch = ctx.getBean(CountDownLatch.class);
        latch.await();
    }
}
