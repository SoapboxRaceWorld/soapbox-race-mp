package br.com.sbrw.mp.srv;

import io.netty.channel.ChannelFuture;

public class Main
{
    public static void main(String[] args)
    {
        int port = 9998;
        if (args.length == 1)
        {
            port = Integer.parseInt(args[0]);
        }
        NettyUdpServer server;

        try
        {
            server = new NettyUdpServer(port);
            ChannelFuture future = server.start();

            // Wait until the connection is closed.
            future.channel().closeFuture().sync();
        } catch (InterruptedException ex)
        {
            System.err.println(ex.getMessage());
        }
    }
}
