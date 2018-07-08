package world.soapboxrace.mp.server.netty.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import world.soapboxrace.mp.race.RaceSession;
import world.soapboxrace.mp.race.RaceSessionManager;
import world.soapboxrace.mp.race.Racer;
import world.soapboxrace.mp.race.RacerManager;
import world.soapboxrace.mp.util.ArrayReader;

import java.nio.ByteBuffer;

public class InfoAfterSyncHandler extends BaseHandler
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

        if (isInfoAfterSync(data))
        {
            logger.debug("Received info after sync");

            if (racer.isSyncReady() && session.allPlayersSyncReady())
            {
                for (Racer sessionRacer : session.getRacers())
                {
                    if (sessionRacer.getClientIndex() != racer.getClientIndex())
                    {
                        sessionRacer.send(transformByteTypeB(sessionRacer, data, racer));
                    }
                }
            }
        }

        super.channelRead(ctx, msg);
    }

    private boolean isInfoAfterSync(byte[] data)
    {
        return data[0] == 0x01;
    }

    private ByteBuffer transformByteTypeB(Racer racerTo, byte[] packet, Racer racerFrom)
    {
        byte[] clone = packet.clone();

        try
        {
            clone = fixTime(racerTo, packet);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        if (clone.length < 4)
        {
            return null;
        }

        byte[] seqArray = ByteBuffer.allocate(2).putShort(racerTo.getSequenceB()).array();
        ByteBuffer buffer = ByteBuffer.allocate(clone.length - 3);

        buffer.put((byte) 0x01);
        buffer.put(racerFrom.getClientIndex());
        buffer.put(seqArray);

        for (int i = 6; i < (clone.length - 1); i++)
        {
            buffer.put(clone[i]);
        }

        buffer.position(4);
        buffer.put((byte) 0xff);
        buffer.put((byte) 0xff);

        return buffer;
    }

    private byte[] fixTime(Racer racer, byte[] packet)
    {
        byte[] timeArray = ByteBuffer.allocate(2).putShort((short) racer.getTimeDiff()).array();

        ArrayReader reader = new ArrayReader(packet);

        reader.seek(10);

        while (reader.getPosition() < reader.getLength())
        {
            byte packetId = reader.readByte();

            if (packetId == (byte) 0xff)
            {
                break;
            }

            byte packetLength = reader.readByte();

            if (packetId == 0x12)
            {
                packet[reader.getPosition()] = timeArray[0];
                packet[reader.getPosition() + 1] = timeArray[1];
            }

            reader.seek(packetLength, true);
        }

        return packet;
    }
}
