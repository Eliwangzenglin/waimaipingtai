package com.wp.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

public class RedisServer {

	public static  JedisPool  qqRedisPool= JedisDb.getPool("127.0.0.1", 1380); 
	public static  JedisPool bizRedisPool = JedisDb.getPool("127.0.0.1", 1321);
	public static  JedisPool  qqRedisPool_int= JedisDb.getPool("127.0.0.1", 1320); 
	public static  JedisPool wcRedisPool = JedisDb.getPool("127.0.0.1", 3380);
	public static  JedisPool mtRedisPool = JedisDb.getPool("127.0.0.1", 4379);
	
	
	public static JedisPool getMtRedisPool() {
		return mtRedisPool;
	}
	public static void setMtRedisPool(JedisPool mtRedisPool) {
		RedisServer.mtRedisPool = mtRedisPool;
	}

	public static JedisUtil redisServer= new JedisUtil();
	public static  JedisPool getPool(String hostPort){
		return qqRedisPool;
	}
	public static void initCenterRedis(){
		
	}
	public static Jedis getJedis(Jedis tempJedis){
		return qqRedisPool.getResource();
	}
	
	public static Jedis getBizJedis(){
		return bizRedisPool.getResource();
	}
	
	public static void returnResource(Jedis tempJedis){
		qqRedisPool.returnResource(tempJedis);
	}
	
	public static void returnBizResource(Jedis jedis){
		bizRedisPool.returnResource(jedis);
	}

	
}
