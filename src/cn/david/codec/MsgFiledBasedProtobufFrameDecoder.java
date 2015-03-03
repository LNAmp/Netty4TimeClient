package cn.david.codec;

import java.util.List;

import cn.david.domain.MsgType;
import cn.david.protobuf.MsgTypeTable;

import com.google.protobuf.MessageLite;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class MsgFiledBasedProtobufFrameDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		//此处过来的已经解决了TCP的丢包黏包问题
		//bytebuf包括两个bit的表示msg类型，后面才是protobuf的实体内容
		byte[] msgTypeBytes = new byte[2];
		in.readBytes(msgTypeBytes, 0, 2);
		String msgType = new String(msgTypeBytes, "UTF-8");
		System.out.println("the receive msg type is " + msgType);
		//如果是“PI”，不需要解包，直接往后写“PI”
		if(MsgType.PING.equals(msgType)) {
			out.add(MsgType.PING);
			return;
		}
		MessageLite msgProto = MsgTypeTable.getMsgProto(msgType);
		int length = in.readableBytes();
		byte[] protoBytes = new byte[length];
		in.readBytes(protoBytes, 0, length);
		MessageLite msg = msgProto.getParserForType().parseFrom(protoBytes, 0, length);
		out.add(msg);
	}

}