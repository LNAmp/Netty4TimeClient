package cn.david.android.client;

import cn.david.domain.ReceiveMsg;

public interface MsgListener {
	
	public void processMsg(ReceiveMsg msg);
	
}
