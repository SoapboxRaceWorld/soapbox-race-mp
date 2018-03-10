package br.com.sbrw.mp.parser;

public class StrParser implements IParser {

	private String playerInfo;

	@Override
	public void parseInputData(byte[] packet) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("player: [");
		stringBuilder.append(new String(packet).trim());
		stringBuilder.append("]\n\n");
		playerInfo = stringBuilder.toString();
	}

	@Override
	public boolean isOk() {
		if (playerInfo != null && !playerInfo.isEmpty()) {
			return true;
		}
		return false;
	}

	@Override
	public byte[] getPlayerPacket(long timeDiff) {
		return playerInfo.getBytes();
	}

	@Override
	public boolean isCarStateOk() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] getCarStatePacket(long timeDiff) {
		// TODO Auto-generated method stub
		return null;
	}

}
