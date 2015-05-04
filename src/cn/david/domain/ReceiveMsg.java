package cn.david.domain;

import com.google.protobuf.MessageLite;

public class ReceiveMsg {
	
	private String msgType;
	private String msgId;
	
	private MessageLite msgContent;

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public MessageLite getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(MessageLite msgContent) {
		this.msgContent = msgContent;
	}
	
	
}
