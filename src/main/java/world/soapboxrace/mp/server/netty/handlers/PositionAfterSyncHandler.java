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

public class PositionAfterSyncHandler extends BaseHandler
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

        if (isPosAfterSync(data))
        {
            logger.debug("got pos after sync");
            racer.parsePacket(data);

            session.getRacers().stream()
                    .filter(r -> r.getClientIndex() != racer.getClientIndex())
                    .forEach(r -> r.send(transformPosPacket(data, racer, r)));
        } else
        {
            super.channelRead(ctx, msg);
        }
    }

    private boolean isPosAfterSync(byte[] data)
    {
        return data.length >= 16
                && data[0] == 0x01
                && data[10] == 0x12
                && data[11] >= 0x1a;
    }

    private ByteBuffer transformPosPacket(byte[] packet, Racer origRacer, Racer targetRacer)
    {
        byte[] sequenceBytes = ByteBuffer.allocate(2).putShort(targetRacer.getSequenceB()).array();
        byte[] timeArray = ByteBuffer.allocate(2).putShort((short) targetRacer.getTimeDiff()).array();

        ByteBuffer buffer = ByteBuffer.allocate(packet.length - 3);
        buffer.put((byte) 0x01);
        buffer.put(origRacer.getClientIndex());
        buffer.put(sequenceBytes);

        for (int i = 6; i < (packet.length - 1); i++)
        {
            if (packet[i] == 0x12 && packet[i + 1] >= 0x1a)
            {
                packet[i + 2] = timeArray[0];
                packet[i + 3] = timeArray[1];
            }
        }

        for (int i = 6; i < packet.length - 1; i++)
        {
            buffer.put(packet[i]);
        }

        return buffer;
    }
}
