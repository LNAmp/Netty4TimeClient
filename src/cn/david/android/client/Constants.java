package cn.david.android.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Constants {
	public static String serverIp;
	public static int serverPort;
	public static String username;
	public static String password;
	public static long loginTimeout;
	
	static {
		InputStream is = Connection.class.getClassLoader().getResourceAsStream("constants.properties");
		Properties prop = new Properties();
		try {
			prop.load(is);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		serverIp = prop.getProperty("serverIp");
		serverPort = Integer.parseInt(prop.getProperty("serverPort"));
		username = prop.getProperty("username");
		password = prop.getProperty("password");
		loginTimeout = Long.parseLong(prop.getProperty("loginTimeout"));
	}
}
