package cn.david.handler;

import cn.david.codec.MyProtobufFramePrepender;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class ClientInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel socketChannel) throws Exception {
		ChannelPipeline p = socketChannel.pipeline();
		//p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
		p.addLast("frameEncoder", new MyProtobufFramePrepender("P"));
		p.addLast("protobufEncoder", new ProtobufEncoder());
	}

}
