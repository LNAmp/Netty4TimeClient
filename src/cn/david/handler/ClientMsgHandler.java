package cn.david.handler;

import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;
import cn.david.android.client.Connection;
import cn.david.android.client.MsgCollector;
import cn.david.android.client.Connection.ListenerWrapper;
import cn.david.client.util.ClientUtil;
import cn.david.domain.IMProto.AckMsg;
import cn.david.domain.IMProto.AskLocAckMsg;
import cn.david.domain.IMProto.ChatAckMsg;
import cn.david.domain.IMProto.ChatMsg;
import cn.david.domain.IMProto.UploadLocMsg;
import cn.david.domain.AckResponse;
import cn.david.domain.AckType;
import cn.david.domain.ChatType;
import cn.david.domain.IdleStateConstant;
import cn.david.domain.MsgType;
import cn.david.domain.ReceiveMsg;
import cn.david.domain.ServerMsg;
import cn.david.future.WriteFuture;
import cn.david.login.LoginFactory;
import cn.david.register.RegisterFactory;
import cn.david.util.DateUtil;

import com.google.protobuf.MessageLite;

/**
 * 用于处理客户端的socket读写、编解码
 * 由于设置了客户端只有一个IO线程，所以不存在多线程读写问题
 * @author 907
 *
 */
public class ClientMsgHandler extends ChannelInboundHandlerAdapter {

	Logger logger = Logger.getLogger(ClientMsgHandler.class);
	private Connection connection;
	
	//为防阻塞，让所有的监听器在单线程池中执行
	private static ExecutorService listenerExecutor;
	
	static {
		listenerExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable, 
						"Listener Porcessor");
				thread.setDaemon(true);
				return thread;
			}
		});
	}
	
	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	//private static int pongCount = 5;
//	//这种做法不是特别好，是临时的
//	private MessageLite msg;
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		ReceiveMsg recvMsg = null;
		if(msg instanceof String) {
			if(MsgType.PING.equals((String)msg)) {
				logger.info("At time : "+ DateUtil.printCurTime() +" ,recevie the PING.");
//				if(pongCount == 5) {
//					return;
//				}
				ServerMsg msg1 = new ServerMsg();
				msg1.setMsgType(MsgType.PONG);
				msg1.setProtoMsgContent(null);
				//pongCount++;
				ctx.writeAndFlush(msg1);
				return;
			}else if(MsgType.PONG.equals((String)msg)) {
				System.out.println(msg);
			}
			//System.out.println(msg);
		} else if(msg instanceof UploadLocMsg) {
			//System.out.println("client recevied: " + msg.toString());
			UploadLocMsg tmpMsg = (UploadLocMsg)msg;
			recvMsg = new ReceiveMsg();
			recvMsg.setMsgId(tmpMsg.getMsgId());
			recvMsg.setMsgType(MsgType.UPLOAD_LOCATION);
			recvMsg.setMsgContent(tmpMsg);
		} else if(msg instanceof AskLocAckMsg) {
			logger.info("process AskLocAckMsg");
			//缺少处理定位消息的函数，暂时只是输出
			//System.out.println("client received: " + msg.toString());
			AskLocAckMsg tmpMsg = (AskLocAckMsg)msg;
			recvMsg = new ReceiveMsg();
			recvMsg.setMsgId(tmpMsg.getMsgId());
			recvMsg.setMsgType(MsgType.ASK_LOCATION_ACK);
			recvMsg.setMsgContent(tmpMsg);
		} else if(msg instanceof AckMsg) {
			//该AckMsg专门用来应对的是Login和Register的“回执”
			logger.info("process ackMsg");
			//processAckMsg((AckMsg)msg);
			AckMsg tmpMsg = (AckMsg)msg;
			recvMsg = new ReceiveMsg();
			recvMsg.setMsgId(tmpMsg.getMsgId());
			recvMsg.setMsgType(MsgType.ACK);
			recvMsg.setMsgContent(tmpMsg);
		} else if(msg instanceof ChatAckMsg) {
			//该ChatAckMsg用来应对的是ChatMsg和AskOffilneMsg的“回执”
			//服务器表示收到了
			//其中ChatAckMsg中的rec_time表示收到的时间
			logger.info("process chatAckMsg");
			ChatAckMsg tmpMsg = (ChatAckMsg)msg;
			recvMsg = new ReceiveMsg();
			recvMsg.setMsgId(tmpMsg.getMsgId());
			recvMsg.setMsgType(MsgType.CHAT_ACK);
			recvMsg.setMsgContent(tmpMsg);
			//System.out.println("client recevied: " + msg.toString());
			//processChatAckMsg((ChatAckMsg)msg);
		} else if(msg instanceof ChatMsg) {
			logger.info("process chatMsg");
			msg = (ChatMsg) msg;
			//System.out.println("client recevied: " + msg.toString());
			//processChatMsg((ChatMsg)msg);
			recvMsg = new ReceiveMsg();
			ChatMsg tmpMsg = (ChatMsg)msg;
			recvMsg.setMsgId(tmpMsg.getMsgId());
			recvMsg.setMsgType(MsgType.CHAT);
			recvMsg.setMsgContent(tmpMsg);
		}
		//调用listener和collector处理消息
		if(recvMsg == null) {
			return;
		}
		if(connection == null) {
			return;
		}
		for(MsgCollector collector : connection.getMsgCollectors()) {
			collector.processMsg(recvMsg);
		}
		listenerExecutor.submit(new ListenerNotification(recvMsg));
	}

	private class ListenerNotification implements Runnable {
		private ReceiveMsg msg;
		
		public ListenerNotification(ReceiveMsg msg) {
			this.msg = msg;
		}
		
		@Override
		public void run() {
			for(ListenerWrapper wrapper : connection.getRecvListeners().values()) {
				logger.info("do");
				wrapper.notifyListener(msg);
			}
		}
		
	}
	
	private void processChatMsg(ChatMsg msg) {
	// 处理聊天消息
		int chatType = msg.getChatType();
		switch(chatType) {
		case ChatType.TEXT :
			processUserChatMsg((ChatMsg) msg); 
			break;
		case ChatType.IMAGE :
			System.out.println("client recevied Image: " + msg.toString());
			break;
		case ChatType.ACK :
			processUserChatAckMsg(msg);
			break;
		default :
			return ;
		}
	}
	
	private void processUserChatMsg(ChatMsg msg) {
		System.out.println("client recevied chat Msg : " + msg.toString());
		//发送文件回执
//		if(ClientUtil.token == "") {
//			throw new RuntimeException("token is null, please login first!");
//		}
//		//暂时去掉发送消息回执
//		//测试用户回执的时候需要去掉
//		ClientUtil.sendChatMsg(msg.getUserIdDesc(), msg.getUserIdSrc(), ChatType.ACK, "Rec", ClientUtil.token, msg.getMsgId());
	}
	
	private void processUserChatAckMsg(ChatMsg msg) {
	//处理用户发过来的“已读”消息
		System.out.println("client recevied user chat ack Msg : " + msg.toString());
		//得到msgId
		String msgId = msg.getMsgId();
		//得到回执的时间
		//Long recTime = msg.getRecTime();
		Long recTime = System.currentTimeMillis();
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
		//Long recTime = msg.getRecTime();
		Long recTime = System.currentTimeMillis();
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
	
	
	
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		logger.info("the channelActive in IMServerMsgHandler is called");

		ChannelPipeline pipeline = ctx.pipeline();
		pipeline.addAfter("protobufEncoder", "idleStateHandler", new IdleStateHandler(
				IdleStateConstant.READER_IDLE_TIME, IdleStateConstant.WRITER_IDLE_TIME, IdleStateConstant.ALL_IDLE_TIME));
		pipeline.addAfter("idleStateHandler", "HeartbeatCheckHandler", new HeartbeatCheckHandler());
		
		super.channelActive(ctx);
	}
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		logger.info("channelInactive");
		super.channelInactive(ctx);
	}
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		logger.info("channelRegistered");
		super.channelRegistered(ctx);
	}
	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		logger.info("channelUnregistered");
		super.channelUnregistered(ctx);
	}
	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx)
			throws Exception {
		// TODO Auto-generated method stub
		logger.info("channelWritabilityChanged");
		super.channelWritabilityChanged(ctx);
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		logger.info("exceptionCaught");
		if(cause instanceof SocketTimeoutException) {
			logger.info("Ready to close the conn,reason:TIME_OUT");
			connection.disconnect();
		}
		super.exceptionCaught(ctx, cause);
	}
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		// TODO Auto-generated method stub
		logger.info("userEventTriggered");
		super.userEventTriggered(ctx, evt);
	}
}
