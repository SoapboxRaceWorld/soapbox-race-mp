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

public class SyncHandler extends BaseHandler
{
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
        ByteBuf buf = datagramPacket.content();
        byte[] packet = ByteBufUtil.getBytes(buf);

        if (isSync(packet))
        {
            logger.info("Received sync packet!");
            MpClient mpClient = MpClients.get(datagramPacket);

            if (mpClient != null)
            {
                MpSession mpSession = MpSessions.get(mpClient);
                mpClient.setSyncOk(true);
                mpSession.setSyncPacket(packet);
                syncOk(mpSession);
            }
        }

        super.channelRead(ctx, msg);
    }

    private boolean isSync(byte[] data)
    {
        return data[0] == 0x00
                && data[3] == 0x07
                && data.length == 22;
    }

    private void syncOk(MpSession mpSession)
    {
        if (mpSession.isAllSyncOk())
        {
            Map<Integer, MpClient> mpClients = mpSession.getClients();
            Iterator<Map.Entry<Integer, MpClient>> iterator = mpClients.entrySet().iterator();
            while (iterator.hasNext())
            {
                Map.Entry<Integer, MpClient> next = iterator.next();
                MpClient value = next.getValue();
                value.send(answer(value, mpSession.getSyncPacket()).array());
            }
        }
    }

    private ByteBuffer answer(MpClient mpClient, byte[] packet)
    {
        return transformByteTypeASync22(mpClient, packet);
    }

    private ByteBuffer transformByteTypeASync22(MpClient mpClient, byte[] packet)
    {
        byte[] seqArray = mpClient.getSequenceA();
        byte[] timeArray = ByteBuffer.allocate(2).putShort((short) mpClient.getTimeDiff()).array();
        byte[] helloTimeBytes = ByteBuffer.allocate(2)
                .putShort((short) mpClient.getHelloTime())
                .array();
        
        ByteBuffer buffer = ByteBuffer.allocate(packet.length);
        buffer.put((byte) 0x00); // MP type
        buffer.put(seqArray);
        buffer.put((byte) 0x02); // SRV type
        buffer.put(timeArray);
        buffer.put(helloTimeBytes);
        
        // Unknown counter
        buffer.put(packet[9]);
        buffer.put(packet[10]);
        // Handshake sync
        buffer.put(packet[11]);
        buffer.put(packet[12]);
        
        // Subpacket
        buffer.put((byte) 0x01); // ID
        buffer.put((byte) 0x03); // size
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x40);
        buffer.put((byte) 0xb7);
        buffer.put((byte) 0xff); // end
        
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x02);
        buffer.put((byte) 0x03);
        buffer.put((byte) 0x04);
        
        return buffer;
    }
}
