package world.soapboxrace.mp.server.netty.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import world.soapboxrace.mp.race.Racer;
import world.soapboxrace.mp.race.RacerManager;
import world.soapboxrace.mp.server.netty.messages.ClientSync;
import world.soapboxrace.mp.server.netty.messages.ServerSync;

import java.nio.ByteBuffer;

public class SyncHandler extends BaseHandler
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

        if (isSync(data))
        {
            logger.debug("Got sync packet");

            ClientSync clientSync = new ClientSync();
            clientSync.read(buf);

            ServerSync response = new ServerSync();
            response.unknownCounter = clientSync.unknownCounter;
//            response.counter = clientSync.counter;
//            response.time = clientSync.time;
            response.counter = racer.getSyncSequence();
            response.time = (int) racer.getTimeDiff();
            response.cliHelloTime = racer.getCliHelloTime();
//            response.cliHelloTime = clientSync.cliHelloTime;

            ByteBuffer responseBuf = ByteBuffer.allocate(22);
            response.write(responseBuf);

            racer.send(responseBuf);
        } else
        {
            super.channelRead(ctx, msg);
        }
    }

    private boolean isSync(byte[] data)
    {
        return data.length == 22
                && data[0] == 0x00
                && data[3] == 0x07
                && data[4] == 0x02;
    }
}
