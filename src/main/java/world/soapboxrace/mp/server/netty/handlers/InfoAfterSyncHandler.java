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

public class InfoAfterSyncHandler extends BaseHandler
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

        if (isInfoAfterSync(data))
        {
            logger.debug("Got info after sync");

            if (session.allPlayersOK())
            {
                logger.debug("doSessionBroadcast");
                doSessionBroadcast(session, racer, data);
            }
        } else
        {
            super.channelRead(ctx, msg);
        }
    }

    private boolean isInfoAfterSync(byte[] data)
    {
        return data[0] == 0x01
                && data.length >= 16;
    }
    
    private void doSessionBroadcast(RaceSession session, Racer racer, byte[] data)
    {
        for (Racer sessionRacer : session.getRacers())
        {
            if (sessionRacer.getClientIndex() == racer.getClientIndex()) continue;

            sessionRacer.send(transformPacket(data, sessionRacer, racer));
        }
    }

    private ByteBuffer transformPacket(byte[] packet, Racer toRacer, Racer fromRacer)
    {
        if (packet.length < 4)
            return ByteBuffer.allocate(0);
        byte[] seqBytes = ByteBuffer.allocate(2).putShort(toRacer.getSequenceB()).array();
        byte[] timeArray = ByteBuffer.allocate(2).putShort((short) toRacer.getTimeDiff()).array();
        ByteBuffer buffer = ByteBuffer.allocate(packet.length - 3);

        buffer.put((byte) 0x01);
        buffer.put(fromRacer.getClientIndex());
        buffer.put(seqBytes);
//        buffer.put(new byte[] { 0x00, 0x00 });

        for (int i = 6; i < (packet.length - 1); i++)
        {
            if (packet[i] == 0x12 && packet[i + 1] >= 0x1a)
            {
                packet[i + 2] = timeArray[0];
                packet[i + 3] = timeArray[1];
            }
        }

        for (int i = 6; i < (packet.length - 1); i++)
        {
            buffer.put(packet[i]);
        }

        return buffer;
    }
}
