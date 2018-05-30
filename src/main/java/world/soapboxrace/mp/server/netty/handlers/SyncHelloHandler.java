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

public class SyncHelloHandler extends BaseHandler
{
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
        ByteBuf buf = datagramPacket.content();

        if (isSyncHello(ByteBufUtil.getBytes(buf)))
        {
            logger.info("Received sync hello!");
            MpClient client = MpClients.get(datagramPacket);

            if (client != null)
            {
                client.setSyncHello(ByteBufUtil.getBytes(buf));
                client.setSyncHelloOk(true);
                syncHelloOk(client);
            }
        }

        super.channelRead(ctx, msg);
    }

    private void syncHelloOk(MpClient mpClient)
    {
        MpSession mpSession = MpSessions.get(mpClient);
        if (mpSession.isAllSyncHelloOk())
        {
            Map<Integer, MpClient> mpClients = mpSession.getClients();
            Iterator<Map.Entry<Integer, MpClient>> iterator = mpClients.entrySet().iterator();
            while (iterator.hasNext())
            {
                Map.Entry<Integer, MpClient> next = iterator.next();
                MpClient value = next.getValue();
                value.send(transformPacket(mpClient).array());
            }
        }
    }

    private ByteBuffer transformPacket(MpClient client)
    {
        return transformPacket(client.getSyncHello(), client);
    }

    private ByteBuffer transformPacket(byte[] packet, MpClient client)
    {
        byte[] timeDiffBytes = ByteBuffer.allocate(2)
                .putShort((short) client.getTimeDiff())
                .array();
        byte[] helloTimeBytes = ByteBuffer.allocate(2)
                .putShort((short) client.getHelloTime())
                .array();
        ByteBuffer buffer = ByteBuffer.allocate(25);

        buffer.put((byte) 0x00); // MP type
        buffer.put(client.getSequenceA());
        buffer.put((byte) 0x02); // SRV type

        buffer.put(timeDiffBytes);
        buffer.put(helloTimeBytes);

        // unknown counter
        buffer.put(packet[9]);
        buffer.put(packet[10]);
        // handshake sync
        buffer.put(packet[11]);
        buffer.put(packet[12]);

        // write subpacket
        buffer.put((byte) 0x00); // pkt type
        buffer.put((byte) 0x06); // pkt size
        buffer.put(client.getClientId());
        buffer.putInt(client.getSessionId());
        buffer.put(createSyncSlots(client.getTotalPlayers()));
        buffer.put((byte) 0xff); // packet end
        
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x02);
        buffer.put((byte) 0x03);
        buffer.put((byte) 0x04);

        return buffer;
    }

    private boolean isSyncHello(byte[] data)
    {
        return data[0] == 0x00
                && data[3] == 0x07
                && data.length == 26;
    }

    private byte createSyncSlots(byte numClients)
    {
        byte res = 0;

        for (int i = 0; i < numClients; i++)
        {
            res |= (1 << i);
        }

        return res;
    }
}
