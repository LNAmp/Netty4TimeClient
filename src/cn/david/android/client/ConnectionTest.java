package cn.david.android.client;

import java.net.ConnectException;

import org.junit.Test;

import cn.david.exception.ConnectionException;

public class ConnectionTest {

	@Test
	public void test1() {
		Connection conn = new Connection();
		try {
			conn.connect();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ConnectionException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		String username = "david";
		String password = "qinhaiou";
		long timeout = 5000;
		String token = null;
		try {
			token = conn.login(username, password, timeout);
		} catch (ConnectionException e) {
			String errMsg = e.getMessage();
			System.out.println(errMsg);
			if(errMsg != null) {
				if(errMsg.contains("No response")) {
					System.out.println("server error");
				}else if(errMsg.contains("Error Code")) {
					System.out.println("用户名和密码错误");
				}
			}
		}
		System.out.println("token:"+token);
	}
	
}
