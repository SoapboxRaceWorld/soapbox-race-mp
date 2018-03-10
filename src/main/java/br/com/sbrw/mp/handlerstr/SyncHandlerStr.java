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

public class SyncHandlerStr extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		DatagramPacket datagramPacket = (DatagramPacket) msg;
		ByteBuf buf = datagramPacket.content();
		if (isSync(buf)) {
			System.out.println("SYNC");
			MpTalker mpTalker = MpAllTalkers.get(datagramPacket);
			if (mpTalker != null) {
				MpSession mpSession = MpSessions.get(mpTalker);
				mpTalker.setSyncOk(true);
				mpSession.setSyncPacket(ByteBufUtil.getBytes(buf));
				syncOk(mpSession);
			}
		}
		super.channelRead(ctx, msg);
	}

	private void syncOk(MpSession mpSession) {
		if (mpSession.isAllSyncOk()) {
			Map<Integer, MpTalker> mpTalkers = mpSession.getMpTalkers();
			Iterator<Entry<Integer, MpTalker>> iterator = mpTalkers.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Integer, MpTalker> next = iterator.next();
				MpTalker value = next.getValue();
				value.send(mpSession.getSyncPacket());
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		System.err.println(cause.getMessage());
		ctx.close();
	}

	private boolean isSync(ByteBuf buf) {
		return buf.getByte(0) == (byte) 0x33 //
				&& buf.getByte(1) == (byte) 0x2d;
	}

}
