package cn.david.android.client;

import com.google.protobuf.GeneratedMessage;

import cn.david.domain.ReceiveMsg;

public class MsgTypeAndIdFilter implements MsgFilter<ReceiveMsg> {
	private Class<? extends GeneratedMessage> msgType;
	private String msgId;
	
	public MsgTypeAndIdFilter(Class< ? extends GeneratedMessage> msgType,String msgId) {
		if(!GeneratedMessage.class.isAssignableFrom(msgType)) {
			throw new IllegalArgumentException("Input msgType must be a sub-class of MessageLite.");
		}
		this.msgType = msgType;
		this.msgId = msgId;
	}

	@Override
	public boolean accpet(ReceiveMsg msg) {
		if(msgId.equals(msg.getMsgId()) && (msgType.isInstance(msg.getMsgContent()))) {
			return true;
		}else {
			return false;
		}
	}

}
