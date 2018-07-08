package world.soapboxrace.mp.server.netty.messages;

import io.netty.buffer.ByteBuf;
import world.soapboxrace.mp.server.netty.UdpMessage;

import java.nio.ByteBuffer;

// session sync26
public class ClientSyncStart implements UdpMessage
{
    // starts from 1? this definitely isn't countA or countB
    public short counter;

    // not the same value as the server
    public short time;

    // different than the value sent in hello? very weird
    public short cliHelloTime;

    // sometimes 0xFFFF (65,535), sometimes smaller values. 
    // this needs to be used when computing handshakeSync for the response
    public short unknownCounter;

    // based on unknownCounter; (1, 65535) = 0xFFFF, 2 = 0xBFFF, 3 = 0xDFFF
    public short handshakeSync;

    // extra info, type 0x00, size 0x06
    public SubPacket subPacket;
    
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
        
        subPacket = new SubPacket();
        subPacket.read(buf);
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        throw new UnsupportedOperationException();
    }

    // Subpacket containing 
    public static class SubPacket implements UdpMessage
    {
        public byte unknown;
        public int sessionID;
        public byte slotByte;
        
        public byte playerSlot;
        public byte maxPlayers;
        
        @Override
        public void read(ByteBuf buf)
        {
            buf.skipBytes(2); // type, size
            
            unknown = buf.readByte();
            sessionID = Math.toIntExact(buf.readUnsignedInt());
            slotByte = buf.readByte();
            
            {
                byte lo = (byte) ((slotByte & 0xf0));
                byte hi = (byte) ((slotByte & 0x0f) >> 1);

                lo >>= 1;
                lo &= 0x7f;
                lo >>= 4;
                
                playerSlot = lo;
                maxPlayers = hi;
            }
            
            buf.readByte(); // 0xFF terminator
        }

        @Override
        public void write(ByteBuffer buffer)
        {
            throw new UnsupportedOperationException();
        }
    }
}
