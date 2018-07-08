package world.soapboxrace.mp.race;

import java.util.HashMap;
import java.util.Map;

public class RaceSessionManager
{
    private static Map<Integer, RaceSession> sessions = new HashMap<>();

    public static RaceSession add(Integer sessionId, RaceSession session)
    {
        sessions.put(sessionId, session);
        
        return sessions.get(sessionId);
    }

    public static RaceSession get(Integer sessionId)
    {
        return sessions.get(sessionId);
    }

    public static RaceSession get(Racer racer)
    {
        return get(racer.getSessionID());
    }
}
