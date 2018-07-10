package world.soapboxrace.mp.server.netty.messages;

import io.netty.buffer.ByteBuf;
import world.soapboxrace.mp.server.netty.UdpMessage;

import java.nio.ByteBuffer;

// session sync26
public class ServerSyncStart implements UdpMessage
{
    public int counter;
    public int time;
    public int cliHelloTime;
    public int unknownCounter;

    // subpacket
    public byte gridIndex;
    public int sessionID;
    public byte numPlayers;

    @Override
    public void read(ByteBuf buf)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        byte[] counterBytes = ByteBuffer.allocate(2).putShort((short) counter).array();
        byte[] timeBytes = ByteBuffer.allocate(2).putShort((short) time).array();
        byte[] helloTimeBytes = ByteBuffer.allocate(2).putShort((short) cliHelloTime).array();
        byte[] unkCounterBytes = ByteBuffer.allocate(2).putShort((short) unknownCounter).array();

        buffer.put((byte) 0x00); // mp type
        buffer.put(counterBytes);
        buffer.put((byte) 0x02); // srv type
        buffer.put(timeBytes);
        buffer.put(helloTimeBytes);

        buffer.put(unkCounterBytes);

        int x = Integer.reverse(0xFFFF);

        if ((int) unknownCounter != 0xFFFF) // 1 -> 0111 ...??? or 1111 ...?
        {
            if (unknownCounter <= 16)
            {
                x &= ~(1 << (31 - (unknownCounter - 1)));
            }
        }

        byte[] hsBytes = ByteBuffer.allocate(4).putInt(x).array();

        buffer.put(hsBytes[0]);
        buffer.put(hsBytes[1]);

        // write subpacket
        buffer.put((byte) 0x00); // type
        buffer.put((byte) 0x06); // type
        buffer.put(gridIndex);
        buffer.putInt(sessionID);

        byte slots = 0;

        for (int i = 0; i < numPlayers; i++)
        {
            slots |= (1 << i);
        }

        buffer.put(slots);
        buffer.put((byte) 0xff); // end

        buffer.put(new byte[]{0x01, 0x01, 0x01, 0x01});
    }
}
