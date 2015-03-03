package cn.david.login;

import io.netty.channel.Channel;
import cn.david.domain.AckCode;
import cn.david.domain.MsgType;
import cn.david.domain.ServerMsg;
import cn.david.domain.IMProto.AckMsg;
import cn.david.domain.IMProto.LoginMsg;
import cn.david.exception.WrongMsgIdException;

public class LoginProcdure {
	
	// 发送的消息id，唯一表示此次loing过程
	private String msgId;
	//是否确认收到了
	private boolean acked;
	//收到的响应码
	private int ackCode;
	//收到的token
	private String token;
	
	//初始化
	public LoginProcdure() {
		this.ackCode = 0;
		this.acked = false;
		this.msgId = "";
		this.token = "";
	}
	
	public String getMsgId() {
		return msgId;
	}
	


	public boolean isAcked(String msgId) {
		validateMsgId(msgId);
		//System.out.println("the acked is " + acked);
		return acked;
	}


	public boolean isPassed(String msgId) {
		validateMsgId(msgId);
		return (ackCode == AckCode.LOGIN_SUCCESS);
	}

	public int getAckCode(String msgId) {
		validateMsgId(msgId);
		return ackCode;
	}
	
	public String getToken(String msgId) {
		validateMsgId(msgId);
		return token;
	}
	
	public synchronized void sendLoginMsg(Channel channel, String username, String password, String msgId ) {
		this.msgId = msgId;
		LoginMsg.Builder msgBuilder = LoginMsg.newBuilder();
		msgBuilder.setUsername(username)
			.setPassword(password)
			.setMsgId(msgId);
		ServerMsg msg = new ServerMsg();
		msg.setMsgType(MsgType.LOGIN);
		msg.setProtoMsgContent(msgBuilder.build());
		channel.writeAndFlush(msg);
	}
	
	public synchronized void processLoginAckMsg(AckMsg ackMsg) {
		validateMsgId(msgId);
		this.ackCode = ackMsg.getResponseCode();
		this.token = ackMsg.getToken(); 
		this.acked = true;
	}
	
	public synchronized LoginState getLoginState(String msgId) {
		validateMsgId(msgId);
		LoginState state = new LoginState();
		state.setAckCode(ackCode);
		state.setAcked(acked);
		state.setToken(token);
		return state;
	}
	
	private void validateMsgId(String msgId)  {
		if(msgId != this.msgId) {
			throw new WrongMsgIdException();
		}
	}
}
