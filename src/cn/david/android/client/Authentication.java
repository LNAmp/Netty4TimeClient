package cn.david.android.client;

import java.util.UUID;

import cn.david.domain.AckCode;
import cn.david.domain.MsgType;
import cn.david.domain.ReceiveMsg;
import cn.david.domain.ServerMsg;
import cn.david.domain.IMProto.AckMsg;
import cn.david.domain.IMProto.LoginMsg;
import cn.david.exception.ConnectionException;

public class Authentication {
	private Connection connection;
	
	public Authentication(Connection connection) {
		this.connection = connection;
	}
	
	public String authenticate(String username,String password,long timeout) throws ConnectionException   {
		//组装登录消息
		String token = null;
		LoginMsg msg = genLoginMsg(username, password);
		MsgCollector collcetor = connection.createMsgCollector(
				new MsgTypeAndIdFilter(AckMsg.class, msg.getMsgId()));
		ServerMsg serverMsg = new ServerMsg();
		serverMsg.setMsgType(MsgType.LOGIN);
		serverMsg.setProtoMsgContent(msg);
		connection.sendMsg(serverMsg);
		ReceiveMsg recvMsg = collcetor.nextResult(timeout);
		if(recvMsg == null) {
			collcetor.cancle();
			throw new ConnectionException("No response from the server.");
		}else if(recvMsg.getMsgContent() instanceof AckMsg) {
			AckMsg ack = (AckMsg)recvMsg.getMsgContent();
			if(ack.getResponseCode() != AckCode.LOGIN_SUCCESS) {
				collcetor.cancle();
				throw new ConnectionException("Error Code:"+ack.getResponseCode());
			}else {
				token = ack.getToken();
			}
		}
		collcetor.cancle();
		return token;		
	}

	private LoginMsg genLoginMsg(String username, String password) {
		String msgId = UUID.randomUUID().toString();
		LoginMsg.Builder builder = LoginMsg.newBuilder();
		builder.setMsgId(msgId);
		builder.setUsername(username);
		builder.setPassword(password);
		LoginMsg loginMsg = builder.build();
		return loginMsg;
	}
}
