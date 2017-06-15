package com.wp.utils;

import java.util.HashMap;
import java.util.Map;
 


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis工具类,用于获取RedisPool. 参考官网说明如下： You shouldn't use the same instance from
 * different threads because you'll have strange errors. And sometimes creating
 * lots of Jedis instances is not good enough because it means lots of sockets
 * and connections, which leads to strange errors as well. A single Jedis
 * instance is not threadsafe! To avoid these problems, you should use
 * JedisPool, which is a threadsafe pool of network connections. This way you
 * can overcome those strange errors and achieve great performance. To use it,
 * init a pool: JedisPool pool = new JedisPool(new JedisPoolConfig(),
 * "localhost"); You can store the pool somewhere statically, it is thread-safe.
 * JedisPoolConfig includes a number of helpful Redis-specific connection
 * pooling defaults. For example, Jedis with JedisPoolConfig will close a
 * connection after 300 seconds if it has not been returned.
 * 
 * @author wujintao
 */
public class JedisUtil {
	/**
	 * 私有构造器.
	 */
	private static String ip;
	private static int port;
	private static int maxActive;
	private static int maxIdle;
	private static long maxWait;
	private static int timeOut;
	public static  JedisPool  qqRedisPool= JedisDb.getPool("127.0.0.1", 5387); 
	public static  JedisPool  qqRedisPool_int= JedisDb.getPool("127.0.0.1", 5567); 
	public static HashMap<String,String> tempHamp;
	JedisUtil() {
	}

	private static Map<String, JedisPool> maps = new HashMap<String, JedisPool>();

	/**
	 * 获取连接池. *如果你遇到 java.net.SocketTimeoutException: Read timed out
	 * exception的异常信息 请尝试在构造JedisPool的时候设置自己的超时值. JedisPool默认的超时时间是2秒(单位毫秒)
	 * 
	 * @return 连接池实例
	 */
	public static JedisPool getPool() {
		return getPool(ip, port);
	}

	private static JedisPool getPool(String ip, int port) {
		String key = ip + ":" + port;
		JedisPool pool = null;
		if (!maps.containsKey(key)) {
			long st=System.currentTimeMillis();			
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxActive(maxActive);
			config.setMaxIdle(maxIdle);
			config.setMaxWait(maxWait);
			config.setTestOnBorrow(true);
			config.setTestOnReturn(true);
			try {
				/**
				 *如果你遇到 java.net.SocketTimeoutException: Read timed out
				 * exception的异常信息 请尝试在构造JedisPool的时候设置自己的超时值.
				 * JedisPool默认的超时时间是2秒(单位毫秒)
				 */ 
				String pass="123456";
				pool = new JedisPool(config, ip, port, timeOut,pass);
				maps.put(key, pool);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			long uuu=System.currentTimeMillis()-st;
			
			System.out.println("---------------------get redis pool,user times(ms)= "+uuu);
		} else {
			pool = maps.get(key);
		}
		return pool;
	}

	public  static JedisPool reInitPool(String ip, int port) {
		String key = ip + ":" + port;
		JedisPool pool = null;
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxActive(maxActive);
		config.setMaxIdle(maxIdle);
		config.setMaxWait(maxWait);
		config.setTestOnBorrow(true);
		config.setTestOnReturn(true);
		try {
			/**
			 *如果你遇到 java.net.SocketTimeoutException: Read timed out
			 * exception的异常信息 请尝试在构造JedisPool的时候设置自己的超时值.
			 * JedisPool默认的超时时间是2秒(单位毫秒)
			 */
			pool = new JedisPool(config, ip, port, timeOut);
			if(pool!=null){
				maps.put(key, pool);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("--------------------- reinit   redis pool error ,"+key);
		}
		
		return pool;
	}
	/**
	 *类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例 没有绑定关系，而且只有被调用到时才会装载，从而实现了延迟加载。
	 */
	private static class RedisUtilHolder {
		// private static JedisUtil instance = new JedisUtil();
		private static JedisUtil instance = new JedisUtil();
	}

	/**
	 *当getInstance方法第一次被调用的时候，它第一次读取
	 * RedisUtilHolder.instance，导致RedisUtilHolder类得到初始化；而这个类在装载并被初始化的时候，会初始化它的静
	 * 态域，从而创建RedisUtil的实例，由于是静态的域，因此只会在虚拟机装载类的时候初始化一次，并由虚拟机来保证它的线程安全性。
	 * 这个模式的优势在于，getInstance方法并没有被同步，并且只是执行一个域的访问，因此延迟初始化并没有增加任何访问成本。
	 */
	/*
	 * private static JedisUtil getInstance() { return RedisUtilHolder.instance;
	 * }
	 */
	public static JedisUtil getInstance(String myip, int myport,
			int myMaxActive, int myMaxIdle, long myMaxWait,int myTimeOut) {
		ip = myip;
		port = myport;
		maxActive = myMaxActive;
		maxIdle = myMaxIdle;
		maxWait = myMaxWait;
		timeOut=myTimeOut;
		return RedisUtilHolder.instance;
	}

	/**
	 * 获取Redis实例.
	 * 
	 * @return Redis工具类实例
	 */
	public Jedis getJedis() {
		
		return qqRedisPool.getResource();
	}

	/**
	 * 获取Redis实例.
	 * 
	 * @return Redis工具类实例
	 */
	public Jedis getJedis(String ip, int port) {
		Jedis jedis = null;
		long start=System.currentTimeMillis();
		int count = 0;
		do {
			try { 
				JedisPool jpool=getPool(ip, port);
				if(jpool!=null){
					jedis = getPool(ip, port).getResource();
				}
			} catch (Exception e) {
				getPool(ip, port).returnBrokenResource(jedis);
			}
			count++;
		} while (jedis == null && count < 10);
		long ss= System.currentTimeMillis()-start;
		System.out.println(">>>>>>>>>>>>>>>>> get redis resource,user times(ms)= "+ ss);
		return jedis;
	}

	/**
	 * 释放redis实例到连接池.
	 * 
	 * @param jedis
	 *            redis实例
	 */
	public void closeJedis(Jedis jedis) {
		closeJedis(jedis, ip, port);
	}

	/**
	 * 释放redis实例到连接池.
	 * 
	 * @param jedis
	 *            redis实例
	 */
	private void closeJedis(Jedis jedis, String ip, int port) {
		if (jedis != null) {
			getPool(ip, port).returnBrokenResource(jedis);
			getPool(ip, port).returnResource(jedis);
		}
	}
	/**
	 * 释放redis实例到连接池.broken
	 * 
	 * @param jedis
	 *            redis实例
	 */
	public void closeBrokenJedis(Jedis jedis) {
		closeBrokenJedis(jedis, ip, port);
	}
	/**
	 * 释放redis实例到连接池.
	 * 
	 * @param jedis
	 *            redis实例
	 */
	private void closeBrokenJedis(Jedis jedis, String ip, int port) {
		if (jedis != null) { 
			getPool(ip, port).returnBrokenResource(jedis);
		}
	}


}
