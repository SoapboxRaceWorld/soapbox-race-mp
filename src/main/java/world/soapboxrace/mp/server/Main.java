package world.soapboxrace.mp.server;

import io.netty.channel.ChannelFuture;
import org.slf4j.LoggerFactory;
import world.soapboxrace.mp.server.netty.NettyServer;

/**
 * The entry point.
 */
public class Main
{
    public static void main(String[] args)
    {
        NettyServer server;

        try
        {
            server = new NettyServer(9998);
            ChannelFuture future = server.start();

            LoggerFactory.getLogger("Race").info("Started UDP server!");

            // Wait until the connection is closed.
            future.channel().closeFuture().sync();
        } catch (InterruptedException ex)
        {
            System.err.println(ex.getMessage());
        }
    }
}
