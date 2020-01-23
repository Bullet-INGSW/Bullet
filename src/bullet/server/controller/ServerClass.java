package bullet.server.controller;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;



//utilizzo SSL come tramite di connessione
public class ServerClass {

    static final boolean SSL=System.getProperty("ssl")!=null;
    static final int port= Integer.parseInt(System.getProperty("port","8998"));

    public static void main(String[] args) throws Exception {
        final SslContext sslCtx;
        if(SSL){ //se il sistema è ssl
            SelfSignedCertificate ssc=new SelfSignedCertificate(); //creo il certificato
            sslCtx= SslContextBuilder.forServer(ssc.certificate(),ssc.privateKey()).build();

        }

        else{
            sslCtx=null;
            //se non è SSL non creo la certificazione
        }

        EventLoopGroup bossGroup =new NioEventLoopGroup(1);
        EventLoopGroup workGroup= new NioEventLoopGroup();
        final GestoreServer serverHandler=new GestoreServer();


        try{
        ServerBootstrap bootstrap=new ServerBootstrap();
        bootstrap.group(bossGroup,workGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,100)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {

                        ChannelPipeline p=socketChannel.pipeline();
                        if(sslCtx!=null){
                            p.addLast(sslCtx.newHandler(socketChannel.alloc()));

                        }
                        p.addLast(serverHandler);
                    }
                });

        //dovrebbe startare il server
        ChannelFuture f=bootstrap.bind(port).sync();


        //aspetta che il socket sia chiuso
        f.channel().closeFuture().sync();
        }

        finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }
}


/*
GROTTA TODOLIST:
1)Studiarmi a che servono e i metodi delle EventLoopGroup
1.b)che è il serverhandler?
1.c)bootstrap?
1.d) channelFuture
2)Chidere agli altri come vogliono gestire le istanze (chiave univoca a ogni istanza?)







 */