package world.soapboxrace.mp.race;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import world.soapboxrace.mp.server.IParser;
import world.soapboxrace.mp.server.SbrwParser;

import java.nio.ByteBuffer;
import java.util.Date;

public class MpClient
{
    // The Netty context associated with the client
    private ChannelHandlerContext ctx;

    // The first packet from the client
    private DatagramPacket datagramPacket;

    // The packet parser
    private IParser parser;

    private byte[] cliTime;
    private byte[] syncHello;

    private byte clientId;

    private byte totalPlayers;

    private int sessionId = 0;

    private long startedTime = new Date().getTime();

    private int sequenceA = 0, sequenceB = 0;

    private long helloTime;

    private boolean syncHelloOk = false;
    private boolean syncOk = false;

    private boolean preInfoOk = false;
    private boolean prePosOk = false;
    private boolean postInfoOk = false;

    public MpClient(
            ChannelHandlerContext ctx,
            DatagramPacket packet,
            int sessionId,
            byte[] cliTime,
            byte clientId,
            byte totalPlayers
    )
    {
        this.ctx = ctx;
        this.datagramPacket = packet;
        this.sessionId = sessionId;
        this.cliTime = cliTime;
        this.clientId = clientId;
        this.totalPlayers = totalPlayers;
        this.parser = new SbrwParser();
    }

    public int getSessionId()
    {
        return sessionId;
    }

    public int getPort()
    {
        return datagramPacket.sender().getPort();
    }

    public void send(byte[] data)
    {
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(data), datagramPacket.sender()));
    }

    public boolean isSyncHelloOk()
    {
        return syncHelloOk;
    }

    public void setSyncHelloOk(boolean helloSyncOk)
    {
        this.syncHelloOk = helloSyncOk;
    }

    public boolean isSyncOk()
    {
        return syncOk;
    }

    public void setSyncOk(boolean syncOk)
    {
        this.syncOk = syncOk;
    }

    public boolean isPreInfoOk()
    {
        return preInfoOk;
    }

    public void setPreInfoOk(boolean preInfoOk)
    {
        this.preInfoOk = preInfoOk;
    }

    public boolean isPrePosOk()
    {
        return prePosOk;
    }

    public void setPrePosOk(boolean prePosOk)
    {
        this.prePosOk = prePosOk;
    }

    public boolean isPostInfoOk()
    {
        return postInfoOk;
    }

    public void setPostInfoOk(boolean postInfoOk)
    {
        this.postInfoOk = postInfoOk;
    }

    public long getTimeDiff()
    {
        long now = new Date().getTime();
        MpSession mpSession = MpSessions.get(this);
        return mpSession.getCliTimeStart() + (now - startedTime);
    }

    public byte getClientId()
    {
        return clientId;
    }

    public byte getTotalPlayers()
    {
        return totalPlayers;
    }

    public byte[] getSequenceA()
    {
        return ByteBuffer.allocate(2).putShort((short) sequenceA++).array();
    }

    public byte[] getSequenceB()
    {
        return ByteBuffer.allocate(2).putShort((short) sequenceA++).array();
    }

    public long getHelloTime()
    {
        return helloTime;
    }

    public void setHelloTime(long helloTime)
    {
        this.helloTime = helloTime;
    }

    public byte[] getSyncHello()
    {
        return syncHello;
    }

    public void setSyncHello(byte[] syncHello)
    {
        this.syncHello = syncHello;
    }

    public void parsePacket(byte[] packet)
    {
        this.parser.parse(packet);
        preInfoOk = this.parser.isOk();
    }

    public byte[] getPlayerPacket()
    {
        return parser.getPlayerPacket(getTimeDiff());
    }
}
