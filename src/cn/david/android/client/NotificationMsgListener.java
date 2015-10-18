package cn.david.android.client;

import org.apache.log4j.Logger;

import cn.david.domain.AckCode;
import cn.david.domain.AckType;
import cn.david.domain.MsgType;
import cn.david.domain.ReceiveMsg;
import cn.david.domain.ServerMsg;
import cn.david.domain.IMProto.AckMsg;
import cn.david.domain.IMProto.NotificationMsg;
import cn.david.exception.ConnectionException;

public class NotificationMsgListener implements MsgListener {

	Logger logger = Logger.getLogger(NotificationMsgListener.class);
	
	private ConnectionManager connManager;
	
	public NotificationMsgListener(ConnectionManager connManager) {
		this.connManager = connManager;
	}
	
	@Override
	public void processMsg(ReceiveMsg msg) {
		if(!(msg.getMsgContent() instanceof NotificationMsg)) {
			throw new IllegalArgumentException("wrong msg type");
		}
		NotificationMsg content = (NotificationMsg) msg.getMsgContent();
		String msgId = content.getMsgId();
		String title = null;
			title = content.getTitle();
		
		logger.info("msgId : "+ msgId + " title: " + title);
		ServerMsg smsg = new ServerMsg();
		AckMsg.Builder builder = AckMsg.newBuilder();
		builder.setAckType(AckType.PUSH)
		.setResponseCode(AckCode.PUSH_RECV)
		.setMsgId(msg.getMsgId())
		.setToken(connManager.getToken());
		smsg.setMsgType(MsgType.ACK);
		smsg.setProtoMsgContent(builder.build());
		try {
			logger.info("Send Ack: " + msg.getMsgId());
			connManager.getConnection().sendMsg(smsg);
		} catch (ConnectionException e) {
			connManager.startReconnection();
			e.printStackTrace();
		}
		
	}

}
