package br.com.sbrw.mp.util;

public class UdpUtil
{
    public static byte generateSlotsBits(int numberOfPlayers)
    {
        switch (numberOfPlayers)
        {
            case 2:
                return 0x03;
            case 3:
                return 0x07;
            case 4:
                return 0x0f;
            case 5:
                return 0x1f;
            case 6:
                return 0x3f;
            case 7:
                return 0x7f;
            case 8:
                return (byte) 0xff;
            default:
                return (byte) 0xff;
        }
    }
}
