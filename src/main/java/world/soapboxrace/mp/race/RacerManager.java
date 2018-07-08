package world.soapboxrace.mp.race;

import java.util.HashMap;
import java.util.Map;

public class RacerManager
{
    private static Map<Integer, Racer> racers = new HashMap<>();

    public static Racer get(Integer port)
    {
        return racers.get(port);
    }

    public static Racer put(Integer port, Racer racer)
    {
        racers.put(port, racer);

        return racers.get(port);
    }
}
