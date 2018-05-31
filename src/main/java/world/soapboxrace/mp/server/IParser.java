package world.soapboxrace.mp.server;

public interface IParser
{
    /**
     * Parse a packet.
     * @param packet The packet
     */
    void parse(byte[] packet);

    boolean isOk();

    byte[] getPlayerPacket(long timeDiff);

    boolean isCarStateOk();

    byte[] getCarStatePacket(long timeDiff);
}
