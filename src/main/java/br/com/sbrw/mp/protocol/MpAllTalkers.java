package br.com.sbrw.mp.protocol;

import java.util.HashMap;
import java.util.Map;

import io.netty.channel.socket.DatagramPacket;

public class MpAllTalkers {

	private static HashMap<Integer, MpTalker> mpTalkers = new HashMap<>();

	public static void put(MpTalker mpTalker) {
		mpTalkers.put(mpTalker.getPort(), mpTalker);
	}

	public static MpTalker get(DatagramPacket datagramPacket) {
		return mpTalkers.get(datagramPacket.sender().getPort());
	}

	public static Map<Integer, MpTalker> getMpTalkers() {
		return mpTalkers;
	}
}
