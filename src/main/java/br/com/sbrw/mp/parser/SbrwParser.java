package br.com.sbrw.mp.parser;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.BitSet;

public class SbrwParser {

	// full packet
	// 00:01:07:02:e3:32:1d:1e:ff:ff:ff:ff:43:4c:29:c9:00:22:00:00:63:68:61:6e:6e:65:6c:2e:45:4e:5f:5f:32:00:00:01:67:c3:7b:d0:fc:33:00:63:ef:76:00:00:00:00:00:c0:01:41:00:4e:49:4c:5a:41:4f:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:01:00:00:00:00:00:00:00:65:00:00:00:00:00:00:00:b0:79:e6:cf:ee:1e:9c:fb:1b:9f:c2:dc:00:00:00:00:12:1a:00:46:98:09:0c:fd:c7:94:42:73:88:4b:13:88:3f:9c:48:57:b1:2d:2d:2d:34:50:00:7f:ff:3e:6a:3d:2a:

	// 00:01:07:02:e3:32:1d:1e:ff:ff:ff:ff:43:4c:29:c9:
	private byte[] header;

	// 00:22:00:00:63:68:61:6e:6e:65:6c:2e:45:4e:5f:5f:32:00:00:01:67:c3:7b:d0:fc:33:00:63:ef:76:00:00:00:00:00:c0:
	private byte[] channelInfo;

	// 01:41:00:4e:49:4c:5a:41:4f:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:01:00:00:00:00:00:00:00:65:00:00:00:00:00:00:00:b0:79:e6:cf:ee:1e:9c:fb:1b:9f:c2:dc:00:00:00:00:
	private byte[] playerInfo;

	// 12:1a:00:46:98:09:0c:fd:c7:94:42:73:88:4b:13:88:3f:9c:48:57:b1:2d:2d:2d:34:50:00:7f:ff:
	private byte[] carState;

	// 3e:6a:3d:2a:
	private byte[] crc;

	private int lastSeq = 0;
	private int lastTime = 0;

	public SbrwParser(byte[] inputData) {
		parseInputData(inputData);
	}

	public boolean parseInputData(byte[] inputData) {
		byte[] fullPacket = inputData.clone();
		boolean good2go = false;

		try {
			header = new byte[16];
			System.arraycopy(fullPacket, 0, header, 0, 16);
			crc = new byte[4];
			System.arraycopy(fullPacket, fullPacket.length - 4, crc, 0, 4);

			byte[] seqBytes = { header[0], header[1] };

			byte[] timeBytes = { header[3], header[4] };

			int seq = new BigInteger(seqBytes).intValue(), time = new BigInteger(timeBytes).intValue();
			if (seq == -1) {
				System.out.println("Reached seq limit, resetting");
				seq = 0;
				lastSeq = 0;
			}

			if (seq < lastSeq && time < lastTime) {
				System.err.println("[packet] out-of-order seq/time: " + seq + "/" + time);
			} else {
				good2go = true;
			}

			this.lastSeq = seq;
			this.lastTime = time;

			int subPacketStart = 16;
			int count = 0;
			while (fullPacket[subPacketStart] != -1 && count < 4) {
				int supPacketLengh = fullPacket[subPacketStart + 1] + 2;
				if (fullPacket[subPacketStart] == 0x00) {
					channelInfo = new byte[supPacketLengh];
					System.arraycopy(fullPacket, subPacketStart, channelInfo, 0, supPacketLengh);
					subPacketStart = subPacketStart + supPacketLengh;
				}
				supPacketLengh = fullPacket[subPacketStart + 1] + 2;
				if (fullPacket[subPacketStart] == 0x01) {
					playerInfo = new byte[supPacketLengh];
					System.arraycopy(fullPacket, subPacketStart, playerInfo, 0, supPacketLengh);
					subPacketStart = subPacketStart + supPacketLengh;
				}
				supPacketLengh = fullPacket[subPacketStart + 1] + 2;
				if (fullPacket[subPacketStart] == 0x12) {
					carState = new byte[supPacketLengh];
					System.arraycopy(fullPacket, subPacketStart, carState, 0, supPacketLengh);
					subPacketStart = subPacketStart + supPacketLengh;
				}
				count++;
			}
			fullPacket = null;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			// System.err.println(UdpDebug.byteArrayToHexString(inputData));
		}

		return good2go;
	}

	public byte[] getHeader() {
		return header;
	}

	public byte[] getChannelInfo() {
		return channelInfo;
	}

	public byte[] getPlayerInfo() {
		return playerInfo;
	}

	public byte[] getCarState() {
		return carState;
	}

	public byte[] getCrc() {
		return crc;
	}

	public int getTime() {
		if (isOk()) {
			return new BigInteger(new byte[] { header[3], header[4] }).intValue();
		}

		return -1;
	}

	public boolean isOk() {
		if (channelInfo == null || playerInfo == null || carState == null) {
			return false;
		}
		return true;
	}

	public byte[] getPlayerPacket(long timeDiff) {
		if (isOk()) {
			byte[] statePosPacket = getStatePosPacket(timeDiff);
			int bufferSize = channelInfo.length + playerInfo.length + statePosPacket.length;
			ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
			byteBuffer.put(channelInfo);
			byteBuffer.put(playerInfo);
			byteBuffer.put(statePosPacket);
			byte[] array = byteBuffer.array();
			statePosPacket = null;
			return array;
		}
		return null;
	}

	public byte[] getStatePosPacket(long timeDiff) {
		if (isOk()) {
			byte[] clone = carState.clone();
			byte[] timeDiffBytes = ByteBuffer.allocate(2).putShort((short) timeDiff).array();
			clone[2] = timeDiffBytes[0];
			clone[3] = timeDiffBytes[1];
			int bufferSize = clone.length;
			ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
			byteBuffer.put(clone);
			byte[] array = byteBuffer.array();
			clone = null;
			timeDiffBytes = null;
			return array;
		}
		return null;
	}

	public String getChannel() {
		if (isOk()) {
			byte[] channelName = new byte[15];
			System.arraycopy(channelInfo, 2, channelName, 0, 15);
			String channelStr = new String(channelName).trim();
			channelName = null;
			return channelStr;
		}
		return null;
	}

	public String getName() {
		if (isOk()) {
			// byte[] playerPacket = getPlayerPacket(50000L);
			byte[] playerName = new byte[15];
			System.arraycopy(playerInfo, 3, playerName, 0, 15);
			String playerNameStr = new String(playerName).trim();
			playerName = null;
			return playerNameStr;
		}
		return null;
	}

	public long getPlayerId() {
		if (isOk()) {
			long personaId = 0;
			for (int i = 43; i <= 46 && playerInfo[i] != 0x00; i++) {
				personaId += (int) playerInfo[i];
			}
			return personaId;
		}
		return -1;
	}

	public int getXPos() {
		if (isOk()) {
			byte[] byteTmp = { carState[9], carState[10], carState[11] };
			return new BigInteger(cleanX(byteTmp)).intValue();
		}
		return -1;
	}

	public int getYPos() {
		if (isOk()) {
			byte[] byteTmp = { carState[5], carState[6], carState[7] };
			return new BigInteger(cleanY(byteTmp)).intValue();
		}
		return -1;
	}

	public int getZPos() {
		if (isOk()) {
			byte[] byteTmp = { carState[7], carState[8], carState[9] };
			return new BigInteger(cleanZ(byteTmp)).intValue();
		}
		return -1;
	}

	private byte[] cleanX(byte[] byteOrig) {
		byte[] byteTmp = byteOrig.clone();
		if (isLowerY()) {
			byteTmp[0] = (byte) (byteTmp[0] & 0x7f);
			byteTmp[2] = (byte) (byteTmp[2] & 0xe0);
			return shiftRight(byteTmp, 5);
		}
		byteTmp[0] = (byte) (byteTmp[0] & 0x3f);
		byteTmp[2] = (byte) (byteTmp[2] & 0xf0);
		return shiftRight(byteTmp, 4);
	}

	private byte[] cleanY(byte[] byteOrig) {
		byte[] byteTmp = byteOrig.clone();
		if (isLowerY()) {
			byteTmp[2] = (byte) (byteTmp[2] & 0xf8);
		} else {
			byteTmp[2] = (byte) (byteTmp[2] & 0xfc);
		}
		return shiftRight(byteTmp, 2);
	}

	private byte[] cleanZ(byte[] byteOrig) {
		byte[] byteTmp = byteOrig.clone();
		if (isLowerY()) {
			byteTmp[0] = (byte) (byteTmp[0] & 0x07);
			byteTmp[2] = (byte) (byteTmp[2] & 0x80);
			return shiftRight(byteTmp, 7);
		}
		byteTmp[0] = (byte) (byteTmp[0] & 0x03);
		byteTmp[2] = (byte) (byteTmp[2] & 0xc0);
		return shiftRight(byteTmp, 6);
	}

	private boolean isLowerY() {
		int intValue = new BigInteger(new byte[] { carState[5], carState[6] }).intValue();
		if (intValue <= 1941) {
			return true;// Y size (17 bits)
		}
		return false;// Y size (18 bits)
	}

	private byte[] shiftRight(byte[] byteOrig, int n) {
		byte bitMask = bitMask(8 - n);
		ByteBuffer allocate = ByteBuffer.allocate(byteOrig.length);
		for (int i = 0; i < byteOrig.length; i++) {
			byte byteTmp = (byte) (byteOrig[i] >> n & bitMask);
			if (i > 0) {
				byteTmp = (byte) ((byteOrig[i - 1] << (8 - n)) | byteTmp);
			}
			allocate.put(byteTmp);
		}
		return allocate.array();
	}

	private byte bitMask(int n) {
		BitSet bitSet = new BitSet();
		for (int i = 0; i < n; i++) {
			bitSet.set(i);
		}
		return bitSet.toByteArray()[0];
	}

}
