package br.com.sbrw.mp.handlerstr;

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

public class PlayerInfoBeforeHandlerStr extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		DatagramPacket datagramPacket = (DatagramPacket) msg;
		ByteBuf buf = datagramPacket.content();
		if (isPlayerInfoPacket(buf)) {
			System.out.println("PLAYER INFO BEFORE");
			MpTalker mpTalker = MpAllTalkers.get(datagramPacket);
			if (mpTalker != null) {
				mpTalker.parsePlayerInfo(ByteBufUtil.getBytes(buf));
				playerInfoBeforeOk(mpTalker);
			}
		}
		super.channelRead(ctx, msg);
	}

	private void playerInfoBeforeOk(MpTalker mpTalker) {
		MpSession mpSession = MpSessions.get(mpTalker);
		if (mpSession.isAllPlayerInfoBeforeOk()) {
			Map<Integer, MpTalker> mpTalkers = mpSession.getMpTalkers();
			Iterator<Entry<Integer, MpTalker>> iterator = mpTalkers.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Integer, MpTalker> next = iterator.next();
				broadcastPlayerInfoFrom(next.getValue());
			}
		}
	}

	private void broadcastPlayerInfoFrom(MpTalker mpTalker) {
		MpSession mpSession = MpSessions.get(mpTalker);
		if (mpSession.isAllPlayerInfoBeforeOk()) {
			Map<Integer, MpTalker> mpTalkers = mpSession.getMpTalkers();
			Iterator<Entry<Integer, MpTalker>> iterator = mpTalkers.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Integer, MpTalker> next = iterator.next();
				if (!next.getKey().equals(mpTalker.getPort())) {
					next.getValue().send(mpTalker.getPlayerPacket());
				}
			}
		}
	}

	private boolean isPlayerInfoPacket(ByteBuf buf) {
		return buf.getByte(0) == (byte) 0x32 //
				&& buf.getByte(1) == (byte) 0x2d;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		System.err.println(cause.getMessage());
		ctx.close();
	}
}
