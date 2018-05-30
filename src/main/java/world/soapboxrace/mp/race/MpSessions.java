package world.soapboxrace.mp.race;

import java.util.HashMap;

public class MpSessions
{

    private static HashMap<Integer, MpSession> sessions = new HashMap<>();

    public static void put(MpSession mpSession)
    {
        sessions.put(mpSession.getSessionId(), mpSession);
    }

    public static MpSession get(MpClient mpClient)
    {
        return sessions.get(mpClient.getSessionId());
    }
}