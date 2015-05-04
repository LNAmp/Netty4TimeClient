package cn.david.client.util;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import cn.david.domain.AckCode;
import cn.david.domain.AckResponse;
import cn.david.domain.LocationType;
import cn.david.domain.MsgType;
import cn.david.domain.ServerMsg;
import cn.david.domain.IMProto.AskLocMsg;
import cn.david.domain.IMProto.AskOfflineMsg;
import cn.david.domain.IMProto.ChatMsg;
import cn.david.domain.IMProto.LoginMsg;
import cn.david.domain.IMProto.RegisterMsg;
import cn.david.domain.IMProto.UploadLocMsg;
import cn.david.future.SyncWriteFuture;
import cn.david.future.WriteFuture;
import cn.david.handler.IMServerInitializer;
import cn.david.login.LoginFactory;
import cn.david.register.RegisterFactory;
import cn.david.register.RegisterState;

import com.google.protobuf.MessageLite;

public class ClientUtil {
	private static String serverIp;
	private static int serverPort;
	private static ChannelFuture channelFuture;
	private static Channel channel = null;
	public static Bootstrap b;
	private static Logger logger = Logger.getLogger(ClientUtil.class);
	public static final Map<String,WriteFuture<AckResponse>> syncWriteMsgs = new ConcurrentHashMap<String,WriteFuture<AckResponse>>();
	public static final Map<String,Long> chatMsgs = new ConcurrentHashMap<String, Long>();
	public static final ConcurrentLinkedQueue<Integer> serverReplyPeriods = new ConcurrentLinkedQueue<Integer>();
	public static final ConcurrentLinkedQueue<Integer> userReplyPeriods = new ConcurrentLinkedQueue<Integer>();
	
	public static String token = "";
	
	private static int failTimes = 0;
	
	public static AckResponse syncWriteMsg(final MessageLite msg, final long timeout,Channel channel) throws InterruptedException, ExecutionException, TimeoutException {
		if(channel == null) {
			throw new RuntimeException("can't connect to the server");
		}
		if(msg == null) {
			throw new NullPointerException("msg is null");
		}
		if(timeout <= 0) {
			throw new IllegalArgumentException("the timeout can't be negative!");
		}
		if(msg instanceof RegisterMsg) {
			RegisterMsg reMsg = (RegisterMsg) msg;
			WriteFuture<AckResponse> future = new SyncWriteFuture(reMsg.getMsgId());
			syncWriteMsgs.put(reMsg.getMsgId(), future);
			
			AckResponse response = _doSyncWriteMsg(channel,MsgType.REGISTER,msg,timeout,future,reMsg.getMsgId());
			syncWriteMsgs.remove(reMsg.getMsgId());
			return response;
		} else if(msg instanceof LoginMsg) {
			LoginMsg loginMsg = (LoginMsg) msg;
			WriteFuture<AckResponse> future = new SyncWriteFuture(loginMsg.getMsgId());
			syncWriteMsgs.put(loginMsg.getMsgId(), future);
			
			AckResponse response = _doSyncWriteMsg(channel,MsgType.LOGIN,msg,timeout,future,loginMsg.getMsgId());
			syncWriteMsgs.remove(loginMsg.getMsgId());
			return response;
		}
		return null;
	}
	
	private static AckResponse _doSyncWriteMsg(Channel channel,final String type,final MessageLite msg,final long timeout,
			final WriteFuture<AckResponse> writeFuture,final String msgId) throws InterruptedException, ExecutionException, TimeoutException {
		ServerMsg serverMsg = new ServerMsg();
		serverMsg.setMsgType(type);
		serverMsg.setProtoMsgContent(msg);
		channel.writeAndFlush(serverMsg).addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				writeFuture.setWriteResult(future.isSuccess());
				writeFuture.setCause(future.cause());
				if(!writeFuture.isWriteSuccess()) {
					syncWriteMsgs.remove(msgId);
				}
			}
		});
		AckResponse response = writeFuture.get(timeout, TimeUnit.MILLISECONDS);
		return response;
	}
	
	public static void loginMembers(int startId,int num)  {
		//登录一千个用户，保存其token
		
		int i = startId;
		int end = num+startId;
		while(i<end) {
			ChannelFuture future = b.connect(ClientUtil.serverIp, ClientUtil.serverPort);
			try {
				future.sync();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(future.isSuccess()&&future.isDone()) {
				Channel channel = future.channel();
				if(channel==null) {
					throw new RuntimeException("the channel is null!");
				}
				String msgId = UUID.randomUUID().toString();
				String username = "david"+(i-3);
				String password = "qinqinhaiou";
				LoginMsg.Builder builder = LoginMsg.newBuilder();
				builder.setMsgId(msgId);
				builder.setUsername(username);
				builder.setPassword(password);
				LoginMsg loginMsg = builder.build();
				//unit is millisecond
				AckResponse response = null;
				try {
					response = ClientUtil.syncWriteMsg(loginMsg, 1000,channel);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(response != null && (response.getResponseCode()==AckCode.LOGIN_SUCCESS)) {
					logger.info((i-2)+" users has logined!");
					i++;
				}
				
			}else {
				failTimes ++;
				logger.info(failTimes+" times connections has failed!");
			}
		}
	}
	
	/**
	 * 
	 */
	static {
		InputStream is = ClientUtil.class.getClassLoader().getResourceAsStream("server.properties");
		Properties prop = new Properties();
		try {
			prop.load(is);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		serverIp = prop.getProperty("serverIp");
		serverPort = Integer.parseInt(prop.getProperty("serverPort"));
	}
	
	/**
	 * 
	 * 
	 * 
	 */
	
	public static void initBootstrap() {
		EventLoopGroup group = new NioEventLoopGroup();
        b = new Bootstrap();
        b.group(group)
         .channel(NioSocketChannel.class)
         .option(ChannelOption.TCP_NODELAY, true)
         .handler(new IMServerInitializer());
	}
	
	public static ChannelFuture connectServer(ChannelFutureListener listener ) throws InterruptedException {
		//ChannelFuture channelFuture = null;
		try {
			channelFuture = b.connect(serverIp, serverPort).sync();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e);
			System.out.println(e.getCause());
		}
		if(channelFuture == null) {
			System.out.println("isNull");
		}else {
//			System.out.println("cancle:"+channelFuture.isCancelled()+
//					"done:"+channelFuture.isDone()+"success:"+channelFuture.isSuccess()+
//					"cause"+channelFuture.cause().getCause()+"causeMsg"+channelFuture.cause().getMessage());
		}
		
		return channelFuture;
	}
	
	public static void writeMsg() {
		if( !(channelFuture.isDone() && channelFuture.isSuccess())) {
			logger.error("can't connect to the server");
			throw new RuntimeException("can't connect to the server");
		} else {
			channel = channelFuture.channel();
		}
		if(channel == null) {
			throw new RuntimeException("can't connect to the server");
		}
		UploadLocMsg.Builder bulider = UploadLocMsg.newBuilder();
		UploadLocMsg locMsg = bulider.setLocationType(LocationType.NETWORK)
		.setUserId(12)
		.setLongitude(128.32)
		.setLatitude(29.00)
		.setMapId("F2B4")
		.setUpdateTime(new Date().getTime()).build();
		ServerMsg smsg = new ServerMsg();
		smsg.setMsgType(MsgType.UPLOAD_LOCATION);
		smsg.setProtoMsgContent(locMsg);
		channel.writeAndFlush(smsg);
		
		//AddressBook.Builder bulider2 = AddressBook.newBuilder();
	}
	
	public static void registerAccount(String username,String password,String email,String msgId) {
		if(channel == null) {
			throw new RuntimeException("can't connect to the server");
		}
		RegisterFactory.getInstance().sendRegisterMsg(channel, username, password, email, msgId);
	}
	
	public static RegisterState checkRegisterState(String msgId) {
		return RegisterFactory.getInstance().getRegisterState(msgId);
	}
	
	public static boolean checkRegisterAcked(String msgId) {
		return RegisterFactory.getInstance().isAcked(msgId);
	}
	
	public static boolean checkRegisterPassed(String msgId) {
		return RegisterFactory.getInstance().isPassed(msgId);
	}
	
	public static int getRegisterRespCode(String msgId) {
		return RegisterFactory.getInstance().getAckCode(msgId);
	}
	
	public static void loginAccount(String username,String password,String msgId) {
		if(channel == null) {
			throw new RuntimeException("can't connect to the server");
		}
		LoginFactory.getInstance().sendLoginMsg(channel, username, password, msgId);
	}
	
	public static boolean checkLoginAcked(String msgId) {
		return LoginFactory.getInstance().isAcked(msgId);
	}
	
	public static boolean checkLoginPassed(String msgId) {
		return LoginFactory.getInstance().isPassed(msgId);
	}
	
	public static int getLoginRespCode(String msgId) {
		return LoginFactory.getInstance().getAckCode(msgId);
	}
	
	public static String getLoginToken(String msgId) {
		return LoginFactory.getInstance().getToken(msgId);
	}
	
	public static void sendChatMsg(int userId,int userBId,int chatType,String content,String token,String msgId) {
		ChatMsg.Builder builder = ChatMsg.newBuilder();
		ChatMsg chatMsg = builder.setUserIdSrc(userId)
			.setUserIdDesc(userBId)
			.setChatType(chatType)
			.setContent(content)
			.setMsgId(msgId)
			.setToken(token)
			.setSendTime(System.currentTimeMillis()).build();
		//客户端发送的时间从s值改为ms值
		ServerMsg msg = new ServerMsg();
		msg.setMsgType(MsgType.CHAT);
		msg.setProtoMsgContent(chatMsg);
		//这里应该将发送时间和msgId存入发送消息中
		
		ClientUtil.chatMsgs.put(chatMsg.getMsgId(), chatMsg.getSendTime());
		if(channel == null) {
			throw new RuntimeException("can't connect to the server");
		}
		channel.writeAndFlush(msg);
	}
	
	public static void askOfflineMsg(int userId,String token,String msgId) {
		AskOfflineMsg.Builder builder = AskOfflineMsg.newBuilder();
		AskOfflineMsg askMsg = builder.setUserId(userId)
			.setToken(token)
			.setMsgId(msgId).build();
		ServerMsg msg = new ServerMsg();
		msg.setMsgType(MsgType.ASK_OFFLINEMSG);
		msg.setProtoMsgContent(askMsg);
		if(channel == null) {
			throw new RuntimeException("can't connect to the server");
		}
		channel.writeAndFlush(msg);
	}
	
	public static AckResponse syncWriteMsg(final MessageLite msg, final long timeout) throws InterruptedException, ExecutionException, TimeoutException {
		if(channel == null) {
			throw new RuntimeException("can't connect to the server");
		}
		if(msg == null) {
			throw new NullPointerException("msg is null");
		}
		if(timeout <= 0) {
			throw new IllegalArgumentException("the timeout can't be negative!");
		}
		if(msg instanceof RegisterMsg) {
			RegisterMsg reMsg = (RegisterMsg) msg;
			WriteFuture<AckResponse> future = new SyncWriteFuture(reMsg.getMsgId());
			syncWriteMsgs.put(reMsg.getMsgId(), future);
			
			AckResponse response = _doSyncWriteMsg(MsgType.REGISTER,msg,timeout,future,reMsg.getMsgId());
			syncWriteMsgs.remove(reMsg.getMsgId());
			return response;
		} else if(msg instanceof LoginMsg) {
			LoginMsg loginMsg = (LoginMsg) msg;
			WriteFuture<AckResponse> future = new SyncWriteFuture(loginMsg.getMsgId());
			syncWriteMsgs.put(loginMsg.getMsgId(), future);
			
			AckResponse response = _doSyncWriteMsg(MsgType.LOGIN,msg,timeout,future,loginMsg.getMsgId());
			syncWriteMsgs.remove(loginMsg.getMsgId());
			return response;
		}
		return null;
	}

	private static AckResponse _doSyncWriteMsg(final String type,final MessageLite msg,final long timeout,
			final WriteFuture<AckResponse> writeFuture,final String msgId) throws InterruptedException, ExecutionException, TimeoutException {
		ServerMsg serverMsg = new ServerMsg();
		serverMsg.setMsgType(type);
		serverMsg.setProtoMsgContent(msg);
		channel.writeAndFlush(serverMsg).addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				writeFuture.setWriteResult(future.isSuccess());
				writeFuture.setCause(future.cause());
				if(!writeFuture.isWriteSuccess()) {
					syncWriteMsgs.remove(msgId);
				}
			}
		});
		AckResponse response = writeFuture.get(timeout, TimeUnit.MILLISECONDS);
		return response;
	}
	
	public static void uploadLoc(int userId,String token,String msgId, int locationType,double longitude,double latitude,String mapId,String floor,long updateTime) {
		if(channel == null) {
			throw new RuntimeException("can't connect to the server");
		}
		UploadLocMsg.Builder builder = UploadLocMsg.newBuilder();
		UploadLocMsg locMsg = builder.setUserId(userId)
			.setToken(token)
			.setMsgId(msgId)
			.setLocationType(locationType)
			.setLongitude(longitude)
			.setLatitude(latitude)
			.setMapId(mapId)
			.setFloor(floor)
			.setUpdateTime(updateTime).build();
		ServerMsg msg = new ServerMsg();
		msg.setMsgType(MsgType.UPLOAD_LOCATION);
		msg.setProtoMsgContent(locMsg);
		channel.writeAndFlush(msg);
	}
	
	public static void askLoc(int userIdSrc,int userIdDesc,String token,String msgId, int locationType) {
		if(channel == null) {
			throw new RuntimeException("can't connect to the server");
		}
		AskLocMsg.Builder builder = AskLocMsg.newBuilder();
		AskLocMsg askLocMsg = builder.setUserIdSrc(userIdSrc)
			.setUserIdDesc(userIdDesc)
			.setToken(token)
			.setMsgId(msgId)
			.setLocationType(locationType).build();
		ServerMsg msg = new ServerMsg();
		msg.setMsgType(MsgType.ASK_LOCATION);
		msg.setProtoMsgContent(askLocMsg);
		channel.writeAndFlush(msg);
	}
}
