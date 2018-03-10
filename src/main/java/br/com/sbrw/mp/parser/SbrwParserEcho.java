package br.com.sbrw.mp.parser;

import java.nio.ByteBuffer;

public class SbrwParserEcho implements IParser {

	// full packet
	// 01:00:00:73:00:01:ff:ff:ff:ff:02:4a:00:50:4c:41:59:45:52:31:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:64:00:00:00:00:00:00:00:72:67:90:a3:e2:ba:08:00:21:00:00:00:fa:91:32:00:c0:2f:92:22:50:03:00:00:00:b0:79:e6:cf:ee:1e:9c:fb:12:1a:ba:ef:98:08:73:de:d1:a5:97:49:c4:25:89:c4:1f:1e:fb:f1:d3:96:96:96:9a:fc:00:1f:ff:b0:8d:c3:30:ff:

	// 01:00:00:73:00:01:ff:ff:ff:ff:
	private byte[] header;

	// 02:4a:00:50:4c:41:59:45:52:31:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:64:00:00:00:00:00:00:00:72:67:90:a3:e2:ba:08:00:21:00:00:00:fa:91:32:00:c0:2f:92:22:50:03:00:00:00:b0:79:e6:cf:ee:1e:9c:fb:
	private byte[] playerInfo;

	// 12:1a:ba:ef:98:08:73:de:d1:a5:97:49:c4:25:89:c4:1f:1e:fb:f1:d3:96:96:96:9a:fc:00:1f:
	private byte[] carState;

	// b0:8d:c3:30:
	private byte[] crc;
	
	private static final byte[] CRC_BYTES = {0x01, 0x02, 0x03, 0x04};

	public void parseInputData(byte[] inputData) {
		byte[] fullPacket = inputData.clone();
		header = new byte[10];
		System.arraycopy(fullPacket, 0, header, 0, 10);
		crc = new byte[4];
		System.arraycopy(fullPacket, fullPacket.length - 5, crc, 0, 4);
		int subPacketStart = 10;
		int count = 0;
		while (fullPacket[subPacketStart] != -1 && count < 4) {
			int supPacketLengh;
			supPacketLengh = fullPacket[subPacketStart + 1] + 2;
			if (fullPacket[subPacketStart] == 0x02) {
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
	}

	public byte[] getHeader() {
		return header;
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

	@Override
	public boolean isOk() {
		if (playerInfo == null || carState == null) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isCarStateOk() {
		if (carState == null) {
			return false;
		}
		return true;
	}

	@Override
	public byte[] getPlayerPacket(long timeDiff) {
		if (isOk()) {
			byte[] statePosPacket = getStatePosPacket(timeDiff);
			int bufferSize = header.length + playerInfo.length + statePosPacket.length + CRC_BYTES.length;
			ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
			byteBuffer.put(header);
			byteBuffer.put(playerInfo);
			byteBuffer.put(statePosPacket);
			byteBuffer.put(CRC_BYTES);
			byte[] array = byteBuffer.array();
			statePosPacket = null;
			return array;
		}
		return null;
	}
	
	@Override
	public byte[] getCarStatePacket(long timeDiff) {
		if (isOk()) {
			byte[] statePosPacket = getStatePosPacket(timeDiff);
			int bufferSize = header.length + statePosPacket.length + CRC_BYTES.length;
			ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
			byteBuffer.put(header);
			byteBuffer.put(carState);
			byteBuffer.put(CRC_BYTES);
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

	public String getName() {
		if (isOk()) {
			byte[] playerPacket = getPlayerPacket(50000L);
			byte[] playerName = new byte[15];
			System.arraycopy(playerPacket, 39, playerName, 0, 15);
			String playerNameStr = new String(playerName).trim();
			playerPacket = null;
			playerName = null;
			return playerNameStr;
		}
		return null;
	}

	// 01:00:00:27:00:00:ff:ff:ff:ff:12:1a:ba:ce:98:08:73:de:d1:a5:97:49:c4:25:89:c4:1f:1e:fb:f1:d3:96:96:96:9a:fc:00:1f:ff:83:59:69:89:ff:
	// 01:00:00:73:00:01:ff:ff:ff:ff:02:4a:00:50:4c:41:59:45:52:31:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:64:00:00:00:00:00:00:00:72:67:90:a3:e2:ba:08:00:21:00:00:00:fa:91:32:00:c0:2f:92:22:50:03:00:00:00:b0:79:e6:cf:ee:1e:9c:fb:12:1a:ba:ef:98:08:73:de:d1:a5:97:49:c4:25:89:c4:1f:1e:fb:f1:d3:96:96:96:9a:fc:00:1f:ff:b0:8d:c3:30:ff:

}
