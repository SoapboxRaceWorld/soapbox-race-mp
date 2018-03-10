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

public class SyncHelloHandlerStr extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		DatagramPacket datagramPacket = (DatagramPacket) msg;
		ByteBuf buf = datagramPacket.content();
		if (isSyncHello(ByteBufUtil.getBytes(buf))) {
			System.out.println("SYNC HELLO");
			MpTalker mpTalker = MpAllTalkers.get(datagramPacket);
			if (mpTalker != null) {
				mpTalker.setSyncHello(ByteBufUtil.getBytes(buf));
				mpTalker.setSyncHelloOk(true);
				syncHelloOk(mpTalker);
			}
		}
		super.channelRead(ctx, msg);
	}

	private boolean isSyncHello(byte[] dataPacket) {
		return dataPacket[0] == (byte) 0x31 && dataPacket[1] == (byte) 0x2d;
	}

	private void syncHelloOk(MpTalker mpTalker) {
		MpSession mpSession = MpSessions.get(mpTalker);
		if (mpSession.isAllSyncHelloOk()) {
			Map<Integer, MpTalker> mpTalkers = mpSession.getMpTalkers();
			Iterator<Entry<Integer, MpTalker>> iterator = mpTalkers.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Integer, MpTalker> next = iterator.next();
				MpTalker value = next.getValue();
				value.send(answer(value));
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		System.err.println(cause.getMessage());
		ctx.close();
	}

	private byte[] answer(MpTalker mpTalker) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("\n ---- SYNC HELLO GERAL! ----\n sessao: [");
		stringBuilder.append(mpTalker.getSessionId());
		stringBuilder.append("]\n");
		stringBuilder.append("syncHello original: [");
		stringBuilder.append((new String(mpTalker.getSyncHello())).trim());
		stringBuilder.append("]\n\n");
		return stringBuilder.toString().getBytes();
	}

}
