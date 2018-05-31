package world.soapboxrace.mp.server.netty.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import world.soapboxrace.mp.race.MpClient;
import world.soapboxrace.mp.race.MpClients;
import world.soapboxrace.mp.race.MpSession;
import world.soapboxrace.mp.race.MpSessions;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;

public class KeepAliveHandler extends BaseHandler
{
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
        ByteBuf buf = datagramPacket.content();
        byte[] packet = ByteBufUtil.getBytes(buf);

        if (isSyncKeepAlive(packet))
        {
            logger.info("Received sync keep-alive packet!");
            MpClient mpTalker = MpClients.get(datagramPacket);
            if (mpTalker != null)
            {
                syncOk(mpTalker, packet);
            }
        }

        super.channelRead(ctx, msg);
    }

    private boolean isSyncKeepAlive(byte[] packet)
    {
        return packet[0] == (byte) 0x00
                && packet[3] == (byte) 0x07
                && packet.length == 18;
    }

    private void syncOk(MpClient mpClient, byte[] packet)
    {
        MpSession mpSession = MpSessions.get(mpClient);
        if (mpSession.isAllSyncOk())
        {
            Map<Integer, MpClient> mpTalkers = mpSession.getClients();
            Iterator<Map.Entry<Integer, MpClient>> iterator = mpTalkers.entrySet().iterator();
            while (iterator.hasNext())
            {
                Map.Entry<Integer, MpClient> next = iterator.next();
                if (!next.getKey().equals(mpClient.getPort()))
                {
                    MpClient value = next.getValue();
                    value.send(answer(value, packet).array());
                }
            }
        }
    }

    private ByteBuffer answer(MpClient mpClient, byte[] packet)
    {
        return transformPacket(mpClient, packet);
    }

    private ByteBuffer transformPacket(MpClient mpClient, byte[] packet)
    {
        byte[] timeArray = ByteBuffer.allocate(2).putShort((short) mpClient.getTimeDiff()).array();
        byte[] helloTimeBytes = ByteBuffer.allocate(2)
                .putShort((short) mpClient.getHelloTime())
                .array();
        
        ByteBuffer buffer = ByteBuffer.allocate(packet.length - 1); // should always be 17 bytes
        buffer.put((byte) 0x00); // MP type
        buffer.put(mpClient.getSequenceA());
        buffer.put((byte) 0x02); // SRV type
        buffer.put(timeArray);
        buffer.put(helloTimeBytes);
        buffer.put(packet[9]);
        buffer.put(packet[10]);
        buffer.put(packet[11]);
        buffer.put(packet[12]);
        buffer.put((byte) 0xff);
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x02);
        buffer.put((byte) 0x03);
        buffer.put((byte) 0x04);
        
        return buffer;
    }
}
