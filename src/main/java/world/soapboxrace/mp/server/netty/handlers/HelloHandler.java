package world.soapboxrace.mp.server.netty.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import world.soapboxrace.mp.race.RaceSession;
import world.soapboxrace.mp.race.RaceSessionManager;
import world.soapboxrace.mp.race.Racer;
import world.soapboxrace.mp.race.RacerManager;
import world.soapboxrace.mp.server.netty.messages.ClientHello;
import world.soapboxrace.mp.server.netty.messages.ServerHello;

import java.nio.ByteBuffer;

public class HelloHandler extends BaseHandler
{
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        DatagramPacket packet = (DatagramPacket) msg;
        ByteBuf buf = packet.content();
        byte[] data = ByteBufUtil.getBytes(buf);

        if (isHello(data))
        {
            ClientHello clientHello = new ClientHello();
            clientHello.read(buf);

            logger.debug("Got HELLO:");
            logger.debug("\tnumPlayers = {}", clientHello.numPlayers);
            logger.debug("\tplayerIdx  = {}", clientHello.playerIndex);
            logger.debug("\thelloTime  = {}", clientHello.cliHelloTime);
            logger.debug("\tsessionId  = {}", clientHello.sessionID);

            RaceSession session = RaceSessionManager.get(clientHello.sessionID);

            if (session == null)
            {
                session = RaceSessionManager.add(clientHello.sessionID,
                        new RaceSession(clientHello.sessionID, clientHello.cliHelloTime));
            }

            int port = packet.sender().getPort();
            Racer racer = RacerManager.get(port);

            if (racer == null)
            {
                logger.debug("* Adding Racer for client: {}", port);
                racer = RacerManager.put(port, new Racer(clientHello.sessionID, clientHello.playerIndex, ctx, packet, clientHello.cliHelloTime));
            } else
            {
                logger.warn("Racer already exists! Sending response anyway.");
            }

            session.addRacer(racer);
            sendHelloResponse(racer);
        } else
        {
            super.channelRead(ctx, msg);
        }
    }

    private boolean isHello(byte[] packet)
    {
        return packet.length == 75
                && packet[0] == 0x00
                && packet[3] == 0x06;
    }

    private void sendHelloResponse(Racer racer)
    {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        ServerHello serverHello = new ServerHello();
        serverHello.helloTime = RaceSessionManager.get(racer.getSessionID()).getTimeBase();
        serverHello.time = (short) racer.getTimeDiff();

        serverHello.write(buffer);

        racer.send(buffer);

        logger.debug("Sent HELLO response");
    }
}
