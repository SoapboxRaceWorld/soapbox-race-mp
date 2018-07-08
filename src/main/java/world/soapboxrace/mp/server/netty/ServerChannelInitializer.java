package world.soapboxrace.mp.server.netty;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import world.soapboxrace.mp.server.netty.handlers.*;
import world.soapboxrace.mp.util.ServerLog;

public class ServerChannelInitializer extends ChannelInitializer<DatagramChannel>
{
    @Override
    protected void initChannel(DatagramChannel ch)
    {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addFirst(new ChannelInboundHandlerAdapter()
        {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
            {
                DatagramPacket packet = (DatagramPacket) msg;

                ServerLog.SERVER_LOGGER.debug("Received packet:");
                System.out.println(ByteBufUtil.prettyHexDump(packet.content()));
                
                super.channelRead(ctx, msg);
            }
        });
        
        // Messages
        pipeline.addLast("hello", new HelloHandler());
        pipeline.addLast("syncStart", new SyncStartHandler());
        pipeline.addLast("infoBeforeSync", new InfoBeforeSyncHandler());
        pipeline.addLast("sync", new SyncHandler());
        pipeline.addLast("keepAlive", new KeepAliveHandler());
        pipeline.addLast("infoAfterSync", new InfoAfterSyncHandler());

        pipeline.addLast(new ChannelInboundHandlerAdapter()
        {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
            {
                DatagramPacket packet = (DatagramPacket) msg;

                packet.release();
            }
        });

//        pipeline.addLast("hello", new HelloHandler());
//        pipeline.addLast("helloSync", new SyncHelloHandler());
//        pipeline.addLast("preInfo", new PreInfoHandler());
//        pipeline.addLast("syncKeepAlive", new KeepAliveHandler());
//        pipeline.addLast("sync", new SyncHandler());
//        pipeline.addLast("postInfo", new PostInfoHandler());
    }
}