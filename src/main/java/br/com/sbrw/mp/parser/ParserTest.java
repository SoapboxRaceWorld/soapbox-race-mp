package br.com.sbrw.mp.parser;

import br.com.sbrw.mp.util.UdpDebug;

public class ParserTest
{
    public static void main(String[] args)
    {
        byte[] packet = UdpDebug.hexStringToByteArray("01:00:00:73:00:01:ff:ff:ff:ff:02:4a:00:50:4c:41:59:45:52:31:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:64:00:00:00:00:00:00:00:72:67:90:a3:e2:ba:08:00:21:00:00:00:fa:91:32:00:c0:2f:92:22:50:03:00:00:00:b0:79:e6:cf:ee:1e:9c:fb:12:1a:ba:ef:98:08:73:de:d1:a5:97:49:c4:25:89:c4:1f:1e:fb:f1:d3:96:96:96:9a:fc:00:1f:ff:b0:8d:c3:30:ff:");
        
        SbrwParserV2 parser = new SbrwParserV2();
        parser.parseInputData(packet);
    }
}
