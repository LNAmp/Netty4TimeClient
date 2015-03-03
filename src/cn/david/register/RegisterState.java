package cn.david.register;

public class RegisterState {
	//是否收到响应
	private boolean acked;
	//收到的响应码
	private int ackCode;
	public boolean isAcked() {
		return acked;
	}
	public void setAcked(boolean acked) {
		this.acked = acked;
	}
	public int getAckCode() {
		return ackCode;
	}
	public void setAckCode(int ackCode) {
		this.ackCode = ackCode;
	}
	
	
}
