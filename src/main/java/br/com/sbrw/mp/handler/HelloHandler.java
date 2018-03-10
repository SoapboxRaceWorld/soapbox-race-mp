package br.com.sbrw.mp.handler;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import br.com.sbrw.mp.parser.SbrwParserEcho;
import br.com.sbrw.mp.protocol.MpAllTalkers;
import br.com.sbrw.mp.protocol.MpSession;
import br.com.sbrw.mp.protocol.MpSessions;
import br.com.sbrw.mp.protocol.MpTalker;
import br.com.sbrw.mp.util.UdpDebug;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

public class HelloHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		DatagramPacket datagramPacket = (DatagramPacket) msg;
		ByteBuf buf = datagramPacket.content();
		if (isHelloPacket(buf)) {
			// System.out.println("HELLO");
			// System.out.println(UdpDebug.byteArrayToHexString(ByteBufUtil.getBytes(buf)));
			byte[] cliTime = ByteBufUtil.getBytes(buf, 69, 2);
			Integer sessionId = buf.getInt(9);
			byte clientIdx = buf.getByte(4);
			byte maxUsers = buf.getByte(13);
			System.out.println("session: [" + sessionId + "]");
			System.out.println("clientIdx: [" + (int) clientIdx + "]");
			System.out.println("maxUsers: [" + (int) maxUsers + "]");
			System.out.println("cliTime :[" + UdpDebug.byteArrayToHexString(cliTime) + "]");
			System.out.println("");
			short cliTimeShort = buf.getShort(69);
			MpTalker mpTalker = new MpTalker(ctx, datagramPacket, new SbrwParserEcho(), sessionId, cliTime, clientIdx, maxUsers);
			mpTalker.setCliHelloTime(cliTimeShort);
			MpAllTalkers.put(mpTalker);
			MpSession mpSession = MpSessions.get(mpTalker);
			if (mpSession == null) {
				mpSession = new MpSession(mpTalker, (int) maxUsers);
				mpSession.setCliTimeStart(cliTimeShort);
			}
			mpSession.put(mpTalker);
			MpSessions.put(mpSession);
			if (mpSession.isFull()) {
				Map<Integer, MpTalker> mpTalkers = mpSession.getMpTalkers();
				Iterator<Entry<Integer, MpTalker>> iterator = mpTalkers.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<Integer, MpTalker> next = iterator.next();
					MpTalker value = next.getValue();
					value.send(answer(value));
				}
			}

			MpAllTalkers.put(mpTalker);
			answer(mpTalker);
		}
		super.channelRead(ctx, msg);
	}

	private boolean isHelloPacket(ByteBuf buf) {
		return buf.getByte(0) == (byte) 0x00 && buf.getByte(3) == (byte) 0x06;
	}

	private byte[] answer(MpTalker mpTalker) {
		byte[] sequenceA = mpTalker.getSequenceA();
		byte[] sendData = UdpDebug.hexStringToByteArray("00:00:00:01:99:99:66:66:01:01:01:01");
		byte[] timeArray = ByteBuffer.allocate(2).putShort((short) mpTalker.getTimeDiff()).array();
		sendData[1] = sequenceA[0];
		sendData[2] = sequenceA[1];
		sendData[4] = timeArray[0];
		sendData[5] = timeArray[1];
		byte[] cliTime = mpTalker.getCliTime();
		sendData[6] = cliTime[0];
		sendData[7] = cliTime[1];
		return sendData;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		System.err.println(cause.getMessage());
	}

}
