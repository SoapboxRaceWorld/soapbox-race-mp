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

public class IdAfterSyncHandler extends BaseHandler
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

        if (isIdAfterSync(data))
        {
            logger.debug("got ID after sync");
            racer.parsePacket(data);

            session.getRacers().stream()
                    .filter(r -> r.getClientIndex() != racer.getClientIndex())
                    .forEach(r -> r.send(transformIdPacket(data, racer, r)));

            if (racer.isParserOK())
            {
                session.getRacers().stream()
                        .filter(r -> r.getClientIndex() != racer.getClientIndex())
                        .forEach(r -> r.send(transformIdPacket(racer.getPlayerPacket(), racer, r)));
            }
        } else
        {
            super.channelRead(ctx, msg);
        }
    }

    private boolean isIdAfterSync(byte[] data)
    {
        return data.length >= 16
                && data[0] == 0x01
                && data[9] == (byte) 0xff
                && data[10] == 0x02;
    }

    private ByteBuffer transformIdPacket(byte[] packet, Racer origRacer, Racer targetRacer)
    {
        byte[] sequenceBytes = ByteBuffer.allocate(2).putShort(targetRacer.getSequenceB()).array();

        ByteBuffer buffer = ByteBuffer.allocate(packet.length - 3);
        buffer.put((byte) 0x01);
        buffer.put(origRacer.getClientIndex());
        buffer.put(sequenceBytes);

        for (int i = 6; i < packet.length - 1; i++)
        {
            buffer.put(packet[i]);
        }

        return buffer;
    }
}
