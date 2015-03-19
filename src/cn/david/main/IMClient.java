package cn.david.main;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import cn.david.client.util.ClientUtil;
import cn.david.domain.AckResponse;
import cn.david.domain.ChatType;
import cn.david.domain.LocationType;
import cn.david.domain.IMProto.LoginMsg;

public class IMClient {

	 private static Logger logger = Logger.getLogger(IMClient.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ClientUtil.initBootstrap();
		try {
			ClientUtil.connectServer(null);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		String msgId = UUID.randomUUID().toString();
//		ClientUtil.registerAccount("david", "qinqinhaiou", "aaaaaa@a.com", msgId);
//		while(true) {
//			if(ClientUtil.checkRegisterAcked(msgId)) {
//				logger.info("the resp is " + ClientUtil.getRegisterRespCode(msgId));
//			}
//		}
//		String msgId = UUID.randomUUID().toString();
//		//进程间通信，关于netty客户端同步发送问题需不断修改
//		ClientUtil.loginAccount("david", "qinqinhaiou", msgId);
//		boolean flag  = true;
//		while(flag) {
//			if(ClientUtil.checkLoginAcked(msgId)) {
//				logger.info("the resp is " + ClientUtil.getLoginRespCode(msgId));
//				flag = false;
//			}
//		}
//		String token = "";
//		if(ClientUtil.checkLoginPassed(msgId)) {
//			token = ClientUtil.getLoginToken(msgId);
//		}
//		String content = "hello,this is the first test!";
//		int userId = 1;
//		String chatMsgId = userId + UUID.randomUUID().toString();
//		ClientUtil.sendChatMsg(1, 2, ChatType.TEXT, content, token, chatMsgId);
//		content = "hello,this is the second test!";
//		chatMsgId = userId + UUID.randomUUID().toString();
//		ClientUtil.sendChatMsg(1, 2, ChatType.TEXT, content, token, chatMsgId);
		
		
//		String remsgId = UUID.randomUUID().toString();
//		ClientUtil.registerAccount("yiqian", "qinqinhaiou", "aaaaaa@a.com", remsgId);
//		boolean f = true;
//		while(f) {
//			if(ClientUtil.checkRegisterAcked(remsgId)) {
//				logger.info("the resp is " + ClientUtil.getRegisterRespCode(remsgId));
//				f = false;
//			}
//		}
//		String msgId = UUID.randomUUID().toString();
//		//进程间通信，关于netty客户端同步发送问题需不断修改
//		ClientUtil.loginAccount("yiqian", "qinqinhaiou", msgId);
//		boolean flag  = true;
//		while(flag) {
//			if(ClientUtil.checkLoginAcked(msgId)) {
//				logger.info("the resp is " + ClientUtil.getLoginRespCode(msgId));
//				String token = ClientUtil.getLoginToken(msgId);
//				logger.info("the token is " + token);
//				flag = false;
//			}
//		}
//		String token = "";
//		if(ClientUtil.checkLoginPassed(msgId)) {
//			token = ClientUtil.getLoginToken(msgId);
//		}
//		int userBId = 2;
//		String askMsgId = userBId + UUID.randomUUID().toString();
//		ClientUtil.askOfflineMsg(userBId, token, askMsgId);
		
		String msgId = UUID.randomUUID().toString();
		String username = "david";
		String password = "qinqinhaiou";
		LoginMsg.Builder builder = LoginMsg.newBuilder();
		builder.setMsgId(msgId);
		builder.setUsername(username);
		builder.setPassword(password);
		LoginMsg loginMsg = builder.build();
		//unit is millisecond
		AckResponse response = null;
		try {
			response = ClientUtil.syncWriteMsg(loginMsg, 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(response);
		String token = response.getToken();
		
//		String content = "hello,this is the first test!";
//		int userId = 1;
//		String chatMsgId = userId + UUID.randomUUID().toString();
//		ClientUtil.sendChatMsg(1, 2, ChatType.TEXT, content, token, chatMsgId);
//		content = "hello,this is the second test!";
//		chatMsgId = userId + UUID.randomUUID().toString();
//		ClientUtil.sendChatMsg(1, 2, ChatType.TEXT, content, token, chatMsgId);
		
		//上传位置
		int userId = 2;
		String locMsgId = userId + "_" + UUID.randomUUID().toString();
		int locationType = LocationType.INDOOR;
		double longitude = 122.08;
		double latitude = 28.99;
		String mapId = "FF4";
		String floor = "2";
		long updateTime = System.currentTimeMillis();
		
		
		
		//ClientUtil.uploadLoc(userId, token, locMsgId, locationType, longitude, latitude, mapId, floor, updateTime);
		//System.out.println("uploadLoc...");
		int userIdSrc = 1;
		int userIdDesc = 2;
		String askLocMsgId = userIdSrc + "_" + UUID.randomUUID().toString();
		ClientUtil.askLoc(userIdSrc, userIdDesc, token, askLocMsgId, locationType);
		System.out.println("askLoc......");
		
		
		
	}

}
