package cn.david.android.client;

import java.util.logging.Logger;

import cn.david.domain.ReceiveMsg;

public class LoggingListener implements MsgListener {

	private Logger logger = Logger.getLogger(Thread.currentThread().getName());
	
	@Override
	public void processMsg(ReceiveMsg msg) {
		if(msg == null) {
			return;
		}
		logger.info("LoggerListener: MsgType:"+msg.getMsgType()+"MsgId:"+msg.getMsgId()+
				"MsgContent:"+msg.getMsgContent().toString());
	}
}
