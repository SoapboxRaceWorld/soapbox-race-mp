package world.soapboxrace.mp.server;

public interface IParser
{
    /**
     * Parse a packet.
     * @param packet The packet
     */
    void parse(byte[] packet);

    boolean isOk();
    
    boolean isPlayerInfoOk();

    byte[] getPlayerPacket(long timeDiff);
    byte[] getPlayerInfoPacket(long timeDiff);

    boolean isCarStateOk();

    byte[] getCarStatePacket(long timeDiff);
}
