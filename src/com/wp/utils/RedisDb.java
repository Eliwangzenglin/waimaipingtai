package com.wp.utils;

import redis.clients.jedis.JedisPool;

public class RedisDb {
	public static JedisPool qqRedisPool = JedisDb
			.getPool("127.0.0.1", 2380); // QQ业务中间数据缓存服务器
}
