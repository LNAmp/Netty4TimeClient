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
import cn.david.domain.ChatType;
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
			//服务器表示收到了
			//其中ChatAckMsg中的rec_time表示收到的时间
			logger.info("process chatAckMsg");
			msg = (ChatAckMsg) msg;
			System.out.println("client recevied: " + msg.toString());
			processChatAckMsg((ChatAckMsg)msg);
		} else if(msg instanceof ChatMsg) {
			logger.info("process chatMsg");
			msg = (ChatMsg) msg;
			System.out.println("client recevied: " + msg.toString());
			processChatMsg((ChatMsg)msg);
		}
	}
	private void processChatMsg(ChatMsg msg) {
	// 处理聊天消息
		int chatType = msg.getChatType();
		switch(chatType) {
		case ChatType.TEXT :
			processUserChatMsg((ChatMsg) msg); 
		case ChatType.IMAGE :
			System.out.println("client recevied Image: " + msg.toString());
		case ChatType.ACK :
			processUserChatAckMsg(msg);
		default :
			return ;
		}
	}
	
	private void processUserChatMsg(ChatMsg msg) {
		System.out.println("client recevied chat Msg : " + msg.toString());
		//发送文件回执
		if(ClientUtil.token == "") {
			throw new RuntimeException("token is null, please login first!");
		}
		ClientUtil.sendChatMsg(msg.getUserIdDesc(), msg.getUserIdSrc(), ChatType.ACK, "Rec", ClientUtil.token, msg.getMsgId());
	}
	
	private void processUserChatAckMsg(ChatMsg msg) {
	//处理用户发过来的“已读”消息
		System.out.println("client recevied user chat ack Msg : " + msg.toString());
		//得到msgId
		String msgId = msg.getMsgId();
		//得到回执时间
		Long recTime = msg.getSendTime();
		if(! ClientUtil.chatMsgs.containsKey(msgId)) {
			throw new RuntimeException("wrong chatAckMsg Id");
		}else {
			//得到时间差
			Integer period =  (int)(long)(recTime - ClientUtil.chatMsgs.remove(msgId));
			//打印出时间差
			logger.info("the time to desc :message[" + msgId +"]: "+period);
			//将时间差写入队列
			ClientUtil.userReplyPeriods.add(period);
		}
	}
	
	private void processChatAckMsg(ChatAckMsg msg) {
		
	//处理服务器的消息回执
		//得到msgId
		String msgId = msg.getMsgId();
		logger.info("server received :message[" + msgId + "]");
		//得到回执的时间
		Long recTime = msg.getRecTime();
		if(! ClientUtil.chatMsgs.containsKey(msgId)) {
			throw new RuntimeException("wrong chatAckMsg Id");
		}else {
			//得到时间差
			//由于是服务器先收到消息，所以服务器的确认应该先返回
			Integer period =  (int)(long)(recTime - ClientUtil.chatMsgs.get(msgId));
			//打印出时间差
			logger.info("the time to server :message[" + msgId +"]: "+period);
			//将时间差写入队列
			ClientUtil.serverReplyPeriods.add(period);
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
