package br.com.sbrw.mp.parser;

import br.com.sbrw.mp.util.ArrayReader;
import br.com.sbrw.mp.util.UdpDebug;

public class SbrwParserV2 implements IParser
{
    private final byte ID_PLAYER_INFO = 0x02;
    private final byte ID_CAR_STATE = 0x12;

    // full packet
    // 01:00:00:73:00:01:ff:ff:ff:ff:02:4a:00:50:4c:41:59:45:52:31:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:64:00:00:00:00:00:00:00:72:67:90:a3:e2:ba:08:00:21:00:00:00:fa:91:32:00:c0:2f:92:22:50:03:00:00:00:b0:79:e6:cf:ee:1e:9c:fb:12:1a:ba:ef:98:08:73:de:d1:a5:97:49:c4:25:89:c4:1f:1e:fb:f1:d3:96:96:96:9a:fc:00:1f:ff:b0:8d:c3:30:ff:

    // 01:00:00:73:00:01:ff:ff:ff:ff:
    private byte[] header;

    // 02:4a:00:50:4c:41:59:45:52:31:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:64:00:00:00:00:00:00:00:72:67:90:a3:e2:ba:08:00:21:00:00:00:fa:91:32:00:c0:2f:92:22:50:03:00:00:00:b0:79:e6:cf:ee:1e:9c:fb:
    private byte[] playerInfo;

    // 12:1a:ba:ef:98:08:73:de:d1:a5:97:49:c4:25:89:c4:1f:1e:fb:f1:d3:96:96:96:9a:fc:00:1f:
    private byte[] carState;

    // b0:8d:c3:30:
    private byte[] crc;

    @Override
    public void parseInputData(byte[] packet)
    {
        System.out.println(UdpDebug.byteArrayToHexString(packet));
        ArrayReader arrayReader = new ArrayReader(packet);

        this.header = arrayReader.readBytes(10);

        System.out.println(UdpDebug.byteArrayToHexString(header));

        while (arrayReader.getPosition() < arrayReader.getLength())
        {
            byte packetId = arrayReader.readByte();

            if (packetId == (byte) 0xff)
            {
                break;
            }

            byte packetLength = arrayReader.readByte();

            if (arrayReader.getPosition() + packetLength > arrayReader.getLength())
            {
                throw new IllegalStateException(String.format("Cannot read packet 0x%02x (0x%02x bytes, position %d, length %d)", 
                        packetId, 
                        packetLength, 
                        arrayReader.getPosition(), 
                        arrayReader.getLength()));
            }

            System.out.println(String.format("packet 0x%02x (0x%02x)", packetId, packetLength));

            switch (packetId)
            {
                case ID_PLAYER_INFO:
                {
                    playerInfo = new byte[packetLength + 2];
                    playerInfo[0] = packetId;
                    playerInfo[1] = packetLength;

                    System.arraycopy(arrayReader.readBytes(packetLength), 0, playerInfo, 2, packetLength);
                    System.out.println(UdpDebug.byteArrayToHexString(playerInfo));

                    break;
                }
                case ID_CAR_STATE:
                {
                    carState = new byte[packetLength + 2];
                    carState[0] = packetId;
                    carState[1] = packetLength;

                    System.arraycopy(arrayReader.readBytes(packetLength), 0, carState, 2, packetLength);
                    System.out.println(UdpDebug.byteArrayToHexString(carState));
                    break;
                }
                default:
                    arrayReader.seek(packetLength, true);
                    break;
            }
        }
        
        arrayReader.seek(arrayReader.getLength() - 4);
        
        crc = arrayReader.readBytes(4);

        System.out.println(UdpDebug.byteArrayToHexString(crc));
    }

    @Override
    public boolean isOk()
    {
        return false;
    }

    @Override
    public byte[] getPlayerPacket(long timeDiff)
    {
        return new byte[0];
    }

    @Override
    public boolean isCarStateOk()
    {
        return false;
    }

    @Override
    public byte[] getCarStatePacket(long timeDiff)
    {
        return new byte[0];
    }
}
