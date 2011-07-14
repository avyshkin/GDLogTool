package com.griddynamics.logtool;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.net.InetSocketAddress;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Consumer {
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    private static final short DEF_PORT = 4444;


    public static void main(String[] args) {
        short port = DEF_PORT;
        if(args.length == 1) {
            try {
                port = Short.parseShort(args[1]);
            } catch (NumberFormatException e) {
                logger.error(e.getMessage(), e);
            }
        }
        Executor threadPool = Executors.newCachedThreadPool();
        ChannelFactory factory = new NioServerSocketChannelFactory(threadPool, threadPool);
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        BeanFactory springFactory = new ClassPathXmlApplicationContext("fileStorageConfiguration.xml");
        final FileStorage fileStorage = (FileStorage) springFactory.getBean("fileStorage");
        final ConsumerHandler consumerHandler = new ConsumerHandler(fileStorage);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(
                        new LogEventDecoder(),
                        consumerHandler);
            }
        });

        bootstrap.setOption("child.keepAlive", true);
        bootstrap.bind(new InetSocketAddress(port));
    }
}
