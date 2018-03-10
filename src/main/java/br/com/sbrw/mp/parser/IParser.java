package br.com.sbrw.mp.parser;

public interface IParser {
	public void parseInputData(byte[] packet);

	public boolean isOk();

	public byte[] getPlayerPacket(long timeDiff);

	boolean isCarStateOk();

	byte[] getCarStatePacket(long timeDiff);
}
