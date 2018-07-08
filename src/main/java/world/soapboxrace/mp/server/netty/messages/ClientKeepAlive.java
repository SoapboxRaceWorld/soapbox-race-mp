package world.soapboxrace.mp.server.netty.messages;

import io.netty.buffer.ByteBuf;
import world.soapboxrace.mp.server.netty.UdpMessage;

import java.nio.ByteBuffer;

// keepAlive sync18
public class ClientKeepAlive implements UdpMessage
{
    public short counter; // not the same as server count
    public short time; // seems to drift ahead of the server time, not sure why
    public short cliHelloTime; // roughly looks like origHelloTime*2 + something
    public short unknownCounter;
    public short handshakeSync; // shifts by unknownCounter in server response; eg uc 4 = 1110 1111 1111 1111, uc 5 = 1111 0111, etc
    
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
