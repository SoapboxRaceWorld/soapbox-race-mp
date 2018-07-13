package world.soapboxrace.mp.race;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
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

    private RacerStatus status;

    private final ChannelHandlerContext ctx;

    private final DatagramPacket firstPacket;

    private final ClientSyncStart syncStart;

    private final SbrwParser parser;

    private final int cliHelloTime;

    private boolean isSyncStartReady, isSyncReady, isParserOK, isInfoOK;

    private short sequenceA, sequenceB, syncSequence, preInfoSequence;

    public Racer(int sessionID, byte clientIndex, ChannelHandlerContext ctx, DatagramPacket firstPacket, int cliHelloTime)
    {
        this.sessionID = sessionID;
        this.clientIndex = clientIndex;
        this.ctx = ctx;
        this.firstPacket = firstPacket;
        this.cliHelloTime = cliHelloTime;
        this.timeStarted = System.currentTimeMillis();
        this.syncStart = new ClientSyncStart();
        this.parser = new SbrwParser();
        this.isParserOK =
                this.isInfoOK =
                        this.isSyncReady =
                                this.isSyncStartReady = false;
        this.syncSequence = 1;
        this.status = RacerStatus.WAITING_HELLO;
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
        final ByteBuf buffer = Unpooled.copiedBuffer(data);
        ctx.writeAndFlush(new DatagramPacket(buffer, firstPacket.sender()));
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

    public int getCliHelloTime()
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

    public boolean isInfoOK()
    {
        return isInfoOK;
    }

    public void parsePacket(byte[] data)
    {
        this.parser.parse(data);
        this.isParserOK = this.parser.isOk();
        this.isInfoOK = this.parser.isPlayerInfoOk();
    }

    public byte[] getPlayerPacket()
    {
        return this.parser.getPlayerPacket(getTimeDiff());
    }

    public byte[] getCarStatePacket()
    {
        return this.parser.getCarStatePacket(getTimeDiff());
    }

    public byte[] getPlayerInfoPacket()
    {
        return this.parser.getPlayerInfoPacket(getTimeDiff());
    }

    public short getPreInfoSequence()
    {
        return preInfoSequence;
    }

    public void incrementPreInfoSequence()
    {
        preInfoSequence++;
    }

    public RacerStatus getStatus()
    {
        return status;
    }

    public void setStatus(RacerStatus status)
    {
        this.status = status;
    }
}
