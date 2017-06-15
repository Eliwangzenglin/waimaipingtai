package com.wp.utils;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Log {
	/**
	 * @author janeky Log演示程序
	 */
	// Logger实例
	public Logger loger; 
	public Logger cpuLoger;
	public Logger errordataLogger;
	public Logger zkLogger;
	public Logger copyLogger;
	// 将Log类封装成单实例的模式，独立于其他类�?以后要用到日志的地方只要获得Log的实例就可以方便使用
	public static Log log;

	// 构�?函数，用于初始化Logger配置�?��的属�?
	private Log() {
		// 获得当前目录路径
		java.net.URL url = this.getClass().getProtectionDomain()
				.getCodeSource().getLocation();
		// System.out.println(url.getPath());
		String jarpath = url.getPath();
		String path = jarpath.substring(0, jarpath.lastIndexOf("/"));
		path = path.replace("bin", "conf");
		// 获得日志类loger的实�?
		loger = Logger.getLogger("R"); 
		// loger�?��的配置文件路�?
		PropertyConfigurator.configure(path + "/log4j.properties");
	}

	public static Log getLoger() {
		if (log != null)
			return log;
		else
			return new Log();
	}
	
	
	// 测试函数
	public static void main(String args[]) {
		Log log = Log.getLoger();
		try {
			// 引发异常 
			log.cpuLoger.info("i am cpu logger");
		} catch (Exception e) {
		}
	}
	 
}
