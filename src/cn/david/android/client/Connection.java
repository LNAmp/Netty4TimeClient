package cn.david.android.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.ConnectException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import cn.david.domain.ReceiveMsg;
import cn.david.domain.ServerMsg;
import cn.david.exception.ConnectionException;
import cn.david.handler.ClientMsgHandler;
import cn.david.handler.IMServerInitializer;

public class Connection {
	//netty客户端与服务器建立连接后得到
	private Channel channel;
	//netty客户端配置
	private static Bootstrap bootstrap;
	//服务器IP和host
	private String serverIp = Constants.serverIp;
	private int serverPort = Constants.serverPort;
	
	private boolean connected = false;
	private boolean authenticated = false;
	private boolean wasAuthenticated = false;
	
	private LoginInfo loginInfo;
	
	public boolean isConnected() {
		return (channel != null) && (channel.isActive()) && connected;
	}
	
	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setWasAuthenticated(boolean wasAuthenticated) {
		this.wasAuthenticated = wasAuthenticated;
	}

	
	public Channel getChannel() {
		return channel;
	}


	//属于该connection的静态属性
	static {
		EventLoopGroup group = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		bootstrap.group(group)
         .channel(NioSocketChannel.class)
         .option(ChannelOption.TCP_NODELAY, true)
         .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
         .handler(new IMServerInitializer());
	}
	

	
	protected final Collection<MsgCollector> collectors = new ConcurrentLinkedQueue<MsgCollector>();
	
	protected final Map<MsgListener,ListenerWrapper> recvListeners = 
		new ConcurrentHashMap<MsgListener, ListenerWrapper>();
	
	protected final Map<MsgListener,ListenerWrapper> sendListeners = 
		new ConcurrentHashMap<MsgListener, ListenerWrapper>();
	
	
	public MsgCollector createMsgCollector(MsgFilter<ReceiveMsg> filter) {
		MsgCollector collector = new MsgCollector(this,filter);
		collectors.add(collector);
		return collector;
	}
	
	public Collection<MsgCollector> getMsgCollectors() {
		return collectors;
	}
	
	public boolean removeMsgCollector(MsgCollector collector) {
		return collectors.remove(collector);
	}
	
	public void addRecvMsgListener(MsgListener listener, MsgFilter<ReceiveMsg> msgFilter) {
		if(listener == null) {
			throw new NullPointerException("listener is null");
		}
		ListenerWrapper wrapper = new ListenerWrapper(listener, msgFilter);
		recvListeners.put(listener, wrapper);
	}
	
	public void removeRecvMsgListener(MsgListener listener) {
		recvListeners.remove(listener);
	}
	
	public Map<MsgListener, ListenerWrapper> getRecvListeners() {
		return recvListeners;
	}
	
	public void addSendMsgListener(MsgListener listener, MsgFilter<ReceiveMsg> msgFilter) {
		if(listener == null) {
			throw new NullPointerException("listener is null");
		}
		ListenerWrapper wrapper = new ListenerWrapper(listener, msgFilter);
		sendListeners.put(listener, wrapper);
	}
	
	public void removeSendMsgListener(MsgListener listener) {
		sendListeners.remove(listener);
	}
	
	public Map<MsgListener, ListenerWrapper> getSendListeners() {
		return sendListeners;
	}
	
	public void fireMsgSendListeners(ReceiveMsg msg) {
		for(ListenerWrapper wrapper : sendListeners.values()) {
			wrapper.notifyListener(msg);
		}
	}
	
	
	//待完成的问题，连接的处理，IO异常的处理，如何将connection注入到msgHandler中
	//应该有连接登录和注册方法提供
	//由于客户端请求量不大，暂时不考虑采用发送消息队列
	
	public synchronized void connect() throws InterruptedException, ConnectionException {
		ChannelFuture channelFuture = null;
		try {
			//此处连接采用同步操作,bootstrap中设置有连接超时
			channelFuture = bootstrap.connect(serverIp, serverPort).sync();
		} catch (InterruptedException e) {
			throw e;
		} catch(Exception e) {
			//此处可以添加服务器不可达的listener;
			throw new ConnectionException(e.getMessage());
		}
		if(channelFuture != null) {
			if(channelFuture.isDone() && channelFuture.cause()!=null) {
				//此处可以添加未知错误的listener;
				throw new ConnectionException("Connection Error:",channelFuture.cause());
			}else if(channelFuture.isDone() && channelFuture.isCancellable()) {
				//此处可以添加取消连接的listener;
			}else if(channelFuture.isDone() && channelFuture.isSuccess()) {
				//此处可以添加成功连接的listener;
				//初始化连接操作
				initConnection(channelFuture);
			}
		}
	}

	private void initConnection(ChannelFuture channelFuture) {
		//得到channel
		channel = channelFuture.channel();
		//设置已连接标志位
		connected = true;
		addRecvMsgListener(new LoggingListener(), null);
		//将connection置入到handler中
		ClientMsgHandler msgHandler = (ClientMsgHandler)channel.pipeline().get("msgHandler");
		if(msgHandler == null) {
			throw new RuntimeException("msgHandler is null.");
		}else {
			msgHandler.setConnection(this);
		}
	}
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @return token
	 * @throws ConnectException (两种类型错误，一种是无法连接收不到服务器数据，另一种是参数错误)
	 */
	public synchronized String login(String username,String password,long timeout) throws ConnectionException {
		if(!isConnected()) {
			throw new IllegalStateException("Not connected to server.");
		}
		if(authenticated) {
			throw new IllegalStateException("Already logged in to server.");
		}
		String token = new Authentication(this).authenticate(username, password,timeout);
		if(token != null) {
			authenticated = true;
			if(loginInfo == null) {
				loginInfo = new LoginInfo();
			}
			loginInfo.setUsername(username);
			loginInfo.setPassword(password);
			loginInfo.setToken(token);
			return token;
		}else {
			authenticated = false;
			throw new RuntimeException("Unknown error.");
		}
	}
	
	public synchronized boolean register(String username, String email,String password,long timeout) throws ConnectionException{
		if(!isConnected()) {
			throw new IllegalStateException("Not connected to server.");
		}
		boolean result = new Registration(this).register(username, email, password, timeout);
		return result;
	}
	
	public synchronized void sendMsg(ServerMsg msg) throws ConnectionException {
		if(isConnected()) {
			channel.writeAndFlush(msg);
		}else{
			throw new ConnectionException("Not connected to server.");
		}
	}
	
	public synchronized void disconnect() {
		ChannelFuture closeFuture = channel.close();
		closeFuture.addListener(new ChannelFutureListener() {
			//对于关闭的处理
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				connected = false;
				authenticated = false;
				Connection.this.setWasAuthenticated(authenticated);
				//channel = null;
			}
		});
	}

	public static class ListenerWrapper {
		private MsgListener msgListener;
		private MsgFilter<ReceiveMsg> msgFilter;
		
		public ListenerWrapper(MsgListener msgListener,MsgFilter<ReceiveMsg> msgFilter) {
			this.msgFilter = msgFilter;
			this.msgListener = msgListener;
		}
		
		public void notifyListener(ReceiveMsg msg) {
			if(msgFilter == null || msgFilter.accpet(msg)) {
				msgListener.processMsg(msg);
			}
		}
	}
}
