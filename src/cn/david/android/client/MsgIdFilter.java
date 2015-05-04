package cn.david.android.client;

import cn.david.domain.ReceiveMsg;

public class MsgIdFilter implements MsgFilter<ReceiveMsg> {

	private String msgId;
	public MsgIdFilter(String msgId) {
		this.msgId = msgId;
	}
	@Override
	public boolean accpet(ReceiveMsg msg) {
		if(msgId.equals(msg.getMsgId())) {
			return true;
		}else{
			return false;
		}
	}
	
}
