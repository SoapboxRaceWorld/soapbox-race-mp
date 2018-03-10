package br.com.sbrw.mp.handlerstr;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import br.com.sbrw.mp.parser.StrParser;
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

public class HelloHandlerStr extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		DatagramPacket datagramPacket = (DatagramPacket) msg;
		ByteBuf buf = datagramPacket.content();
		// System.out.println(UdpDebug.byteArrayToHexString(ByteBufUtil.getBytes(buf)));
		if (isHelloPacket(buf)) {
			System.out.println("HELLO");
			byte[] cliTime = ByteBufUtil.getBytes(buf, 2, 2);
			String sessionIdStr = new String(ByteBufUtil.getBytes(buf, 5, 4));
			System.out.println(sessionIdStr);
			Integer sessionId = Integer.valueOf(sessionIdStr);

			String sessionMaxUsersStr = new String(ByteBufUtil.getBytes(buf, 10, 1));
			System.out.println(sessionMaxUsersStr);
			Integer sessionMaxUsers = Integer.valueOf(sessionMaxUsersStr);
			MpTalker mpTalker = new MpTalker(ctx, datagramPacket, new StrParser(), sessionId, cliTime, (byte) 0x00, sessionMaxUsers.byteValue());
			MpAllTalkers.put(mpTalker);
			MpSession mpSession = MpSessions.get(mpTalker);
			if (mpSession == null) {
				mpSession = new MpSession(mpTalker, sessionMaxUsers);
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
		}
		super.channelRead(ctx, msg);
	}

	private boolean isHelloPacket(ByteBuf buf) {
		return buf.getByte(0) == (byte) 0x30 && buf.getByte(1) == (byte) 0x2d;
	}

	private byte[] answer(MpTalker mpTalker) {
		MpSession mpSession = MpSessions.get(mpTalker);
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("AE manolo\n");
		stringBuilder.append(" sua sessao [");
		stringBuilder.append(mpTalker.getSessionId());
		stringBuilder.append("]\n");
		stringBuilder.append(" maxUsers [");
		stringBuilder.append(mpSession.getMaxUsers());
		stringBuilder.append("]\n");
		stringBuilder.append(" isFull [");
		stringBuilder.append(mpSession.isFull());
		stringBuilder.append("]\n\n");
		return stringBuilder.toString().getBytes();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		System.err.println(cause.getMessage());
		ctx.close();
	}

}
