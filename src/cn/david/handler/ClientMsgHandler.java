package cn.david.handler;

import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import cn.david.client.util.ClientUtil;
import cn.david.domain.IMProto.AckMsg;
import cn.david.domain.IMProto.AskLocAckMsg;
import cn.david.domain.IMProto.ChatAckMsg;
import cn.david.domain.IMProto.ChatMsg;
import cn.david.domain.IMProto.UploadLocMsg;
import cn.david.domain.AckResponse;
import cn.david.domain.AckType;
import cn.david.domain.MsgType;
import cn.david.domain.ServerMsg;
import cn.david.future.WriteFuture;
import cn.david.login.LoginFactory;
import cn.david.register.RegisterFactory;
import cn.david.util.DateUtil;

import com.google.protobuf.MessageLite;

public class ClientMsgHandler extends ChannelInboundHandlerAdapter {
	
	Logger logger = Logger.getLogger(ClientMsgHandler.class);
	
//	private static int pongCount = 0;
//	//这种做法不是特别好，是临时的
//	private MessageLite msg;
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if(msg instanceof String) {
			if(MsgType.PING.equals((String)msg)) {
				logger.info("At time : "+ DateUtil.printCurTime() +" ,recevie the PING.");
//				if(pongCount == 1) {
//					return;
//				}
				ServerMsg msg1 = new ServerMsg();
				msg1.setMsgType(MsgType.PONG);
				msg1.setProtoMsgContent(null);
				//pongCount++;
				ctx.writeAndFlush(msg1);
			}
			//System.out.println(msg);
		} else if(msg instanceof UploadLocMsg) {
			System.out.println("client recevied: " + msg.toString());
		} else if(msg instanceof AskLocAckMsg) {
			logger.info("process AskLocAckMsg");
			//缺少处理定位消息的函数，暂时只是输出
			System.out.println("client received: " + msg.toString());
		} else if(msg instanceof AckMsg) {
			//该AckMsg专门用来应对的是Login和Register的“回执”
			logger.info("process ackMsg");
			processAckMsg((AckMsg)msg);
		} else if(msg instanceof ChatAckMsg) {
			//该ChatAckMsg用来应对的是ChatMsg和AskOffilneMsg的“回执”
			logger.info("process chatAckMsg");
			msg = (ChatAckMsg) msg;
			System.out.println("client recevied: " + msg.toString());
		} else if(msg instanceof ChatMsg) {
			logger.info("process chatMsg");
			msg = (ChatMsg) msg;
			System.out.println("client recevied: " + msg.toString());
		}
	}
	private void processAckMsg(AckMsg msg2) {
		//RegisterFactory.getInstance().processRegisterAckMsg(msg2);
		AckResponse response = new AckResponse();
		response.setAckType(msg2.getAckType());
		response.setMsgId(msg2.getMsgId());
		response.setResponseCode(msg2.getResponseCode());
		response.setToken(msg2.getToken());
		//根据id得到map中的future
		WriteFuture<AckResponse> future = ClientUtil.syncWriteMsgs.get(msg2.getMsgId());
		if(future != null) {
			future.setResponse(response);
		}
	}
	


	
	
}
