package world.soapboxrace.mp.server.netty.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class BaseHandler extends ChannelInboundHandlerAdapter
{
    final Logger logger;
    
    {
        logger = LoggerFactory.getLogger(getClass().getSimpleName());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        super.exceptionCaught(ctx, cause);

        logger.error("Error occurred", cause);
    }
}
