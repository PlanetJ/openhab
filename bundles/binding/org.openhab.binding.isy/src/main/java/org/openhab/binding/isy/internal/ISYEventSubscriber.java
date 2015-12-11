package org.openhab.binding.isy.internal;

import java.net.URI;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

public class ISYEventSubscriber {
	
	private WebSocketClientHandshaker handshaker; 
	private Channel channel;
	
	private String host;
	private int port;
	
	public ISYEventSubscriber(String host, int port, String username, String password) {
		this.host = host;
		this.port = port;
		HttpHeaders headers = new DefaultHttpHeaders()
				.add(HttpHeaders.Names.ORIGIN, "com.universal-devices.websockets.isy");
		if(StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)){
			String auth = Base64.encodeBase64URLSafeString(new String(username + ":" + password).getBytes());
			headers.add(HttpHeaders.Names.AUTHORIZATION, "Basic "+auth);
		}
				
		
		handshaker = WebSocketClientHandshakerFactory.newHandshaker(URI.create("ws://"+host+":"+port+"/rest/subscribe"), WebSocketVersion.V13, 
				"ISYSUB", true, headers);
	}
	
	public boolean start() {
		
		Bootstrap b = new Bootstrap();
		b.group(new NioEventLoopGroup())
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel socketChannel) throws Exception {
					ChannelPipeline p = socketChannel.pipeline();
					p.addLast(new HttpClientCodec(), new HttpObjectAggregator(8192), new WebsocketClientHandler(handshaker));
				}
			});

		
		try {
			channel = b.connect(host, port).sync().channel();
			handshaker.handshake(channel);
		} catch (InterruptedException e) {
			return false;
		}
		
		return channel.isOpen();
	}
	
	public void stop(){
		if(channel != null && channel.isOpen())
			handshaker.close(channel, new CloseWebSocketFrame());
	}
	
	public static class WebsocketClientHandler extends SimpleChannelInboundHandler<Object>{

		private final WebSocketClientHandshaker handshaker;
		
		public WebsocketClientHandler(WebSocketClientHandshaker handshaker) {
			this.handshaker = handshaker;
		}
		
		@Override
		protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
			Channel ch = ctx.channel();
			if(!handshaker.isHandshakeComplete()){
				handshaker.finishHandshake(ch, (FullHttpResponse)msg);
				return;
			}
			
			if(msg instanceof WebSocketFrame){
				if(msg instanceof TextWebSocketFrame){
					System.out.println(((TextWebSocketFrame) msg).text());
				}
			}
		}
		
	}

}
