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
import java.util.Map.Entry;

public class HelloHandler extends BaseHandler
{
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
        ByteBuf buf = datagramPacket.content();

        if (isHelloPacket(buf))
        {
            logger.info("Received hello!");
            byte[] cliTime = ByteBufUtil.getBytes(buf, 69, 2);
            int sessionId = buf.getInt(9);
            byte clientIndex = buf.getByte(4);
            byte playerCount = buf.getByte(13);

            logger.info("Session ID: {} - client index: {} - player count: {}",
                    sessionId,
                    clientIndex,
                    playerCount);

            short cliTimeShort = buf.getShort(69);

            MpClient mpClient = new MpClient(ctx, datagramPacket, sessionId, cliTime, clientIndex, playerCount);
            mpClient.setHelloTime(cliTimeShort);

            MpClients.put(mpClient);
            MpSession mpSession = MpSessions.get(mpClient);
            if (mpSession == null)
            {
                mpSession = new MpSession(mpClient, (int) playerCount);
                mpSession.setCliTimeStart(cliTimeShort);
            }

            mpSession.put(mpClient);
            MpSessions.put(mpSession);

            if (mpSession.isFull())
            {
                Map<Integer, MpClient> mpClients = mpSession.getClients();
                Iterator<Entry<Integer, MpClient>> iterator = mpClients.entrySet().iterator();
                while (iterator.hasNext())
                {
                    Entry<Integer, MpClient> next = iterator.next();
                    MpClient value = next.getValue();
                    value.send(answer(value).array());
                }
            }

            MpClients.put(mpClient);
            answer(mpClient);
        }

        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        logger.error("While handling HELLO:", cause);
        super.exceptionCaught(ctx, cause);
    }

    private boolean isHelloPacket(ByteBuf buf)
    {
        return buf.getByte(0) == 0x00
                && buf.getByte(3) == 0x06;
    }

    private ByteBuffer answer(MpClient client)
    {
        byte[] sequenceBytes = client.getSequenceA();
        byte[] timeDiffBytes = ByteBuffer.allocate(2)
                .putShort((short) client.getTimeDiff())
                .array();
        byte[] helloTimeBytes = ByteBuffer.allocate(2)
                .putShort((short) client.getHelloTime())
                .array();
        
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.put((byte) 0x00);
        buffer.put(sequenceBytes);
        buffer.put((byte) 0x01);
        buffer.put(timeDiffBytes);
        buffer.put(helloTimeBytes);
        
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x02);
        buffer.put((byte) 0x03);
        buffer.put((byte) 0x04);
        
        return buffer;
    }
}
