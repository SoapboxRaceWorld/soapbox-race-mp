package br.com.sbrw.mp.protocol;

import java.util.HashMap;

public class MpSessions {

	private static HashMap<Integer, MpSession> sessions = new HashMap<>();

	public static void put(MpSession mpSession) {
		sessions.put(mpSession.getSessionId(), mpSession);
	}

	public static MpSession get(MpTalker mpTalker) {
		return sessions.get(mpTalker.getSessionId());
	}
}
