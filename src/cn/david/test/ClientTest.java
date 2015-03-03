package cn.david.test;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import cn.david.client.util.ClientUtil;

public class ClientTest {
	
	Logger logger = Logger.getLogger(ClientTest.class);
	@Before
	public void initClient() {
		ClientUtil.initBootstrap();
		try {
			ClientUtil.connectServer(null);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.info("Init the client.");
	}
	
	@Test
	public void registerTest() {
		String msgId = UUID.randomUUID().toString();
//		ClientUtil.registerAccount("david", "", "", msgId);
//		while(ClientUtil.cheakRegisterAcked(msgId)) {
//			logger.info("the resp is " + ClientUtil.getRegisterRespCode(msgId));
//		}
	}
}
