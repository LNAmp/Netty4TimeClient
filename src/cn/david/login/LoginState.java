package cn.david.login;

public class LoginState {
	//是否收到响应
	private boolean acked;
	//收到的响应码
	private int ackCode;
	//收到的token
	private String token;
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
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
	
}
