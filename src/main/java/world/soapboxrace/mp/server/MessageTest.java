package world.soapboxrace.mp.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import world.soapboxrace.mp.server.netty.messages.ClientKeepAlive;
import world.soapboxrace.mp.server.netty.messages.ClientSync;
import world.soapboxrace.mp.server.netty.messages.ClientSyncStart;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MessageTest
{
    public static void main(String[] a) throws IOException
    {
        byte[] clientSyncBytes = Files.readAllBytes(Paths.get("estudo/testdata/cli-sync.bin"));
        byte[] clientSyncStartBytes = Files.readAllBytes(Paths.get("estudo/testdata/cli-sync-start.bin"));
        byte[] clientKeepAliveBytes = Files.readAllBytes(Paths.get("estudo/testdata/cli-keepalive.bin"));

        ClientSync clientSync = new ClientSync();
        ClientSyncStart clientSyncStart = new ClientSyncStart();
        ClientKeepAlive clientKeepAlive = new ClientKeepAlive();
        
        ByteBuf clientSyncBuf = Unpooled.copiedBuffer(clientSyncBytes);
        ByteBuf clientSyncStartBuf = Unpooled.copiedBuffer(clientSyncStartBytes);
        ByteBuf clientKeepAliveBuf = Unpooled.copiedBuffer(clientKeepAliveBytes);
        
        clientSync.read(clientSyncBuf);
        clientSyncStart.read(clientSyncStartBuf);
        clientKeepAlive.read(clientKeepAliveBuf);
        
        System.out.println();
    }
}
