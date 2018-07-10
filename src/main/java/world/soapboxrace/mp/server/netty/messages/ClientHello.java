package world.soapboxrace.mp.server.netty.messages;

import io.netty.buffer.ByteBuf;
import world.soapboxrace.mp.server.netty.UdpMessage;

import java.nio.ByteBuffer;

public class ClientHello implements UdpMessage
{
    public byte[] cryptoTicket;
    public byte[] ticketIV;
    public int cliHelloTime;
    public byte numPlayers;
    public byte playerIndex;
    public int sessionID;
    
    @Override
    public void read(ByteBuf buf)
    {
        // 				byteBuffer.put(gridIndex);
        //				byteBuffer.put(helloPacket);
        //				byteBuffer.putInt(eventDataEntity.getId().intValue());
        //				byteBuffer.put(numOfRacers);
        //				byteBuffer.putInt(personaId.intValue());
        
        cryptoTicket = new byte[32];
        ticketIV = new byte[32];
        
        buf.skipBytes(4);
        
        buf.readBytes(cryptoTicket);
        buf.readBytes(ticketIV);
        
        buf.skipBytes(1);
        
        cliHelloTime = buf.getUnsignedShort(69);
        
        playerIndex = cryptoTicket[0];
        numPlayers = cryptoTicket[9];
        sessionID = buf.getInt(9);
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        throw new UnsupportedOperationException();
    }
}
