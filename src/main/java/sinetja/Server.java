package sinetja;

import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.router.Router;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.io.File;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

public class Server extends Router<Action> {
    private static final int MAX_CONTENT_LENGTH = 1024 * 1024;

    //----------------------------------------------------------------------------
    // Config

    private SslContext sslContext = null;

    private int maxContentLength = MAX_CONTENT_LENGTH;

    /**
     * Default: UTF-8.
     */
    private Charset charset = CharsetUtil.UTF_8;

    private CorsConfig cors;

    private Instantiator instantiator = new Instantiator();

    private Object before;

    private Object after;

    private Object error = new DefaultErrorHandler();

    //----------------------------------------------------------------------------

    public Server() {
        notFound(new DefaultNotFoundHandler());
    }

    public SslContext sslContext() {
        return sslContext;
    }

    public Server sslContext(SslContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    /**
     * Uses autogenerated selfsigned certificate.
     */
    public Server jdkSsl() throws SSLException, CertificateException {
        SelfSignedCertificate cert = new SelfSignedCertificate();
        sslContext = SslContextBuilder.forServer(cert.certificate(), cert.privateKey())
                .sslProvider(SslProvider.JDK)
                .build();
        return this;
    }

    public Server jdkSsl(String certChainFile, String keyFile) throws SSLException {
        sslContext = SslContextBuilder.forServer(new File(certChainFile), new File(keyFile))
                .sslProvider(SslProvider.JDK)
                .build();
        return this;
    }

    public Server jdkSsl(String certChainFile, String keyFile, String keyPassword) throws SSLException {
        sslContext = SslContextBuilder.forServer(new File(certChainFile), new File(keyFile), keyPassword)
                .sslProvider(SslProvider.JDK)
                .build();
        return this;
    }

    /**
     * Uses autogenerated selfsigned certificate.
     */
    public Server openSsl() throws SSLException, CertificateException {
        SelfSignedCertificate cert = new SelfSignedCertificate();
        sslContext = SslContextBuilder.forServer(cert.certificate(), cert.privateKey())
                .sslProvider(SslProvider.OPENSSL)
                .build();
        return this;
    }

    public Server openSsl(String certChainFile, String keyFile) throws SSLException {
        sslContext = SslContextBuilder.forServer(new File(certChainFile), new File(keyFile))
                .sslProvider(SslProvider.OPENSSL)
                .build();
        return this;
    }

    public Server openSsl(String certChainFile, String keyFile, String keyPassword) throws SSLException {
        sslContext = SslContextBuilder.forServer(new File(certChainFile), new File(keyFile), keyPassword)
                .sslProvider(SslProvider.OPENSSL)
                .build();
        return this;
    }

    //----------------------------------------------------------------------------

    public int maxContentLength() {
        return maxContentLength;
    }

    /**
     * Default max content length in request body is 1 MB.
     */
    public Server maxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
        return this;
    }

    public Charset charset() {
        return charset;
    }

    public Server charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public CorsConfig cors() {
        return cors;
    }

    public Server cors(CorsConfig cors) {
        this.cors = cors;
        return this;
    }

    //----------------------------------------------------------------------------

    public Instantiator instantiator() {
        return instantiator;
    }

    public Server instantiator(Instantiator instantiator) {
        this.instantiator = instantiator;
        return this;
    }

    public Object before() {
        return before;
    }

    public Server before(Action before) {
        this.before = before;
        return this;
    }

    public Server before(Class<? extends Action> before) {
        this.before = before;
        return this;
    }

    public Object after() {
        return after;
    }

    public Server after(Action after) {
        this.after = after;
        return this;
    }

    public Server after(Class<? extends Action> after) {
        this.after = after;
        return this;
    }

    public Object error() {
        return error;
    }

    public Server error(ErrorHandler error) {
        this.error = error;
        return this;
    }

    public Server error(Class<? extends ErrorHandler> error) {
        this.error = error;
        return this;
    }

    //----------------------------------------------------------------------------

    private final PipelineInitializer pipelineInitializer = new PipelineInitializer(this);

    public void start(int port) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .childOption(ChannelOption.TCP_NODELAY, java.lang.Boolean.TRUE)
                    .childOption(ChannelOption.SO_KEEPALIVE, java.lang.Boolean.TRUE)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(pipelineInitializer);

            // Bind on all network interfaces
            Channel ch = b.bind(port).sync().channel();

            if (sslContext == null)
                Log.info("HTTP server started: http://127.0.0.1:" + port + '/');
            else
                Log.info("HTTPS server started: https://127.0.0.1:" + port + '/');

            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
