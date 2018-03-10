package br.com.sbrw.mp.handlerstr;

import java.nio.ByteBuffer;

import br.com.sbrw.mp.protocol.MpAllTalkers;
import br.com.sbrw.mp.protocol.MpTalker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

public class SyncKeepAliveStr extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		DatagramPacket datagramPacket = (DatagramPacket) msg;
		ByteBuf buf = datagramPacket.content();
		if (isSyncKeepAlive(ByteBufUtil.getBytes(buf))) {
			System.out.println("SYNC KEEP ALIVE");
			MpTalker mpTalker = MpAllTalkers.get(datagramPacket);
			if (mpTalker != null) {
				answer(mpTalker, ByteBufUtil.getBytes(buf));
			}
		}
		super.channelRead(ctx, msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		System.err.println(cause.getMessage());
		ctx.close();
	}

	private boolean isSyncKeepAlive(byte[] dataPacket) {
		if (dataPacket[0] == (byte) 0x00 //
				&& dataPacket[3] == (byte) 0x07 //
				&& dataPacket.length == 18) {
			return true;
		}
		return false;
	}

	private void answer(MpTalker mpTalker, byte[] packet) {
		byte[] transformByteTypeA = transformByteTypeA(mpTalker, packet);
		mpTalker.send(transformByteTypeA);
	}

	private byte[] transformByteTypeA(MpTalker mpTalker, byte[] packet) {
		byte[] seqArray = mpTalker.getSequenceA();
		byte[] timeArray = ByteBuffer.allocate(2).putShort((short) mpTalker.getTimeDiff()).array();
		int size = packet.length - 1;
		byte[] dataTmp = new byte[size];
		dataTmp[1] = seqArray[0];
		dataTmp[2] = seqArray[1];
		int iDataTmp = 3;
		for (int i = 4; i < packet.length; i++) {
			dataTmp[iDataTmp++] = packet[i];
		}
		dataTmp[4] = timeArray[0];
		dataTmp[5] = timeArray[1];
		return dataTmp;
	}
}
