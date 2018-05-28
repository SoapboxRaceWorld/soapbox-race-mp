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

public class SyncHandler extends ChannelInboundHandlerAdapter
{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
        ByteBuf buf = datagramPacket.content();
        byte[] packet = ByteBufUtil.getBytes(buf);
        if (isHelloSync(packet))
        {
            System.out.println("SYNC");
            MpTalker mpTalker = MpAllTalkers.get(datagramPacket);
            if (mpTalker != null)
            {
                MpSession mpSession = MpSessions.get(mpTalker);
                mpTalker.setSyncOk(true);
                mpSession.setSyncPacket(packet);
                syncOk(mpSession);
            }
        }
        super.channelRead(ctx, msg);
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
                value.send(answer(value, mpSession.getSyncPacket()));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        cause.printStackTrace();
        System.err.println(cause.getMessage());
    }

    private boolean isHelloSync(byte[] dataPacket)
    {
        if (dataPacket[0] == (byte) 0x00 //
                && dataPacket[3] == (byte) 0x07 //
                && dataPacket.length == 22)
        {
            return true;
        }
        return false;
    }

    private byte[] answer(MpTalker mpTalker, byte[] packet)
    {
        return transformByteTypeASync22(mpTalker, packet);
    }

    private byte[] transformByteTypeASync22(MpTalker mpTalker, byte[] packet)
    {
        byte[] seqArray = mpTalker.getSequenceA();
        byte[] timeArray = ByteBuffer.allocate(2).putShort((short) mpTalker.getTimeDiff()).array();

        // C->S structure:
        // mp type (1), counter (2), srv type (1) x2, time (2), 
        // cli hello time (2), unk counter (2), HS sync (2), 2-byte subpacket
        // crc

        int size = packet.length;
        byte[] dataTmp = new byte[size];

        dataTmp[0] = 0x00; // MP type 0
        dataTmp[1] = seqArray[0];
        dataTmp[2] = seqArray[1];

        dataTmp[3] = 0x02; // SRV type 2
        // Client sends a different time...
        dataTmp[4] = timeArray[0];
        dataTmp[5] = timeArray[1];

        short helloTime = (short) mpTalker.getCliHelloTime();
        byte[] helloTimeBytes = ByteBuffer.allocate(2).putShort(helloTime).array();

        dataTmp[6] = helloTimeBytes[0];
        dataTmp[7] = helloTimeBytes[1];

        // unknown counter is repeated from the client
        dataTmp[8] = packet[9];
        dataTmp[9] = packet[10];

        // HS sync comes from the client
        dataTmp[10] = packet[11];
        dataTmp[11] = packet[12];

        dataTmp[12] = 0x01; // subpacket type
        dataTmp[13] = 0x03; // subpacket size
        dataTmp[14] = 0x00; // byte 1
        dataTmp[15] = 0x40; // byte 2
        dataTmp[16] = (byte) 0xb7; // byte 3
        dataTmp[17] = (byte) 0xff;

        dataTmp[18] = (byte) 0x01;
        dataTmp[19] = (byte) 0x01;
        dataTmp[20] = (byte) 0x01;
        dataTmp[21] = (byte) 0x01;
        
        return dataTmp;

//		int size = packet.length;
//		byte[] dataTmp = new byte[size];
//		dataTmp[1] = seqArray[0];
//		dataTmp[2] = seqArray[1];
//		int iDataTmp = 3;
//		for (int i = 4; i < packet.length; i++) {
//			dataTmp[iDataTmp++] = packet[i];
//		}
//		dataTmp[4] = timeArray[0];
//		dataTmp[5] = timeArray[1];
//
//		dataTmp[15] = (byte) 0x01;
//		dataTmp[16] = (byte) 0xff;
//		dataTmp[17] = (byte) 0xff;
//		dataTmp[18] = (byte) 0x01;
//		dataTmp[19] = (byte) 0x01;
//		dataTmp[20] = (byte) 0x01;
//		dataTmp[21] = (byte) 0x01;
//		return dataTmp.clone();
    }
}
