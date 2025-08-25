package cn.sct.networkmanager.agent.transport.netty;

import cn.sct.networkmanager.agent.config.CPEConfigProperties;
import cn.sct.networkmanager.agent.domain.enums.CWMPClientState;
import cn.sct.networkmanager.agent.domain.enums.CWMPEventCode;
import cn.sct.networkmanager.agent.domain.enums.CWMPServeState;
import cn.sct.networkmanager.agent.protocol.cwmp.DefaultCwmpSession;
import cn.sct.networkmanager.agent.protocol.cwmp.http.HttpMessageCreateFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class CWMPNettyServe {
    private final CPEConfigProperties config;
    private static final Logger log = Loggers.getLogger(CWMPNettyServe.class);
    private final AtomicInteger connectedCount = new AtomicInteger(0);
    private final DefaultCwmpSession session;
    @Getter
    private  CWMPServeState state = CWMPServeState.INITIAL;
    private final HttpMessageCreateFactory httpMessageCreateFactory;
    private final String receiveUrl ;
    private final ApplicationEventPublisher eventPublisher;

    public CWMPNettyServe(CPEConfigProperties config, DefaultCwmpSession session, HttpMessageCreateFactory httpMessageCreateFactory, String receiveUrl, ApplicationEventPublisher eventPublisher) {
        this.config = config;
        this.session = session;
        this.httpMessageCreateFactory=httpMessageCreateFactory;
        this.receiveUrl = receiveUrl;
        this.eventPublisher = eventPublisher;
    }
    public void start()  {
        if (state != CWMPServeState.started){
            // 创建主从线程组
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup(1);
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel channel) throws Exception {
                                ChannelPipeline pipeline = channel.pipeline();
                                pipeline.addLast(new HttpServerCodec());
                                pipeline.addLast(new HttpObjectAggregator(1024 * 1024));
                                pipeline.addLast(new SimpleChannelInboundHandler<FullHttpRequest>(){
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) throws Exception {
                                        if (connectedCount.get() > config.getConnectionLimit()){//请求繁忙，限流
                                            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.SERVICE_UNAVAILABLE);
                                            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
                                            channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                                            return;
                                        }
                                        if (request.method() != HttpMethod.GET || !request.uri().equals(receiveUrl)) {
                                            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
                                            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
                                            channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                                            return;
                                        }
                                        //判断请求是否建立，建立连接请求
                                        if (!session.isAlive()){
                                            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NO_CONTENT);
                                            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
                                            channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                                            session.connect(CWMPEventCode.CONNECTION_REQUEST,"")
                                                    .flatMap(aboolean -> {
                                                        if (aboolean){
                                                            return session.send(httpMessageCreateFactory.createNull(),
                                                                    new AtomicInteger(0));
                                                        }
                                                        return Mono.empty();
                                                    }).subscribe();
                                            return;
                                        }
                                        //会话进行中，采用retry-after的方式，等待acs端重发请求
                                        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.SERVICE_UNAVAILABLE);
                                        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
                                        response.headers().set("Retry-After",60);
                                        channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                                    }
                                });
                            }
                        });
                // 绑定端口并启动服务器
                ChannelFuture future = bootstrap.bind(config.getHost(), config.getPort()).sync();
                state=CWMPServeState.started;
                log.info("HTTP server started on host:port {}:{} " ,config.getHost(), config.getPort());
                Disposable tokenBucket = Flux.interval(Duration.ofSeconds(1), Duration.ofSeconds(1))
                        .doOnNext(ignore -> connectedCount.compareAndSet(0, 1))
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe();
                // 等待服务器通道关闭
                future.channel().closeFuture().addListener(f -> {
                    log.warn("HTTP server stopped");
                    state=CWMPServeState.CLOSED;
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                    tokenBucket.dispose();
                    eventPublisher.publishEvent(new CWMPServeStateEvent(this, CWMPServeState.CLOSED));
                });
            } catch (InterruptedException e){
                log.error("HTTP server start error:{}",e.getMessage());
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                state=CWMPServeState.INITIAL;
            }
        }
    }


    @Getter
    public static class CWMPServeStateEvent extends ApplicationEvent {
        private final CWMPServeState cwmpServeState;
        public CWMPServeStateEvent(CWMPNettyServe serve, CWMPServeState cwmpServeState) {
            super(serve);
            this.cwmpServeState = cwmpServeState;
        }
    }
}
