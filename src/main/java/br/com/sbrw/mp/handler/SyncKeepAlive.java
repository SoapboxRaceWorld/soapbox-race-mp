package br.com.sbrw.mp.handler;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import br.com.sbrw.mp.protocol.MpAllTalkers;
import br.com.sbrw.mp.protocol.MpSession;
import br.com.sbrw.mp.protocol.MpSessions;
import br.com.sbrw.mp.protocol.MpTalker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

public class SyncKeepAlive extends ChannelInboundHandlerAdapter
{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
        ByteBuf buf = datagramPacket.content();
        byte[] packet = ByteBufUtil.getBytes(buf);
        if (isSyncKeepAlive(packet))
        {
            // System.out.println("SYNC KEEP ALIVE");
            MpTalker mpTalker = MpAllTalkers.get(datagramPacket);
            if (mpTalker != null)
            {
                MpSession mpSession = MpSessions.get(mpTalker);
//                mpTalker.setSyncOk(true);
//                mpSession.setKeepAlivePacket(packet);
//                syncOk(mpSession);
                syncOk(mpTalker, packet);
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        cause.printStackTrace();
        System.err.println(cause.getMessage());
    }

    private boolean isSyncKeepAlive(byte[] dataPacket)
    {
        if (dataPacket[0] == (byte) 0x00 //
                && dataPacket[3] == (byte) 0x07 //
                && dataPacket.length == 18)
        {
            return true;
        }
        return false;
    }

    private void syncOk(MpSession mpSession)
    {
        if (mpSession.isAllSyncOk())
        {
            Map<Integer, MpTalker> mpTalkers = mpSession.getMpTalkers();
            Iterator<Entry<Integer, MpTalker>> iterator = mpTalkers.entrySet().iterator();
            while (iterator.hasNext())
            {
                Entry<Integer, MpTalker> next = iterator.next();
                MpTalker value = next.getValue();
                value.send(answer(value, mpSession.getKeepAlivePacket()));
            }
        }
    }

    private void syncOk(MpTalker mpTalker, byte[] packet)
    {
        MpSession mpSession = MpSessions.get(mpTalker);
        if (mpSession.isAllSyncOk())
        {
            Map<Integer, MpTalker> mpTalkers = mpSession.getMpTalkers();
            Iterator<Entry<Integer, MpTalker>> iterator = mpTalkers.entrySet().iterator();
            while (iterator.hasNext())
            {
                Entry<Integer, MpTalker> next = iterator.next();
                if (!next.getKey().equals(mpTalker.getPort()))
                {
                    MpTalker value = next.getValue();
                    value.send(answer(value, packet));
                }
            }
        }
    }

    private byte[] answer(MpTalker mpTalker, byte[] packet)
    {
        return transformByteTypeA(mpTalker, packet);
    }

    private byte[] transformByteTypeA(MpTalker mpTalker, byte[] packet)
    {
        // Client structure:
        // mp type (byte), counter (short), srv type (byte), srv type 2 (byte)
        // time (short), cli hello time (short), counter? (short), hs sync (short)
        // packet end (0xff) (byte)
        
        byte[] seqArray = mpTalker.getSequenceA();
        byte[] timeArray = ByteBuffer.allocate(2).putShort((short) mpTalker.getTimeDiff()).array();
        int size = packet.length - 1;
        byte[] dataTmp = new byte[size];
        
        dataTmp[0] = 0x00; // MP type
        dataTmp[1] = seqArray[0]; // seq
        dataTmp[2] = seqArray[1]; // seq
        dataTmp[3] = 0x02; // srv type
        dataTmp[4] = timeArray[0]; // time
        dataTmp[5] = timeArray[1]; // time
        dataTmp[6] = packet[7]; // cli hello time
        dataTmp[7] = packet[8]; // cli hello time
        dataTmp[8] = packet[9]; // ucount
        dataTmp[9] = packet[10]; // ucount
        dataTmp[10] = packet[11]; // hs sync
        dataTmp[11] = packet[12]; // hs sync
        dataTmp[12] = (byte) 0xff; // end
        dataTmp[13] = 0x01;
        dataTmp[14] = 0x01;
        dataTmp[15] = 0x01;
        dataTmp[16] = 0x01;
        
        return dataTmp;
    }
}
