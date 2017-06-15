package com.wp.utils;
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
public class JedisDb {
	/**
	 * 私有构造器.
	 */
	private static Log log = Log.getLoger(); 
	private static String pass = "1234567"; 
	
	public static JedisPool getPool(String ip, int port) {
		JedisPool pool = null;
		int centerRedisMaxActive = 400;
		int centerRedisMaxIdle = 40;
		int timeOut = 60000;
		long centerRedisMaxWait = 60000l; 
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxActive(centerRedisMaxActive);
		config.setMaxIdle(centerRedisMaxIdle);
		config.setMaxWait(centerRedisMaxWait);
		config.setTestOnBorrow(true);
		config.setTestOnReturn(true);
		config.setMinIdle(8);//设置最小空闲
		config.setTestWhileIdle(true);//Idle时进行连接扫描
		config.setTimeBetweenEvictionRunsMillis(5000);//表示idle object evitor两次扫描之间要sleep的毫秒数
		//表示idle object evitor每次扫描的最多的对象数
		config.setNumTestsPerEvictionRun(10);
		//表示一个对象至少停留在idle状态的最短时间，然后才能被idle object evitor扫描并驱逐；这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义
		config.setMinEvictableIdleTimeMillis(10000);

		
		try {
			pool = new JedisPool(config, ip, port, timeOut, pass);
		} catch (Exception e) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return pool;
	}
}
