package cn.david.thread;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.apache.log4j.Logger;

import cn.david.client.util.ClientUtil;

/**
 * 将服务器的返回时间和用户的确认返回时间写入文本内
 * 应该设置为后台线程
 * @author david
 *
 */
public class PrintPeriodsThread extends Thread {
	
	Logger logger = Logger.getLogger(PrintPeriodsThread.class);
	private Writer serverPeriodOs;
	private Writer userPeriodOs;
	@Override
	public void run() {
		while(!Thread.interrupted()) {
			logger.info("flush period to file");
			if(serverPeriodOs == null) {
					try {
						serverPeriodOs = new FileWriter("D:\\serverPeriod.txt") ;
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			if(userPeriodOs == null) {
					try {
						userPeriodOs = new FileWriter("D:\\userPeriod.txt") ;
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			//100个数刷新一次
			if(ClientUtil.serverReplyPeriods.size() > 100) {
				for(int i = 0; i < 100 ; i++) {
					Integer period = ClientUtil.serverReplyPeriods.poll();
					try {
						serverPeriodOs.write(period+"");
						serverPeriodOs.write(System.getProperty("line.separator"));
						serverPeriodOs.flush();
					} catch (IOException e) {
						try {
							serverPeriodOs.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						e.printStackTrace();
					}
				}
			}
			
			//100个数刷新一次
			if(ClientUtil.userReplyPeriods.size() > 100) {
				for(int i = 0; i < 100 ; i++) {
					Integer period = ClientUtil.userReplyPeriods.poll();
					try {
						userPeriodOs.write(period+"");
						userPeriodOs.write(System.getProperty("line.separator"));
						userPeriodOs.flush();
					} catch (IOException e) {
						try {
							serverPeriodOs.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						e.printStackTrace();
					}
				}
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
