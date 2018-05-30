package world.soapboxrace.mp.race;

import java.util.HashMap;
import java.util.Map;

import io.netty.channel.socket.DatagramPacket;

public class MpClients
{
	private static HashMap<Integer, MpClient> mpClients = new HashMap<>();

	public static void put(MpClient mpClient) {
		mpClients.put(mpClient.getPort(), mpClient);
	}

	public static MpClient get(DatagramPacket datagramPacket) {
		return mpClients.get(datagramPacket.sender().getPort());
	}

	public static Map<Integer, MpClient> getMpClients() {
		return mpClients;
	}
}