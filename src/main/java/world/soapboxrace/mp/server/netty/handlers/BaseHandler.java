package world.soapboxrace.mp.server.netty.handlers;

import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class BaseHandler extends ChannelInboundHandlerAdapter
{
    protected final Logger logger;
    
    {
        logger = LoggerFactory.getLogger(getClass().getSimpleName());
    }
}
