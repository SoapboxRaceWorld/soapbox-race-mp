package world.soapboxrace.mp.server.netty.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
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

        int port = packet.sender().getPort();
        Racer racer = RacerManager.get(port);

        if (racer == null)
        {
            logger.error("Racer not found!");
            return;
        }

        RaceSession session = RaceSessionManager.get(racer);

        if (isInfoBeforeSync(data))
        {
            logger.debug("got info before sync");

            racer.parsePacket(data);

            if (session.allPlayersInfoOK())
            {
//                System.out.println("info hexdump");
//                System.out.println(ByteBufUtil.prettyHexDump(Unpooled.copiedBuffer(
//                        transformPlayerPacket(racer.getPlayerInfoPacket(), racer, racer.getClientIndex())
//                )));
                logger.debug("all players OK!");
                doSessionBroadcast(session);
            }
        } else
        {
            super.channelRead(ctx, msg);
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

    private void doSessionBroadcast(RaceSession session)
    {
        for (Racer racer : session.getRacers())
        {
            for (Racer otherRacer : session.getRacers())
            {
                if (otherRacer.getClientIndex() == racer.getClientIndex()) continue;

                otherRacer.send(transformPlayerPacket(racer.getPlayerPacket(), otherRacer, racer.getClientIndex()));
                logger.debug("{} -> {}", racer.getClientIndex(), otherRacer.getClientIndex());
            }

            racer.incrementPreInfoSequence();
        }
    }

    private ByteBuffer transformPlayerPacket(byte[] data, Racer toRacer, byte racerIndex)
    {
        ByteBuffer buffer = ByteBuffer.allocate(data.length - 3);
        byte[] seqBytes = ByteBuffer.allocate(2).putShort(toRacer.getPreInfoSequence()).array();

        buffer.put((byte) 0x01);
        buffer.put(racerIndex);
        buffer.put(seqBytes);

        for (int i = 6; i < data.length - 1; i++)
        {
            buffer.put(data[i]);
        }

        return buffer;
    }
}
