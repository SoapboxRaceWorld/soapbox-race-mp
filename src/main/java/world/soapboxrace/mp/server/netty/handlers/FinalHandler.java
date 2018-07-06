package world.soapboxrace.mp.server.netty.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

/**
 * One purpose: Free memory.
 */
public class FinalHandler extends BaseHandler
{
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        ((DatagramPacket) msg).content().release();
    }
}
