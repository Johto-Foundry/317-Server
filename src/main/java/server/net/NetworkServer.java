package server.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.TimeUnit;

public final class NetworkServer {

    public static final int DEFAULT_PORT = 43594;

    private final int port;

    private EventLoopGroup eventLoopGroup;
    private ChannelGroup channels;
    private Channel serverChannel;

    public NetworkServer() {
        this(DEFAULT_PORT);
    }

    public NetworkServer(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port out of range: " + port);
        }
        this.port = port;
    }

    public synchronized void start() throws InterruptedException {
        if (isRunning()) {
            return;
        }

        EventLoopGroup group = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        ChannelGroup activeChannels = new DefaultChannelGroup(group.next());

        try {
            Channel boundChannel = new ServerBootstrap()
                    .group(group)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {
                            channel.attr(Session.KEY).set(new Session(channel));
                            activeChannels.add(channel);
                        }
                    })
                    .bind(port)
                    .sync()
                    .channel();

            activeChannels.add(boundChannel);
            eventLoopGroup = group;
            channels = activeChannels;
            serverChannel = boundChannel;
        } catch (InterruptedException | RuntimeException exception) {
            activeChannels.close().syncUninterruptibly();
            group.shutdownGracefully(0, 5, TimeUnit.SECONDS).syncUninterruptibly();
            throw exception;
        }
    }

    public synchronized void stop() {
        ChannelGroup activeChannels = channels;
        EventLoopGroup group = eventLoopGroup;

        channels = null;
        eventLoopGroup = null;
        serverChannel = null;

        if (activeChannels != null) {
            activeChannels.close().syncUninterruptibly();
        }
        if (group != null) {
            group.shutdownGracefully(0, 5, TimeUnit.SECONDS).syncUninterruptibly();
        }
    }

    public synchronized boolean isRunning() {
        return serverChannel != null && serverChannel.isActive();
    }
}
