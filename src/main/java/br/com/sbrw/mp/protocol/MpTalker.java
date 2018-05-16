package br.com.sbrw.mp.protocol;

import java.nio.ByteBuffer;
import java.util.Date;

import br.com.sbrw.mp.parser.IParser;
import br.com.sbrw.mp.util.UdpDebug;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

public class MpTalker {

	private ChannelHandlerContext ctx;
	private DatagramPacket datagramPacket;
	private byte[] cliTime;
	private byte[] syncHello;
	private byte[] sync;
	private byte clientIdx;
	private byte totalPlayers;
	private IParser parser;
	private boolean playerInfoBeforeOk = false;
	private boolean playerStatePosBeforeOk = false;
	private boolean playerInfoAfterOk = false;
	private Integer sessionId = 0;
	private long startedTime = new Date().getTime();
	private int countA = -1;
	private int countB = 0;
	private boolean syncHelloOk = false;
	private boolean syncOk = false;
	private long cliHelloTime;

	public MpTalker(ChannelHandlerContext ctx, DatagramPacket datagramPacket, IParser parser, Integer sessionId, byte[] cliTime, byte clientIdx, byte totalPlayers) {
		this.ctx = ctx;
		this.datagramPacket = datagramPacket;
		this.parser = parser;
		this.sessionId = sessionId;
		this.cliTime = cliTime;
		this.clientIdx = clientIdx;
		this.totalPlayers = totalPlayers;
	}

	public Integer getSessionId() {
		return sessionId;
	}

	public Integer getPort() {
		return datagramPacket.sender().getPort();
	}

	public void send(byte[] packetData) {
		// if (getClientIdx() == 0) {
		// System.out.println("out: " + UdpDebug.byteArrayToHexString(packetData));
		// }
		ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(packetData), datagramPacket.sender()));
	}

	public void parsePlayerInfo(byte[] playerInfo) {
		if (parser != null) {
			parser.parseInputData(playerInfo);
			playerInfoBeforeOk = parser.isOk();
		}
	}

	public boolean isPlayerInfoBeforeOk() {
		return parser.isOk();
	}

	public boolean isCarStateInfoBeforeOk() {
		if (parser == null) {
			return false;
		}
		return parser.isCarStateOk();
	}

	public byte[] getCliTime() {
		return cliTime;
	}

	public long getTimeDiff() {
		long now = new Date().getTime();
		MpSession mpSession = MpSessions.get(this);
		return mpSession.getCliTimeStart() + (now - startedTime);
	}

	public byte[] getSequenceA() {
		return ByteBuffer.allocate(2).putShort((short) countA++).array();
	}

	public byte[] getSequenceB() {
		return ByteBuffer.allocate(2).putShort((short) countB++).array();
	}

	public boolean isSyncHelloOk() {
		return syncHelloOk;
	}

	public void setSyncHelloOk(boolean helloSyncOk) {
		this.syncHelloOk = helloSyncOk;
	}

	public boolean isSyncOk() {
		return syncOk;
	}

	public void setSyncOk(boolean syncOk) {
		this.syncOk = syncOk;
	}

	public byte[] getSyncHello() {
		return syncHello;
	}

	public void setSyncHello(byte[] syncHello) {
		this.syncHello = syncHello;
	}

	public byte[] getPlayerPacket() {
		return parser.getPlayerPacket(getTimeDiff());
	}

	public boolean isPlayerInfoAfterOk() {
		return playerInfoAfterOk;
	}

	public void setPlayerInfoAfterOk(boolean playerInfoAfterOk) {
		this.playerInfoAfterOk = playerInfoAfterOk;
	}

	public byte getClientIdx() {
		return clientIdx;
	}

	public byte[] getSync() {
		return sync;
	}

	public void setSync(byte[] sync) {
		this.sync = sync;
	}

	public long getCliHelloTime() {
		return cliHelloTime;
	}

	public void setCliHelloTime(long cliHelloTime) {
		this.cliHelloTime = cliHelloTime;
	}

	public boolean isPlayerStatePosBeforeOk() {
		return playerStatePosBeforeOk;
	}

	public void setPlayerStatePosBeforeOk(boolean playerStatePosBeforeOk) {
		this.playerStatePosBeforeOk = playerStatePosBeforeOk;
	}

	public byte[] getCarStatePacket() {
		return parser.getCarStatePacket(getTimeDiff());
	}

	public byte getTotalPlayers()
	{
		return totalPlayers;
	}
}
