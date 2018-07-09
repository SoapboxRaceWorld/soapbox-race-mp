package world.soapboxrace.mp.server;

import io.netty.channel.ChannelFuture;
import org.slf4j.LoggerFactory;
import world.soapboxrace.mp.server.netty.NettyServer;
import world.soapboxrace.mp.util.BitConverter;
import world.soapboxrace.mp.util.ServerLog;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * The entry point.
 */
public class Main
{
    /**
     * Converts an integer to a 32-bit binary string
     *
     * @param number    The number to convert
     * @param groupSize The number of bits in a group
     * @return The 32-bit long bit string
     */
    public static String intToString(int number, int groupSize)
    {
        StringBuilder result = new StringBuilder();

        for (int i = 31; i >= 0; i--)
        {
            int mask = 1 << i;
            result.append((number & mask) != 0 ? "1" : "0");

            if (i % groupSize == 0)
                result.append(" ");
        }
        result.replace(result.length() - 1, result.length(), "");

        return result.toString();
    }

    /**
     * Converts an short to a 16-bit binary string
     *
     * @param number    The number to convert
     * @param groupSize The number of bits in a group
     * @return The 16-bit long bit string
     */
    public static String shortToString(short number, int groupSize)
    {
        StringBuilder result = new StringBuilder();

        for (int i = 15; i >= 0; i--)
        {
            int mask = 1 << i;
            result.append((number & mask) != 0 ? "1" : "0");

            if (i % groupSize == 0)
                result.append(" ");
        }
        result.replace(result.length() - 1, result.length(), "");

        return result.toString();
    }

    public static void main(String[] args)
    {
        byte res = 0;

        for (int i = 0; i < 7; i++)
        {
            res |= (1 << i);
        }
        
        System.out.println(res);

        System.out.println(shortToString((short) 65535, 4));
        System.out.println(shortToString((short) 61439, 4));

        int x = 0xFFFF;

        x = Integer.reverse(x);
//
        x &= ~(1 << 31 - (1 - 1));
//        x &= ~(1 << (31 - 17 - 1));

        System.out.println(intToString(x, 4));

        byte[] data = ByteBuffer.allocate(4).putInt(x).array();
//        data.clone();
//
//        BitSet bitSet = new BitSet(16);
//        bitSet.set(0, 15);
//
//        bitSet.flip(3);
//        
//        StringBuilder s = new StringBuilder();
//        for( int i = 0; i < bitSet.length();  i++ )
//        {
//            s.append(bitSet.get(i) ? 1: 0 );
//        }
//
//        System.out.println( s );
////        System.out.println( bitSet.toByteArray().length );
//
//        System.out.println(Integer.parseInt(s.toString(), 2));

//        System.out.println( (short) (bitSet.toLongArray()[0]) );


        NettyServer server;

        try
        {
            server = new NettyServer(9998);
            ChannelFuture future = server.start();

            ServerLog.SERVER_LOGGER.info("Started UDP server!");

            // Wait until the connection is closed.
            future.channel().closeFuture().sync();
        } catch (InterruptedException ex)
        {
            System.err.println(ex.getMessage());
        }
    }
}
