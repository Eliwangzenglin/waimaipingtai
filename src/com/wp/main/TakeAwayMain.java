package com.wp.main;

import java.util.HashMap;
import java.util.Properties;

import redis.clients.jedis.JedisPool;

import com.wp.service.TakeAwayService;
import com.wp.utils.HttpThreadPool;
import com.wp.utils.Log;
import com.wp.utils.ServerConfig;
import com.wp.utils.Utils;

/**
 * 
 *获取商铺信息
 */
public class TakeAwayMain {

	private ServerConfig config;
	private static Log log = Log.getLoger();
	private String[] vipList = null;
	public static HashMap<String, String> running = new HashMap<String, String>();
	public JedisPool jpool;

	public int qiChaChaThreadSetNum = 0;

	public static void main(String[] args) throws Exception {
		TakeAwayMain ts = new TakeAwayMain();
		if (args.length > 0) {
			ts.start("", args[0]);
		} else {
			ts.start("", "");
		}
	}

	public void start(String mainFile, String mestag) throws Exception {
		if (mainFile == null || "".equals(mainFile) || mainFile.length() < 1) {
			String filePath = this.getClass().getResource("/").getPath();
			filePath = filePath.substring(1).replace("bin", "conf");
			if (filePath.contains(":")) {
				mainFile = filePath + "server.properties";
			} else {
				mainFile = "/" + filePath + "/" + "server.properties";
			}
		}
		log.loger.info("conf file path = " + mainFile + ",tag =" + mestag);
		start(Utils.loadProps(mainFile), mestag);
	}

	public void start(Properties mainProperties, String mestag)
			throws Exception {
		this.config = new ServerConfig(mainProperties);
		doWork();
	}

	private void doWork() {
		HttpThreadPool.initPool();
		try {
			startWork("0");
			Thread.sleep(1000 * 10);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void startWork(String threaNum) throws InterruptedException {
		for (int i = 0; i < 50; i++) {
			TakeAwayService run = new TakeAwayService();
			Thread thread = new Thread(run);
			thread.start();
			Thread.sleep(3000);
		}
	}
}
