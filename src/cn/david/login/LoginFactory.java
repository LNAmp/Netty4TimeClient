package cn.david.login;

public class LoginFactory {
	private static volatile LoginProcdure lp = null;
	
	private LoginFactory() {
		
	}
	
	public static LoginProcdure getInstance() {
		if(lp == null) {
			synchronized (LoginFactory.class) {
				if(lp == null) {
					lp = new LoginProcdure();
				}
			}
		}
		return lp;
	}
}
