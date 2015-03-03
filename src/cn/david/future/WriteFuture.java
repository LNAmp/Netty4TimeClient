package cn.david.future;

import java.util.concurrent.Future;

public interface WriteFuture<T> extends Future<T> {
	
	Throwable cause();
	
	void setCause(Throwable cause);
	
	boolean isWriteSuccess();
	
	void setWriteResult(boolean result);
	
	String getRequestId();
	
	void setResponse(T response);
	
	T getResponse();
}
