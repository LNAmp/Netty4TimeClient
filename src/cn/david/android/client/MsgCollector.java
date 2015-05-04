package cn.david.android.client;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import cn.david.domain.ReceiveMsg;

public class MsgCollector {
	private MsgFilter<ReceiveMsg> msgFilter;
	private ArrayBlockingQueue<ReceiveMsg> resultQueue;
	private Connection conn;
	private boolean cancelled = false;
	
	
	public MsgCollector(Connection conn, MsgFilter<ReceiveMsg> msgFilter) {
		this.conn = conn;
		this.msgFilter = msgFilter;
		this.resultQueue = new ArrayBlockingQueue<ReceiveMsg>(100);
	}
	
	public void cancle() {
		if(!cancelled) {
			cancelled = true;
			conn.removeMsgCollector(this);
		}
	}
	
	public MsgFilter<ReceiveMsg> getMsgFilter() {
		return msgFilter;
	}
	
	public ReceiveMsg pollResult() {
		return resultQueue.poll();
	}
	
	public ReceiveMsg nextResult() {
		try {
			return resultQueue.take();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public ReceiveMsg nextResult(long timeout) {
		try {
			return resultQueue.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void processMsg(ReceiveMsg msg) {
		if(msg == null) {
			return;
		}
		if(msgFilter == null || msgFilter.accpet(msg)) {
			//如果结果队列满了，则从头删除数据
			while(!resultQueue.offer(msg)) {
				resultQueue.poll();
			}
		}
	}
}
