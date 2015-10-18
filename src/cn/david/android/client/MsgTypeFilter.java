package cn.david.android.client;

import com.google.protobuf.GeneratedMessage;

import cn.david.domain.ReceiveMsg;

public class MsgTypeFilter implements MsgFilter<ReceiveMsg> {
	private Class<? extends GeneratedMessage> clazzType;
	
	public MsgTypeFilter(Class< ? extends GeneratedMessage> clazzType) {
		if(!GeneratedMessage.class.isAssignableFrom(clazzType)) {
			throw new IllegalArgumentException("Input msgType must be a sub-class of MessageLite.");
		}
		this.clazzType = clazzType;
	}
	@Override
	public boolean accpet(ReceiveMsg msg) {
		if(clazzType.isInstance(msg.getMsgContent())) {
			return true;
		}else {
			return false;
		}
	}

}
