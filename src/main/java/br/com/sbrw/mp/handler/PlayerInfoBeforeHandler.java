package br.com.sbrw.mp.handler;

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

public class PlayerInfoBeforeHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		DatagramPacket datagramPacket = (DatagramPacket) msg;
		ByteBuf buf = datagramPacket.content();
		if (isPlayerInfoPacket(buf)) {
			// System.out.println("PLAYER INFO BEFORE");
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
					MpTalker value = next.getValue();
					value.send(transformByteTypeB(mpTalker, mpTalker.getClientIdx()));
				}
			}
		}
	}

	private boolean isPlayerInfoPacket(ByteBuf buf) {
		return buf.getByte(0) == (byte) 0x01 //
				&& buf.getByte(6) == (byte) 0xff //
				&& buf.getByte(7) == (byte) 0xff //
				&& buf.getByte(8) == (byte) 0xff //
				&& buf.getByte(9) == (byte) 0xff;
	}

	private byte[] transformByteTypeB(MpTalker mpTalker, byte cliIdx) {
		byte[] packet = mpTalker.getPlayerPacket();
		if (packet.length < 4) {
			return null;
		}
		byte[] seqArray = mpTalker.getSequenceB();
		int size = packet.length - 3;
		byte[] dataTmp = new byte[size];
		dataTmp[0] = 1;
		dataTmp[1] = cliIdx;// mpTalker.getClientIdx();
		dataTmp[2] = seqArray[0];
		dataTmp[3] = seqArray[1];
		int iDataTmp = 4;
		for (int i = 6; i < (packet.length - 1); i++) {
			dataTmp[iDataTmp++] = packet[i];
		}
		dataTmp[4] = (byte) 0xff;
		dataTmp[5] = (byte) 0xff;
		return dataTmp;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		System.err.println(cause.getMessage());
	}
}
