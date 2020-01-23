package bullet.client.logic;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;

public class ConnessioneClient {
    static final boolean SSL=System.getProperty("ssl")!=null;
    static final String HOST= System.getProperty("host","127.0.0.1");
    static  final int PORT= Integer.parseInt(System.getProperty("port","8998"));
    static final int SIZE= Integer.parseInt(System.getProperty("size","256"));


    public static void main(String[] args) throws SSLException, InterruptedException {
        final SslContext sslCtx;

        if(SSL){

            sslCtx= SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        }

        else{
            sslCtx=null;
        }
        //inizializza i certificati

        EventLoopGroup group=new NioEventLoopGroup();
        try{
            Bootstrap b=new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p=socketChannel.pipeline();
                            if(sslCtx!=null){
                                p.addLast(sslCtx.newHandler(socketChannel.alloc(),HOST,PORT));
                            }
                            p.addLast((ChannelHandler) new GestoreClient());
                        }
                    });

            ChannelFuture f=b.connect(HOST,PORT).sync();

            f.channel().closeFuture().sync();


        }

        finally{
            group.shutdownGracefully();
        }



    }






}
