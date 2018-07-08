package world.soapboxrace.mp.server.netty.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import world.soapboxrace.mp.race.RaceSession;
import world.soapboxrace.mp.race.RaceSessionManager;
import world.soapboxrace.mp.race.Racer;
import world.soapboxrace.mp.race.RacerManager;

import java.nio.ByteBuffer;

public class InfoBeforeSyncHandler extends BaseHandler
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

        RaceSession session = RaceSessionManager.get(racer);

        if (isInfoBeforeSync(data))
        {
            logger.debug("Got info before sync");
            racer.parsePacket(data);

            if (session.allPlayersOK())
            {
                logger.debug("All players marked as OK");

                session.getRacers().forEach(this::broadcastInfoFrom);
            }
        }

        super.channelRead(ctx, msg);
    }

    private void broadcastInfoFrom(Racer racer)
    {
        RaceSession session = RaceSessionManager.get(racer);

        if (session.allPlayersOK())
        {
            for (Racer sessionRacer : session.getRacers())
            {
                if (sessionRacer.getClientIndex() == racer.getClientIndex()) continue;

                sessionRacer.send(transformPacket(racer.getPlayerPacket(), racer));
                logger.debug("{} -> {}", racer.getClientIndex(), sessionRacer.getClientIndex());
            }
        }
    }

    private boolean isInfoBeforeSync(byte[] data)
    {
        return data[0] == 0x01
                && data[6] == (byte) 0xff
                && data[7] == (byte) 0xff
                && data[8] == (byte) 0xff
                && data[9] == (byte) 0xff;
    }

    private ByteBuffer transformPacket(byte[] data, Racer racer)
    {
        if (data.length < 4)
            return null;

        ByteBuffer buffer = ByteBuffer.allocate(data.length - 3);
        buffer.put((byte) 0x01);
        buffer.put(racer.getClientIndex());

        buffer.put(ByteBuffer.allocate(2).putShort(racer.getSequenceA()).array());

        for (int i = 6; i < data.length - 1; i++)
        {
            buffer.put(data[i]);
        }

        return buffer;
    }
}
