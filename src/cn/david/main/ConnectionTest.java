package cn.david.main;

import cn.david.android.client.Connection;
import cn.david.exception.ConnectionException;

public class ConnectionTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
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
		//conn.disconnect();
		int i = 0;
		while(i<200) {
			if(!conn.getChannel().isActive()) {
				System.out.println(conn.getChannel().isOpen());
				System.out.println(conn.getChannel().isRegistered());
				System.out.println(conn.getChannel().isWritable());
				System.out.println("channel has been closed.");
			}else {
				//System.out.println("channel is alive.");
			}
			i++;
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
//		String username = "david";
//		String password = "qinhaiou";
//		long timeout = 20000;
//		String token = null;
//		try {
//			token = conn.login(username, password, timeout);
//		} catch (ConnectionException e) {
//			String errMsg = e.getMessage();
//			System.out.println(errMsg);
//			if(errMsg != null) {
//				if(errMsg.contains("No response")) {
//					System.out.println("server error");
//				}else if(errMsg.contains("Error Code")) {
//					System.out.println("用户名和密码错误");
//				}
//			}
//		}
//		System.out.println("token:"+token);
	}

}
