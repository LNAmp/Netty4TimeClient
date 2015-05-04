package cn.david.android.client;

public interface MsgFilter<T> {
	public boolean accpet(T msg);
}
