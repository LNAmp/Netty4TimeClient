package cn.david.android.client;

import java.util.UUID;

import cn.david.domain.AckCode;
import cn.david.domain.MsgType;
import cn.david.domain.ReceiveMsg;
import cn.david.domain.ServerMsg;
import cn.david.domain.IMProto.AckMsg;
import cn.david.domain.IMProto.RegisterMsg;
import cn.david.exception.ConnectionException;

public class Registration {
	
	private Connection connection;
	
	public Registration(Connection connection) {
		this.connection = connection;
	}
	
	public boolean register(String username, String email,String password,long timeout) throws ConnectionException {
		boolean result = false;
		RegisterMsg msg = genRegisterMsg(username,email,password);
		MsgCollector collcetor = connection.createMsgCollector(
				new MsgTypeAndIdFilter(AckMsg.class, msg.getMsgId()));
		ServerMsg serverMsg = new ServerMsg();
		serverMsg.setMsgType(MsgType.REGISTER);
		serverMsg.setProtoMsgContent(msg);
		connection.sendMsg(serverMsg);
		ReceiveMsg recvMsg = collcetor.nextResult(timeout);
		if(recvMsg == null) {
			collcetor.cancle();
			throw new ConnectionException("No response from the server.");
		}else if(recvMsg.getMsgContent() instanceof AckMsg) {
			AckMsg ack = (AckMsg)recvMsg.getMsgContent();
			if(ack.getResponseCode() != AckCode.REGISTER_SUCCESS) {
				throw new ConnectionException("Error Code:"+ack.getResponseCode());
			}else {
				result = true;
			}
		}
		collcetor.cancle();
		return result;
	}

	private RegisterMsg genRegisterMsg(String username, String email,
			String password) {
		String msgId = UUID.randomUUID().toString();
		RegisterMsg.Builder builder = RegisterMsg.newBuilder();
		builder.setUsername(username);
		builder.setEmail(email);
		builder.setMsgId(msgId);
		builder.setPassword(password);
		RegisterMsg regMsg = builder.build();
		return regMsg;
	}
}
