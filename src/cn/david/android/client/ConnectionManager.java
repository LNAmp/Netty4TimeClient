package cn.david.android.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import cn.david.domain.ReceiveMsg;
import cn.david.domain.IMProto.NotificationMsg;
import cn.david.exception.ConnectionException;

public class ConnectionManager {

	private ExecutorService executorService;
	private TaskTracker taskTracker;
	private TaskSubmitter taskSubmitter;
	private List<Runnable> taskList;
	private Connection connection;
	private Future<?> futureTask;
	private boolean running = false;
	private Thread reconnThread;
	private NotificationMsgListener pnListener;
	
	private String token;
	
	private Logger logger = Logger.getLogger(ConnectionManager.class);
	
	public static ConnectionManager getInstance() {
		return LazyHolder.connManager;
	}
	
	private static class LazyHolder {
		private static final ConnectionManager connManager = new ConnectionManager();
	}
	
	private ConnectionManager() {
		this.executorService = Executors.newSingleThreadExecutor();
		taskTracker = new TaskTracker();
		taskSubmitter = new TaskSubmitter();
		taskList = new ArrayList<Runnable>();
		reconnThread = new ReconnectionThread();
		pnListener = new NotificationMsgListener(this);
	}
	
	public ExecutorService getExecutorService() {
		return executorService;
	}

	public TaskTracker getTaskTracker() {
		return taskTracker;
	}

	public List<Runnable> getTaskList() {
		return taskList;
	}

	public Future<?> getFutureTask() {
		return futureTask;
	}

	public boolean isConnected() {
		return connection!=null && connection.isConnected();
	}
	
	public boolean isAuthenticated() {
		return connection!= null && connection.isAuthenticated() && connection.isConnected();
	}
	
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	public void connect() {
		logger.info("connect()...");
		addTask(new ConnectTask());
		addTask(new LoginTask());
		logger.info("connect() done...");
		
	}
	
	public void disconnect() {
		logger.info("disconnect()...");
		terminateConnection();
	}
	
	private void terminateConnection() {
		logger.info("terminateConnection()...");
		Runnable runnable = new Runnable() {
			final ConnectionManager connManager = ConnectionManager.this;

			@Override
			public void run() {
				if(connManager.isConnected()) {
					logger.info("terminateConnection()...run()");
					connManager.getConnection().disconnect();
				}
				connManager.runTask();
			}
			
		};
		addTask(runnable);
	}

	public void startReconnection() {
		synchronized (reconnThread) {
			if(!reconnThread.isAlive()) {
				reconnThread.setName("Reconnection Thread.");
				reconnThread.start();
			}
		}
	}
	
	/**
	 * 以下是一些关于task的操作方法
	 * @author 907
	 *
	 */
	public void runTask() {
		logger.info("runTask()...");
		synchronized (taskList) {
			running = false;
			futureTask = null;
			if(!taskList.isEmpty()) {
				Runnable runnable = taskList.get(0);
				taskList.remove(0);
				running = true;
				futureTask = taskSubmitter.submit(runnable);
				if(futureTask == null) {
					taskTracker.decrease();
				}
			}
		}
		taskTracker.decrease();
		logger.info("runTask()...done");
	}

	public void addTask(Runnable runnable) {
		logger.info("addTask()...");
		taskTracker.increase();
		synchronized (taskList) {
			if(taskList.isEmpty() && !running) {
				running = true;
				futureTask = taskSubmitter.submit(runnable);
				if(futureTask == null) {
					taskTracker.decrease();
				}
			}else {
				taskList.add(runnable);
			}
		}
		logger.info("addTask()...done");
	}
	
	public class TaskSubmitter {
		
		public Future<?> submit(Runnable task) {
			Future<?> result = null;
			if(!getExecutorService().isTerminated() && !getExecutorService().isShutdown()
					&& task != null) {
				result = getExecutorService().submit(task);
			}
			return result;
		}
	}
	
	public class TaskTracker {
		public int count;
		public TaskTracker() {
			count = 0;
		}
		
		public void increase() {
			synchronized(getTaskTracker()) {
				getTaskTracker().count++;
				logger.info("Incremented task count to "+ count);
			}
		}
		
		public void decrease() {
			synchronized(getTaskTracker()) {
				getTaskTracker().count--;
				logger.info("Decremented task count to "+ count);
			}
		}
	}
	
	private class ReconnectionThread extends Thread {
		private final ConnectionManager connManager;
		private int waiting = 0;
		
		public ReconnectionThread() {
			this.connManager = ConnectionManager.this;
		}
		
		public void run() {
			try {
				while(!isInterrupted() && !connManager.isAuthenticated()) {
					logger.info("The "+waiting+" time to reconnect,trying to reconnect in " + waiting() + " seconds");
					Thread.sleep((long)waiting() * 1000L);
					connManager.connect();
					waiting++;
					} 
				}catch (InterruptedException e) {
					//在此可以唤醒重连失败的listener；
				}
			
		}

		private int waiting() {
			if(waiting > 20) {
				return 600;
			}
			if(waiting > 13) {
				return 300;
			}
			return waiting <= 7 ? 10 : 60;
		}
	}
	
	private class ConnectTask implements Runnable {
		
		private final ConnectionManager connManager;
		
		public ConnectTask() {
			this.connManager = ConnectionManager.this;
		}
		@Override
		public void run() {
			logger.info("ConnectTask.run()...");
			if(!connManager.isConnected()) {
				Connection connection = new Connection();
				connManager.setConnection(connection);
				try {
					connection.connect();
					logger.info("Connected to server successfully.");
				} catch (InterruptedException e) {
					//e.printStackTrace();
				} catch (ConnectionException e) {
					logger.info("Connect failed,cause message:"+e.getMessage());
					//可以添加连接失败的listener；
					//e.printStackTrace();
				} finally {
					//不管是否发生错误，都应该运行下一个任务；
					connManager.runTask();
				}
			}else {
				logger.info("Connected to server already.");
				connManager.runTask();
			}
		}
		
	}
	
	//中间省略的ResiterTask
	
	private class LoginTask implements Runnable {
		private final ConnectionManager connManager;
		
		public LoginTask() {
			this.connManager = ConnectionManager.this;
		}

		@Override
		public void run() {
			logger.info("LoginTask.run()...");
			
			if(!connManager.isAuthenticated()) {
				String username = Constants.username;
				String password = Constants.password;
				long timeout = Constants.loginTimeout;
				String token = null;
				try {
					token = connManager.getConnection().login(username, password, timeout);
					//System.out.println("reToken:"+token);
					MsgFilter<ReceiveMsg> pnFilter = new MsgTypeFilter(NotificationMsg.class);
					connManager.getConnection().addRecvMsgListener(pnListener, pnFilter);
				} catch (ConnectionException e) {
					String errMsg = e.getMessage();
					//System.out.println(errMsg);
					if(errMsg != null) {
						if(errMsg.contains("No response")) {
							logger.info("LoginError:server error");
							// 启动重新连接
							connManager.startReconnection();
						}else if(errMsg.contains("Error Code")) {
							logger.info("LonginError:username or pwd was wrong");
							//在此可以唤醒处理该错误的listener
						}
					}
				} finally {
					connManager.runTask();
				}
				connManager.setToken(token);
			}else {
				logger.info("Logged in already.");
				connManager.runTask();
			}
		}
		
	}
}
