package world.soapboxrace.mp.server.netty.messages;

import io.netty.buffer.ByteBuf;
import world.soapboxrace.mp.server.netty.UdpMessage;

import java.nio.ByteBuffer;

// session sync22
public class ClientSync implements UdpMessage
{
    public short counter;
    public short time; // does not match server time
    public short cliHelloTime; // does not match hello time in first packet
    public short unknownCounter;
    public short handshakeSync; // set unknownCounter bit to 0
    
    @Override
    public void read(ByteBuf buf)
    {
        buf.skipBytes(1);
        counter = (short) buf.readUnsignedShort();
        buf.skipBytes(2);
        time = (short) buf.readUnsignedShort();
        cliHelloTime = (short) buf.readUnsignedShort();
        unknownCounter = (short) buf.readUnsignedShort();
        handshakeSync = (short) buf.readUnsignedShort();
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        throw new UnsupportedOperationException();
    }
}
