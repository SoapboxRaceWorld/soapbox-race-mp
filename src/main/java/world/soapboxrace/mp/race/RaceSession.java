package world.soapboxrace.mp.race;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RaceSession
{
    private final int sessionID;

    private final int timeBase;

    private final List<Racer> racers;

    private final Logger logger;

    public RaceSession(int sessionID, int timeBase)
    {
        this.sessionID = sessionID;
        this.timeBase = timeBase;
        this.racers = new ArrayList<>();
        this.logger = LoggerFactory.getLogger(String.format("session-%d", sessionID));
    }

    public int getSessionID()
    {
        return sessionID;
    }

    public int getTimeBase()
    {
        return timeBase;
    }

    public List<Racer> getRacers()
    {
        return racers;
    }

    public void addRacer(Racer racer)
    {
        racers.add(racer);
        logger.debug("Added racer {}", racer.getClientIndex());
    }

    public boolean allPlayersSyncReady()
    {
        return racers.stream().allMatch(Racer::isSyncReady);
    }

    public boolean allPlayersSyncStartReady()
    {
        return racers.stream().allMatch(Racer::isSyncStartReady);
    }

    public boolean allPlayersOK()
    {
        return racers.stream().allMatch(Racer::isParserOK);
    }
    
    public boolean allPlayersInfoOK()
    {
        return racers.stream().allMatch(Racer::isInfoOK);
    }
}
