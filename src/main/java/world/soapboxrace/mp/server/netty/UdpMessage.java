package world.soapboxrace.mp.server.netty;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public interface UdpMessage
{
    void read(ByteBuf buf);
    
    void write(ByteBuffer buffer);
}
