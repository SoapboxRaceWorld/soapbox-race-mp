package world.soapboxrace.mp.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import world.soapboxrace.mp.server.netty.handlers.*;

public class ServerChannelInitializer extends ChannelInitializer<DatagramChannel>
{
    @Override
    protected void initChannel(DatagramChannel ch) throws Exception
    {
        ChannelPipeline pipeline = ch.pipeline();
        
        pipeline.addLast("hello", new HelloHandler());
        pipeline.addLast("helloSync", new SyncHelloHandler());
        pipeline.addLast("preInfo", new PreInfoHandler());
        pipeline.addLast("syncKeepAlive", new KeepAliveHandler());
        pipeline.addLast("sync", new SyncHandler());
    }
}