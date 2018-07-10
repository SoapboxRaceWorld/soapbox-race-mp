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

        int port = packet.sender().getPort();
        Racer racer = RacerManager.get(port);

        if (racer == null)
        {
            logger.error("Racer not found!");
            return;
        }

        RaceSession session = RaceSessionManager.get(racer);

        if (isSyncStart(data))
        {
            racer.getSyncStart().read(buf);
            racer.setSyncStartReady(true);

            logger.debug("Got sync start:");
            logger.debug("\tcounter        = {}", racer.getSyncStart().counter);
            logger.debug("\thandshakeSync  = {}", racer.getSyncStart().handshakeSync);
            logger.debug("\tunknownCounter = {}", racer.getSyncStart().unknownCounter);
            logger.debug("\tcliHelloTime   = {}", racer.getSyncStart().cliHelloTime);
            logger.debug("\ttime           = {}", racer.getSyncStart().time);
            logger.debug("\tSubpacket:");
            logger.debug("\t\tmaxPlayers = {}", racer.getSyncStart().subPacket.maxPlayers);
            logger.debug("\t\tplayerSlot = {}", racer.getSyncStart().subPacket.playerSlot);
            logger.debug("\t\tslotByte   = {}", Integer.toString(racer.getSyncStart().subPacket.slotByte, 16));
            logger.debug("\t\tsessionID  = {}", racer.getSyncStart().subPacket.sessionID);
            logger.debug("\t\tunknown    = {}", racer.getSyncStart().subPacket.unknown);

            answer(racer, racer.getSyncStart());

            if (session.allPlayersSyncStartReady())
            {
                logger.debug("Re-sending sync start");
                session.getRacers().forEach(r -> answer(r, r.getSyncStart()));
            }
        } else
        {
            super.channelRead(ctx, msg);
        }
    }

    private boolean isSyncStart(byte[] data)
    {
        return data.length == 26
                && data[0] == 0x00
                && data[3] == 0x07
                && data[4] == 0x02;
    }

    private void answer(Racer racer, ClientSyncStart clientSyncStart)
    {
//        ClientSyncStart clientSyncStart = racer.getSyncStart();
        ServerSyncStart response = new ServerSyncStart();
        response.unknownCounter = clientSyncStart.unknownCounter;
        response.time = (int) racer.getTimeDiff();
//        response.time = clientSyncStart.time;
        response.sessionID = clientSyncStart.subPacket.sessionID;
        response.counter = racer.getSyncSequence();
//        response.cliHelloTime = clientSyncStart.cliHelloTime;
        response.cliHelloTime = racer.getCliHelloTime();
        response.numPlayers = clientSyncStart.subPacket.maxPlayers;
        response.gridIndex = clientSyncStart.subPacket.playerSlot;

        ByteBuffer buffer = ByteBuffer.allocate(25);
        response.write(buffer);
        racer.send(buffer);
    }
}
