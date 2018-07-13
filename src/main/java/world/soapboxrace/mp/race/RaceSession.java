package world.soapboxrace.mp.race;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.soapboxrace.mp.util.ConcurrencyUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RaceSession
{
    private final int sessionID;

    private final int timeBase;

    private final List<Racer> racers;

    private final Logger logger;

    private SessionStatus status;

    public RaceSession(int sessionID, int timeBase)
    {
        this.sessionID = sessionID;
        this.timeBase = timeBase;
        this.racers = new ArrayList<>();
        this.logger = LoggerFactory.getLogger(String.format("session-%d", sessionID));
        this.status = SessionStatus.LOADING;
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

    public boolean allPlayersInState(RacerStatus status)
    {
        return racers.stream().allMatch(r -> r.getStatus() == status);
    }

    public boolean allPlayersOK()
    {
        return racers.stream().allMatch(Racer::isParserOK);
    }

    public boolean allPlayersInfoOK()
    {
        return racers.stream().allMatch(Racer::isInfoOK);
    }

    public SessionStatus getStatus()
    {
        return status;
    }

    public void startBroadcasting()
    {
        if (status == SessionStatus.BROADCASTING)
        {
            logger.error("Tried to start broadcasting while we are already broadcasting");
            return;
        }

        if (!allPlayersInState(RacerStatus.WAITING_ID_AFTER_SYNC))
        {
            logger.error("Cannot broadcast yet. States={}", racers.stream().map(Racer::getStatus).map(RacerStatus::toString).collect(Collectors.joining(",")));
            return;
        }

        this.status = SessionStatus.BROADCASTING;

        ConcurrencyUtil.EXECUTOR.scheduleAtFixedRate(new SessionBroadcaster(), 60, 60, TimeUnit.MILLISECONDS);
        logger.debug("Started broadcasting!");
//        new SessionBroadcaster().start();
    }

    private class SessionBroadcaster implements Runnable
    {
        @Override
        public void run()
        {
            for (Racer racer : racers)
            {
                for (Racer sessionRacer : racers)
                {
                    if (sessionRacer.getClientIndex() != racer.getClientIndex())
                    {
                        racer.send(transformPacket(sessionRacer.getCarStatePacket(), sessionRacer, racer));
                    }
                }
            }
        }

        private byte[] transformPacket(byte[] packet, Racer racerFrom, Racer racerTo)
        {
            byte[] timeBytes = ByteBuffer.allocate(2).putShort((short) racerTo.getTimeDiff()).array();
            byte[] sequenceBytes = ByteBuffer.allocate(2).putShort(racerTo.getSequenceB()).array();

            ByteBuffer buffer = ByteBuffer.allocate(packet.length - 3);
            buffer.put((byte) 0x01);
            buffer.put(sequenceBytes);
            buffer.put(racerFrom.getClientIndex());

            for (int i = 6; i < packet.length - 1; i++)
            {
                if (packet[i] == 0x12 && packet[i + 1] >= 0x1a)
                {
                    logger.debug("Fixing time @ {}", i + 2);

                    packet[i + 2] = timeBytes[0];
                    packet[i + 3] = timeBytes[1];
                }
            }

            for (int i = 6; i < packet.length - 1; i++)
            {
                buffer.put(packet[i]);
            }

            return buffer.array();
        }
    }
}
