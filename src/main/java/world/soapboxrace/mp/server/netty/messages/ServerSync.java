package world.soapboxrace.mp.server.netty.messages;

import io.netty.buffer.ByteBuf;
import world.soapboxrace.mp.server.netty.UdpMessage;

import java.nio.ByteBuffer;

public class ServerSync implements UdpMessage
{
    public short counter;
    public short time;
    public short cliHelloTime;
    public short unknownCounter;

    @Override
    public void read(ByteBuf buf)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        byte[] sequenceBytes = ByteBuffer.allocate(2).putShort(counter).array();
        byte[] timeBytes = ByteBuffer.allocate(2).putShort(time).array();
        byte[] helloTimeBytes = ByteBuffer.allocate(2).putShort(cliHelloTime).array();
        byte[] ucBytes = ByteBuffer.allocate(2).putShort(unknownCounter).array();

        buffer.put((byte) 0x00);
        buffer.put(sequenceBytes);
        buffer.put((byte) 0x02);
        buffer.put(timeBytes);
        buffer.put(helloTimeBytes);
        buffer.put(ucBytes);

        int x = Short.toUnsignedInt((short) 65535);

        x = Integer.reverse(x);

        x &= ~(1 << (31 - unknownCounter - 1));

        byte[] hsBytes = ByteBuffer.allocate(4).putInt(x).array();

        buffer.put(hsBytes[0]);
        buffer.put(hsBytes[1]);

        // write subpacket
        buffer.put((byte) 0x01); // ID
        buffer.put((byte) 0x03); // size
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x40);
        buffer.put((byte) 0xb7);
        buffer.put((byte) 0xff); // end

        buffer.put(new byte[]{0x01, 0x01, 0x01, 0x01});
    }
}
