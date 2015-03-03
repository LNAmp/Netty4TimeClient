package cn.david.future;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cn.david.domain.AckResponse;

public class SyncWriteFuture implements WriteFuture<AckResponse> {

	private CountDownLatch latch = new CountDownLatch(1);
	private Throwable cause;
	private final String requestId;
	private boolean writeResult;
	private AckResponse response;
	
	public SyncWriteFuture(String requestId) {
		this.requestId = requestId;
		//writeResult = true;
	}
	
	@Override
	public Throwable cause() {
		return cause;
	}

	@Override
	public String getRequestId() {
		return requestId;
	}

	@Override
	public AckResponse getResponse() {
		return response;
	}

	@Override
	public boolean isWriteSuccess() {
		return writeResult;
	}

	@Override
	public void setCause(Throwable cause) {
		this.cause = cause;
	}

	@Override
	public void setResponse(AckResponse response) {
		this.response = response;
		latch.countDown();
	}

	@Override
	public void setWriteResult(boolean result) {
		this.writeResult = result;
	}

	@Override
	public boolean cancel(boolean arg0) {
		return true;
	}

	@Override
	public AckResponse get() throws InterruptedException, ExecutionException {
		latch.await();
		return response;
	}

	@Override
	public AckResponse get(long arg0, TimeUnit arg1)
			throws InterruptedException, ExecutionException, TimeoutException {
		latch.await(arg0, arg1);
		return response;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return false;
	}

}
