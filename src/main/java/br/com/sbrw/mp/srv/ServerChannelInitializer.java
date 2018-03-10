package br.com.sbrw.mp.srv;

import br.com.sbrw.mp.handler.HelloHandler;
import br.com.sbrw.mp.handler.PlayerInfoAfterHandler;
import br.com.sbrw.mp.handler.PlayerInfoBeforeHandler;
import br.com.sbrw.mp.handler.SyncHandler;
import br.com.sbrw.mp.handler.SyncHelloHandler;
import br.com.sbrw.mp.handler.SyncKeepAlive;
import br.com.sbrw.mp.handlerstr.HelloHandlerStr;
import br.com.sbrw.mp.handlerstr.PlayerInfoAfterHandlerStr;
import br.com.sbrw.mp.handlerstr.PlayerInfoBeforeHandlerStr;
import br.com.sbrw.mp.handlerstr.SyncHandlerStr;
import br.com.sbrw.mp.handlerstr.SyncHelloHandlerStr;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;

public class ServerChannelInitializer extends ChannelInitializer<DatagramChannel> {
	@Override
	protected void initChannel(DatagramChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast("hello", new HelloHandler());
		pipeline.addLast("syncHello", new SyncHelloHandler());
		pipeline.addLast("sync", new SyncHandler());
		pipeline.addLast("syncKeepAlive", new SyncKeepAlive());
		pipeline.addLast("playerInfoBefore", new PlayerInfoBeforeHandler());
		pipeline.addLast("playerInfoAfter", new PlayerInfoAfterHandler());

		pipeline.addLast("helloHandlerStr", new HelloHandlerStr());
		pipeline.addLast("syncHelloHandlerStr", new SyncHelloHandlerStr());
		pipeline.addLast("playerInfoBeforeHandlerStr", new PlayerInfoBeforeHandlerStr());
		pipeline.addLast("syncHandlerStr", new SyncHandlerStr());
		pipeline.addLast("playerInfoAfterHandlerStr", new PlayerInfoAfterHandlerStr());

	}
}
