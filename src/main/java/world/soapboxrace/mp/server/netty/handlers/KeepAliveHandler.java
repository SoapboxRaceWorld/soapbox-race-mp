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

        Racer racer = RacerManager.get(packet.sender().getPort());

        if (racer == null)
        {
            logger.error("Racer is null!");
            return;
        }

        if (isKeepAlive(data))
        {
            logger.debug("Got keep-alive packet");

            ClientKeepAlive clientKeepAlive = new ClientKeepAlive();
            clientKeepAlive.read(buf);

            ServerKeepAlive response = new ServerKeepAlive();
            response.counter = racer.getSequenceC();
            response.helloTime = racer.getCliHelloTime();
            response.time = (short) racer.getTimeDiff();
            response.unknownCounter = clientKeepAlive.unknownCounter;

            ByteBuffer buffer = ByteBuffer.allocate(17);
            response.write(buffer);
            
            racer.send(buffer);
            
            logger.debug("Sent keep-alive response");
        }

        super.channelRead(ctx, msg);
    }

    private boolean isKeepAlive(byte[] data)
    {
        return data.length == 18
                && data[0] == 0x00
                && data[3] == 0x07
                && data[4] == 0x02;
    }
}
