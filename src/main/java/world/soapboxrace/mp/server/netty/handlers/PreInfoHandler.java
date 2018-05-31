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

public class PreInfoHandler extends BaseHandler
{
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
        ByteBuf buf = datagramPacket.content();

        if (isPreInfo(buf))
        {
            logger.info("Received pre-info packet!");
            MpClient client = MpClients.get(datagramPacket);

            if (client != null)
            {
                client.parsePacket(ByteBufUtil.getBytes(buf));
                playerInfoBeforeOk(client);
            }
        }

        super.channelRead(ctx, msg);
    }

    private boolean isPreInfo(ByteBuf buf)
    {
        return buf.getByte(0) == (byte) 0x01 //
                && buf.getByte(6) == (byte) 0xff //
                && buf.getByte(7) == (byte) 0xff //
                && buf.getByte(8) == (byte) 0xff //
                && buf.getByte(9) == (byte) 0xff;
    }

    private void playerInfoBeforeOk(MpClient mpClient)
    {
        MpSession mpSession = MpSessions.get(mpClient);
        if (mpSession.isAllPlayerInfoBeforeOk())
        {
            Map<Integer, MpClient> mpTalkers = mpSession.getClients();
            Iterator<Map.Entry<Integer, MpClient>> iterator = mpTalkers.entrySet().iterator();
            while (iterator.hasNext())
            {
                Map.Entry<Integer, MpClient> next = iterator.next();
                broadcastPlayerInfoFrom(next.getValue());
            }
        }
    }

    private void broadcastPlayerInfoFrom(MpClient mpClient)
    {
        MpSession mpSession = MpSessions.get(mpClient);
        if (mpSession.isAllPlayerInfoBeforeOk())
        {
            Map<Integer, MpClient> mpClients = mpSession.getClients();
            Iterator<Map.Entry<Integer, MpClient>> iterator = mpClients.entrySet().iterator();
            while (iterator.hasNext())
            {
                Map.Entry<Integer, MpClient> next = iterator.next();
                if (!next.getKey().equals(mpClient.getPort()))
                {
                    MpClient value = next.getValue();
                    value.send(transformPacket(mpClient, mpClient.getClientId()).array());
                }
            }
        }
    }

    private ByteBuffer transformPacket(MpClient client, byte clientIndex)
    {
        byte[] packet = client.getPlayerPacket();
        if (packet.length < 4)
        {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.allocate(packet.length - 3);
        buffer.put((byte) 0x01);
        buffer.put(clientIndex);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);

        for (int i = 6; i < (packet.length - 1); i++)
        {
            buffer.put(packet[i]);
        }

        return buffer;
    }
}
