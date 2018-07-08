package world.soapboxrace.mp.server.netty.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import world.soapboxrace.mp.race.RaceSession;
import world.soapboxrace.mp.race.RaceSessionManager;
import world.soapboxrace.mp.race.Racer;
import world.soapboxrace.mp.race.RacerManager;
import world.soapboxrace.mp.server.netty.messages.ClientSyncStart;
import world.soapboxrace.mp.server.netty.messages.ServerSyncStart;

import java.nio.ByteBuffer;

public class SyncStartHandler extends BaseHandler
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

        if (isSyncStart(data))
        {
            logger.debug("Got sync-start packet");

            racer.getSyncStart().read(buf);
            racer.setSyncStartReady(true);

            for (Racer sessionRacer : session.getRacers())
            {
                if (sessionRacer.isSyncStartReady())
                    answer(racer);
            }
        }

        super.channelRead(ctx, msg);
    }

    private void answer(Racer racer)
    {
        ServerSyncStart syncStart = new ServerSyncStart();
        ByteBuffer buffer = ByteBuffer.allocate(25);

        ClientSyncStart racerSyncStart = racer.getSyncStart();
        ClientSyncStart.SubPacket subPacket = racerSyncStart.subPacket;

        syncStart.gridIndex = subPacket.playerSlot;
        syncStart.numPlayers = subPacket.maxPlayers;
        syncStart.unknownCounter = racerSyncStart.unknownCounter;
        syncStart.cliHelloTime = racer.getCliHelloTime();
        syncStart.counter = racer.getSequenceC();
        syncStart.sessionID = racer.getSessionID();
        syncStart.time = (short) racer.getTimeDiff();

        syncStart.write(buffer);
        racer.send(buffer);
    }

    private boolean isSyncStart(byte[] data)
    {
        return data.length == 26
                && data[0] == 0x00
                && data[3] == 0x07
                && data[4] == 0x02;
    }
}
