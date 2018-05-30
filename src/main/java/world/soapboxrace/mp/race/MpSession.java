package world.soapboxrace.mp.race;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class MpSession
{
    private Map<Integer, MpClient> clients = new HashMap<>();

    private Integer sessionId;
    private Integer maxUsers;
    private byte[] syncPacket;
    private byte[] keepAlivePacket;
    private long cliTimeStart;

    public MpSession(MpClient mpClient, Integer maxUsers)
    {
        this.maxUsers = maxUsers;
        sessionId = mpClient.getSessionId();
        put(mpClient);
    }

    public Integer getSessionId()
    {
        return sessionId;
    }

    public void put(MpClient mpTalker)
    {
        clients.put(mpTalker.getPort(), mpTalker);
    }

    public Integer getMaxUsers()
    {
        return maxUsers;
    }

    public boolean isFull()
    {
        return this.clients.size() == maxUsers;
    }

    public Map<Integer, MpClient> getClients()
    {
        return clients;
    }

    public boolean isAllSyncHelloOk()
    {
        Map<Integer, MpClient> mpClientsTmp = getClients();
        Iterator<Entry<Integer, MpClient>> iterator = mpClientsTmp.entrySet().iterator();
        while (iterator.hasNext())
        {
            Entry<Integer, MpClient> next = iterator.next();
            MpClient value = next.getValue();
            if (!value.isSyncHelloOk())
            {
                return false;
            }
        }
        return true;
    }

    public boolean isAllPlayerInfoBeforeOk()
    {
        Map<Integer, MpClient> mpClientsTmp = getClients();
        Iterator<Entry<Integer, MpClient>> iterator = mpClientsTmp.entrySet().iterator();
        while (iterator.hasNext())
        {
            Entry<Integer, MpClient> next = iterator.next();
            MpClient value = next.getValue();
            if (!value.isPreInfoOk())
            {
                return false;
            }
        }
        return true;
    }

    public boolean isAllCarStateBeforeOk()
    {
        Map<Integer, MpClient> mpClientsTmp = getClients();
        Iterator<Entry<Integer, MpClient>> iterator = mpClientsTmp.entrySet().iterator();
        while (iterator.hasNext())
        {
            Entry<Integer, MpClient> next = iterator.next();
            MpClient value = next.getValue();
            if (!value.isPrePosOk())
            {
                return false;
            }
        }
        return true;
    }

    public boolean isAllPlayerInfoAfterOk()
    {
        Map<Integer, MpClient> mpClientsTmp = getClients();
        Iterator<Entry<Integer, MpClient>> iterator = mpClientsTmp.entrySet().iterator();
        while (iterator.hasNext())
        {
            Entry<Integer, MpClient> next = iterator.next();
            MpClient value = next.getValue();
            if (!value.isPostInfoOk())
            {
                return false;
            }
        }
        return true;
    }

    public boolean isAllSyncOk()
    {
        Map<Integer, MpClient> mpClientsTmp = getClients();
        Iterator<Entry<Integer, MpClient>> iterator = mpClientsTmp.entrySet().iterator();
        while (iterator.hasNext())
        {
            Entry<Integer, MpClient> next = iterator.next();
            MpClient value = next.getValue();
            if (!value.isSyncOk())
            {
                return false;
            }
        }
        return true;
    }

    public byte[] getSyncPacket()
    {
        return syncPacket;
    }

    public void setSyncPacket(byte[] syncPacket)
    {
        this.syncPacket = syncPacket;
    }

    public long getCliTimeStart()
    {
        return cliTimeStart;
    }

    public void setCliTimeStart(long cliTimeStart)
    {
        this.cliTimeStart = cliTimeStart;
    }

    public byte[] getKeepAlivePacket()
    {
        return keepAlivePacket;
    }

    public void setKeepAlivePacket(byte[] keepAlivePacket)
    {
        this.keepAlivePacket = keepAlivePacket;
    }
}
