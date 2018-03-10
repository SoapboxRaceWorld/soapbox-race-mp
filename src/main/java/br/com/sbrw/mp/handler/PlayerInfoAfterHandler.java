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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

public class PlayerInfoAfterHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		DatagramPacket datagramPacket = (DatagramPacket) msg;
		ByteBuf buf = datagramPacket.content();
		if (isPlayerInfoPacket(buf)) {
			MpTalker mpTalker = MpAllTalkers.get(datagramPacket);
			if (mpTalker != null) {
				if (mpTalker.isSyncOk()) {
					// System.out.println("PLAYER INFO AFTER");
					playerInfoAfterOk(mpTalker, ByteBufUtil.getBytes(buf));
				}
			}
		}
		super.channelRead(ctx, msg);
	}

	private void playerInfoAfterOk(MpTalker mpTalker, byte[] packet) {
		MpSession mpSession = MpSessions.get(mpTalker);
		if (mpSession.isAllSyncOk()) {
			Map<Integer, MpTalker> mpTalkers = mpSession.getMpTalkers();
			Iterator<Entry<Integer, MpTalker>> iterator = mpTalkers.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Integer, MpTalker> next = iterator.next();
				if (!next.getKey().equals(mpTalker.getPort())) {
					MpTalker value = next.getValue();
					value.send(transformByteTypeB(value, packet, mpTalker));
				}
			}
		}
	}

	private byte[] transformByteTypeB(MpTalker mpTalker, byte[] packet, MpTalker mpTalkerFrom) {
		byte[] clone = packet.clone();
		try {
			clone = fixParser(mpTalker, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// byte[] clone = packet.clone();
		// byte[] packet = mpTalker.getPlayerPacket();
		if (clone.length < 4) {
			return null;
		}
		byte[] seqArray = mpTalker.getSequenceB();
		int size = clone.length - 3;
		byte[] dataTmp = new byte[size];
		dataTmp[0] = 1;
		dataTmp[1] = mpTalkerFrom.getClientIdx();// mpTalker.getClientIdx();
		dataTmp[2] = seqArray[0];
		dataTmp[3] = seqArray[1];
		int iDataTmp = 4;
		for (int i = 6; i < (clone.length - 1); i++) {
			dataTmp[iDataTmp++] = clone[i];
		}
		dataTmp[4] = (byte) 0xff;
		dataTmp[5] = (byte) 0xff;
		return dataTmp;
	}

	private byte[] fixParser(MpTalker mpTalker, byte[] fullPacket) {
		// if (mpTalker.getClientIdx() == 0) {
		byte[] timeArray = ByteBuffer.allocate(2).putShort((short) (mpTalker.getTimeDiff())).array();
		// System.out.println(UdpDebug.byteArrayToHexString(fullPacket));
		int subPacketStart = 10;
		int count = 0;
		byte nextType = (byte) 0xff;
		byte[] debugTmp = new byte[4];
		while (fullPacket[subPacketStart] != -1 && count < 4) {
			nextType = fullPacket[subPacketStart];
			if (nextType == 18 || nextType == 16 || nextType != 2) {
				if (nextType == 18) {
					debugTmp[0] = fullPacket[subPacketStart];
					debugTmp[1] = fullPacket[subPacketStart + 1];
					debugTmp[2] = fullPacket[subPacketStart + 2];
					debugTmp[3] = fullPacket[subPacketStart + 3];
					fullPacket[subPacketStart + 2] = timeArray[0];
					fullPacket[subPacketStart + 3] = timeArray[1];
					// System.out.println(UdpDebug.byteArrayToHexString(debugTmp));
				}
			}
			int supPacketLengh = fullPacket[subPacketStart + 1] + 2;
			subPacketStart = subPacketStart + supPacketLengh;
			// System.out.println("loop");
			count++;
		}
		// }
		return fullPacket;
	}

	private byte[] fixStatePosTime(MpTalker mpTalker, byte[] packet) {
		byte[] clone = packet.clone();
		byte[] timeArray = ByteBuffer.allocate(2).putShort((short) mpTalker.getTimeDiff()).array();
		if (mpTalker.getClientIdx() == 0) {
			int headerSize = 9;
			int subPktSizeTmp = 0;
			byte nextType = (byte) 0xff;
			int subPktSize = 0;
			byte[] packetTmp = new byte[2];

			while (true) {
				nextType = clone[subPktSizeTmp + headerSize + 1];
				if (nextType == -1 || (nextType != 18 && nextType != 16 && nextType != 2)) {
					System.out.println("peraeeee");
					packetTmp = new byte[2];
					packetTmp[0] = nextType;
					packetTmp[1] = (byte) subPktSize;
					System.out.println(UdpDebug.byteArrayToHexString(clone));
					System.out.println(UdpDebug.byteArrayToHexString(packetTmp));
					System.out.println("next-type: " + nextType);
					System.out.println("next-size: " + subPktSize);
					System.out.println("subPktSizeTmp: " + subPktSizeTmp);
					break;
				}
				// subPktSize = clone[subPktSizeTmp + headerSize + 2];
				// debug
				// packetTmp = new byte[2];
				// packetTmp[0] = nextType;
				// packetTmp[1] = (byte) subPktSize;
				// System.out.println(UdpDebug.byteArrayToHexString(packetTmp));
				// fim debug
				subPktSizeTmp = clone[subPktSizeTmp + headerSize + 2];
				// headerSize = -50;
				// subPktSizeTmp = subPktSizeTmp + subPktSize;
				subPktSizeTmp = subPktSizeTmp + 2;
				// break;
			}

			if (clone[10] == (byte) 0x12) {
				clone[12] = timeArray[0];
				clone[13] = timeArray[1];
			}
			// System.out.println("");
		}
		return clone;
	}

	private boolean isPlayerInfoPacket(ByteBuf buf) {
		return buf.getByte(0) == (byte) 0x01;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		System.err.println(cause.getMessage());
	}

	public static void main(String[] args) {
		byte[] fullPacket = UdpDebug.hexStringToByteArray(
				"01:00:00:6e:00:07:00:05:ff:ff:10:45:00:01:00:00:00:09:00:00:00:a4:32:c1:1c:65:00:00:00:00:00:00:00:00:00:80:40:ac:fa:18:00:a0:56:20:1d:bc:fa:18:00:ac:a0:0f:1d:90:18:16:1d:80:41:68:00:00:00:00:00:d8:fa:18:00:30:3d:69:00:80:41:68:00:a4:e4:7c:22:12:1a:a4:e5:98:08:72:36:d0:e5:9b:e9:c4:25:89:c4:1f:3e:fb:f9:d3:96:96:96:9b:34:08:1f:ff:aa:56:c9:3c:ff:");
		// byte[] fullPacket = UdpDebug.hexStringToByteArray(
		// "01:00:00:27:00:03:00:00:ff:ff:12:1a:75:50:98:08:73:de:d1:a5:97:49:c4:25:89:c4:1f:1e:fb:f9:d3:96:96:96:9a:fc:00:1f:ff:9a:12:33:81:ff:");
		int subPacketStart = 10;
		int count = 0;
		byte nextType = (byte) 0xff;
		while (fullPacket[subPacketStart] != -1 && count < 4) {
			int supPacketLengh = fullPacket[subPacketStart + 1] + 2;
			subPacketStart = subPacketStart + supPacketLengh;
			nextType = fullPacket[subPacketStart];
			if (nextType == 18 || nextType == 16 || nextType != 2) {
				if (nextType == 18) {
					System.out.println("eh 12");
				}
			}
			System.out.println("loop");
			count++;
		}
	}

}
