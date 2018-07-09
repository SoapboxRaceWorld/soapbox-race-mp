package world.soapboxrace.mp.race;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import world.soapboxrace.mp.server.SbrwParser;
import world.soapboxrace.mp.server.netty.messages.ClientSyncStart;

import java.nio.ByteBuffer;

public class Racer
{
    private final int sessionID;

    private final byte clientIndex;

    private final long timeStarted;

    private final RacerInfo racerInfo;

    private final ChannelHandlerContext ctx;

    private final DatagramPacket firstPacket;

    private final ClientSyncStart syncStart;

    private final SbrwParser parser;

    private final short cliHelloTime;

    private boolean isSyncStartReady, isSyncReady, isParserOK;

    private short sequenceA, sequenceB, syncSequence;

    private byte[] playerInfoPacket;

    public Racer(int sessionID, byte clientIndex, ChannelHandlerContext ctx, DatagramPacket firstPacket, short cliHelloTime)
    {
        this.sessionID = sessionID;
        this.clientIndex = clientIndex;
        this.ctx = ctx;
        this.firstPacket = firstPacket;
        this.cliHelloTime = cliHelloTime;
        this.timeStarted = System.currentTimeMillis();
        this.racerInfo = new RacerInfo();
        this.syncStart = new ClientSyncStart();
        this.parser = new SbrwParser();
        this.isSyncStartReady = false;
        this.isSyncReady = false;
        this.isParserOK = false;
        this.syncSequence = 1;
    }

    public int getSessionID()
    {
        return sessionID;
    }

    public byte getClientIndex()
    {
        return clientIndex;
    }

    public long getTimeStarted()
    {
        return timeStarted;
    }

    public RacerInfo getRacerInfo()
    {
        return racerInfo;
    }

    public long getTimeDiff()
    {
        return RaceSessionManager.get(sessionID).getTimeBase() + (System.currentTimeMillis() - timeStarted);
    }

    public void send(ByteBuffer buffer)
    {
        send(buffer.array());
    }

    public void send(byte[] data)
    {
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(data), firstPacket.sender()));
    }

    public ClientSyncStart getSyncStart()
    {
        return syncStart;
    }

    public boolean isSyncStartReady()
    {
        return isSyncStartReady;
    }

    public void setSyncStartReady(boolean syncStartReady)
    {
        isSyncStartReady = syncStartReady;
    }

    public boolean isSyncReady()
    {
        return isSyncReady;
    }

    public void setSyncReady(boolean syncReady)
    {
        isSyncReady = syncReady;
    }

    public short getCliHelloTime()
    {
        return cliHelloTime;
    }

    public short getSequenceA()
    {
        return sequenceA++;
    }

    public short getSequenceB()
    {
        return sequenceB++;
    }

    public short getSyncSequence()
    {
        return syncSequence++;
    }

    public boolean isParserOK()
    {
        return isParserOK;
    }

    public void parsePacket(byte[] data)
    {
        this.parser.parse(data);
        this.isParserOK = this.parser.isOk();
    }

    public byte[] getPlayerPacket()
    {
        return this.parser.getPlayerPacket(getTimeDiff());
    }
}
