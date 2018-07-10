package world.soapboxrace.mp.server.netty.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import world.soapboxrace.mp.race.RaceSession;
import world.soapboxrace.mp.race.RaceSessionManager;
import world.soapboxrace.mp.race.Racer;
import world.soapboxrace.mp.race.RacerManager;
import world.soapboxrace.mp.server.netty.messages.ClientKeepAlive;
import world.soapboxrace.mp.server.netty.messages.ServerKeepAlive;

import java.nio.ByteBuffer;

public class KeepAliveHandler extends BaseHandler
{
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        DatagramPacket packet = (DatagramPacket) msg;
        ByteBuf buf = packet.content();
        byte[] data = ByteBufUtil.getBytes(buf);

        int port = packet.sender().getPort();
        Racer racer = RacerManager.get(port);

        if (racer == null)
        {
            logger.error("Racer not found!");
            return;
        }

        RaceSession session = RaceSessionManager.get(racer);

        if (isKeepAlive(data))
        {
            ClientKeepAlive clientKeepAlive = new ClientKeepAlive();
            clientKeepAlive.read(buf);

            ServerKeepAlive serverKeepAlive = new ServerKeepAlive();
            serverKeepAlive.unknownCounter = clientKeepAlive.unknownCounter;
//            serverKeepAlive.time = clientKeepAlive.time;
//            serverKeepAlive.helloTime = clientKeepAlive.cliHelloTime;
            serverKeepAlive.time = (int) racer.getTimeDiff();
            serverKeepAlive.helloTime = racer.getCliHelloTime();
            serverKeepAlive.counter = racer.getSyncSequence();

            ByteBuffer buffer = ByteBuffer.allocate(17);
            serverKeepAlive.write(buffer);
            
            racer.send(buffer);
            
            logger.debug("Sent keep-alive response");
        } else
        {
            super.channelRead(ctx, msg);
        }
    }

    private boolean isKeepAlive(byte[] data)
    {
        return data.length == 18
                && data[0] == 0x00
                && data[3] == 0x07
                && data[4] == 0x02;
    }
}
