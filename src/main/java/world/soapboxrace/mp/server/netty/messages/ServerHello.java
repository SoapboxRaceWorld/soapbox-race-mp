package world.soapboxrace.mp.server.netty.messages;

import io.netty.buffer.ByteBuf;
import world.soapboxrace.mp.server.netty.UdpMessage;

import java.nio.ByteBuffer;

public class ServerHello implements UdpMessage
{
    public short time;
    public int helloTime;
    
    @Override
    public void read(ByteBuf buf)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        // mp type
        buffer.put((byte) 0x00);
        
        // counter
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        
        // srv type
        buffer.put((byte) 0x01);
        
        byte[] timeBytes = ByteBuffer.allocate(2).putShort(time).array();
        byte[] helloTimeBytes = ByteBuffer.allocate(2).putShort((short) helloTime).array();
        
        buffer.put(timeBytes);
        buffer.put(helloTimeBytes);
        
        buffer.put(new byte[] { 0x01, 0x01, 0x01, 0x01 });
    }
}
