package br.com.sbrw.mp.parser;

public class SbrwParserV2 implements IParser
{
    @Override
    public void parseInputData(byte[] packet)
    {
        
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
