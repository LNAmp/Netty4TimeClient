package cn.david.domain;

public class AckResponse {
	private int ackType;
	private int responseCode;
	private String msgId;
	private String token;
	public int getAckType() {
		return ackType;
	}
	public void setAckType(int ackType) {
		this.ackType = ackType;
	}
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	public String getMsgId() {
		return msgId;
	}
	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	@Override
	public String toString() {
		return "AckResponse [ackType=" + ackType + ", msgId=" + msgId
				+ ", responseCode=" + responseCode + ", token=" + token + "]";
	}
	
	
	
}
