package br.com.sbrw.mp.handler;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import br.com.sbrw.mp.protocol.MpAllTalkers;
import br.com.sbrw.mp.protocol.MpSession;
import br.com.sbrw.mp.protocol.MpSessions;
import br.com.sbrw.mp.protocol.MpTalker;
import br.com.sbrw.mp.util.UdpDebug;
import br.com.sbrw.mp.util.UdpUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

public class SyncHelloHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		DatagramPacket datagramPacket = (DatagramPacket) msg;
		ByteBuf buf = datagramPacket.content();
		if (isHelloSync(ByteBufUtil.getBytes(buf))) {
			// System.out.println("SYNC HELLO");
			MpTalker mpTalker = MpAllTalkers.get(datagramPacket);
			if (mpTalker != null) {
				mpTalker.setSyncHello(ByteBufUtil.getBytes(buf));
				mpTalker.setSyncHelloOk(true);
				syncHelloOk(mpTalker);
			}
		}
		// MpTalker mpTalkerTmp = MpAllTalkers.get(datagramPacket);
		// if (mpTalkerTmp != null && mpTalkerTmp.getClientIdx() == 1) {
		// System.out.println("from: [" + mpTalkerTmp.getClientIdx() + "] " +
		// UdpDebug.byteArrayToHexString(ByteBufUtil.getBytes(buf)));
		// }
		super.channelRead(ctx, msg);
	}

	private void syncHelloOk(MpTalker mpTalker) {
		MpSession mpSession = MpSessions.get(mpTalker);
		if (mpSession.isAllSyncHelloOk()) {
			Map<Integer, MpTalker> mpTalkers = mpSession.getMpTalkers();
			Iterator<Entry<Integer, MpTalker>> iterator = mpTalkers.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Integer, MpTalker> next = iterator.next();
				MpTalker value = next.getValue();
				value.send(answer(mpTalker));
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		System.err.println(cause.getMessage());
	}

	private boolean isHelloSync(byte[] dataPacket) {
		if (dataPacket[0] == (byte) 0x00 //
				&& dataPacket[3] == (byte) 0x07 //
				&& dataPacket.length == 26) {
			return true;
		}
		return false;
	}

	private byte[] answer(MpTalker mpTalker) {
		return transformByteTypeASync(mpTalker, mpTalker.getSyncHello());
	}

	private byte[] transformByteTypeASync(MpTalker mpTalker, byte[] packet) {
		byte[] clone = packet.clone();
		clone = transformByteTypeA(mpTalker, clone);
		clone[(clone.length - 11)] = 0;
		clone[(clone.length - 6)] = UdpUtil.generateSlotsBits(mpTalker.getTotalPlayers());
		clone[8] = 0x00;
		clone[9] = 0x01;
		clone[10] = 0x7f;
		return clone;
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

//	private byte[] transformByteTypeASync(MpTalker mpTalker, byte[] packet) {
//		byte[] clone = packet.clone();
//		clone = transformByteTypeA(mpTalker, clone);
//		clone[(clone.length - 11)] = 0;
//		clone[(clone.length - 6)] = UdpUtil.generateSlotsBits(mpTalker.getTotalPlayers());
//		clone[8] = 0x00;
//		clone[9] = 0x01;
//		clone[10] = (byte) 0xff; // maximum slots? seems to work
//		return clone;
//	}
//
//	private byte[] transformByteTypeA(MpTalker mpTalker, byte[] packet) {
//		byte[] seqArray = mpTalker.getSequenceA();
//		byte[] timeArray = ByteBuffer.allocate(2).putShort((short) mpTalker.getTimeDiff()).array();
//
//		ByteBuffer response = ByteBuffer.allocate(packet.length - 1);
//		response.put((byte) 0x00);
//		response.put(seqArray);
//		response.put((byte) 0x02);
//		response.put(timeArray);
//		response.put(mpTalker.getCliTime());
//		response.put((byte) 0xff);
//		response.put((byte) 0xff);
//		response.put((byte) 0xff);
//		response.put((byte) 0xff);
//		
//		// Subpacket
//		response.put((byte) 0x00);
//		response.put((byte) 0x06);
//		response.put(mpTalker.getClientIdx());
//		response.putInt(mpTalker.getSessionId());
//		response.put(UdpUtil.generateSlotsBits(mpTalker.getTotalPlayers()));
//		response.put((byte) 0xff);
//		response.put((byte) 0x01);
//		response.put((byte) 0x01);
//		response.put((byte) 0x01);
//		response.put((byte) 0x01);
//		
//		return response.array();
//	}
}
