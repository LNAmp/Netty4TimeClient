package cn.david.exception;

public class ConnectionException extends Exception {

	private Throwable wrappedThrowable;
	
	public ConnectionException() {
		// TODO Auto-generated constructor stub
		super();
	}

	public ConnectionException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ConnectionException(Throwable wrappedThrowable) {
		super(wrappedThrowable);
		// TODO Auto-generated constructor stub
		this.wrappedThrowable = wrappedThrowable;
	}

	public ConnectionException(String message, Throwable wrappedThrowable) {
		super(message,wrappedThrowable);
		// TODO Auto-generated constructor stub
		this.wrappedThrowable = wrappedThrowable;
	}

	public ConnectionException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}

	@Override
	public synchronized Throwable getCause() {
		// TODO Auto-generated method stub
		return super.getCause();
	}

	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return super.getMessage();
	}

	
}
