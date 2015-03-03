package cn.david.register;

public class RegisterFactory {
	
	private static volatile RegisterProcdure rp ;
	
	private RegisterFactory() {
		
	}
	
	public static RegisterProcdure getInstance() {
		if(rp == null) {
			synchronized (RegisterFactory.class) {
				if(rp == null) {
					rp = new RegisterProcdure();
				}
			}
		}
		return rp;
	}
	
}
