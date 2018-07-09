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

        Racer racer = RacerManager.get(packet.sender().getPort());

        if (racer == null)
        {
            logger.error("Racer is null!");
            return;
        }

        if (isSync(data))
        {
            logger.debug("Got sync packet");
            racer.setSyncReady(true);

            ClientSync clientSync = new ClientSync();
            clientSync.read(buf);

            ServerSync serverSync = new ServerSync();
            ByteBuffer buffer = ByteBuffer.allocate(22);
            
            serverSync.cliHelloTime = racer.getCliHelloTime();
            serverSync.time = (short) racer.getTimeDiff();
            serverSync.counter = racer.getSyncSequence();
            serverSync.unknownCounter = clientSync.unknownCounter;
            
            serverSync.write(buffer);
            
            racer.send(buffer);
            
            logger.debug("Sent sync response");
        } else
        {
            super.channelRead(ctx, msg);
        }
    }

    private boolean isSync(byte[] data)
    {
        return data.length == 22
                && data[0] == 0x00
                && data[3] == 0x07;
    }
}
