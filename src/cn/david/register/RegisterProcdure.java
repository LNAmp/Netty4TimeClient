package cn.david.register;

import cn.david.domain.AckCode;
import cn.david.domain.MsgType;
import cn.david.domain.ServerMsg;
import cn.david.domain.IMProto.AckMsg;
import cn.david.domain.IMProto.RegisterMsg;
import cn.david.exception.WrongMsgIdException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class RegisterProcdure {
	
	private String msgId;
	
	//是否确认收到了
	private boolean acked;
	//收到的响应码
	private int ackCode;
	
	public RegisterProcdure() {
		this.ackCode = 0;
		this.acked = false;
		this.msgId = "";
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
		return (ackCode == AckCode.REGISTER_SUCCESS);
	}

	public int getAckCode(String msgId) {
		validateMsgId(msgId);
		return ackCode;
	}


	public synchronized void sendRegisterMsg(Channel channel, String username,String password,String email,String msgId) {
		this.msgId = msgId;
		RegisterMsg.Builder msgBuilder = RegisterMsg.newBuilder();
		msgBuilder.setUsername(username)
		.setPassword(password)
		.setEmail(email)
		.setMsgId(msgId);
		ServerMsg msg = new ServerMsg();
		msg.setMsgType(MsgType.REGISTER);
		msg.setProtoMsgContent(msgBuilder.build());
		channel.writeAndFlush(msg);
	}
	
	public synchronized void processRegisterAckMsg(AckMsg ackMsg) {
		validateMsgId(msgId);
		this.ackCode = ackMsg.getResponseCode();
		this.acked = true;
	}
	
	public synchronized RegisterState getRegisterState(String msgId) {
		validateMsgId(msgId);
		RegisterState state = new RegisterState();
		state.setAckCode(ackCode);
		state.setAcked(acked);
		return state;
	}
	
	private void validateMsgId(String msgId)  {
		if(msgId != this.msgId) {
			throw new WrongMsgIdException();
		}
	}
	
}
