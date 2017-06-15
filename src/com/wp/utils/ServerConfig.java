package com.wp.utils;

import java.util.Properties;

/**
 * Configuration for the servers conif :
 * 
 * @author adyliu (imxylz@gmail.com)
 * @since 1.0
 */
public class ServerConfig {

	protected final Properties props;
	public static final byte CrcLength = 4;

	public ServerConfig(Properties props) {
		this.props = props;
	}

	/**
	 * 获取配置的并发数
	 * 
	 * @return
	 */
	public int getMaxTranTread() {
		return Utils.getInt(props, "num.max.tran.thread");
	}

	/**
	 * 获取配置的并发数
	 * 
	 * @return
	 */
	public int getMaxPutTread() {
		return Utils.getInt(props, "num.max.put.thread");
	}

	/**
	 * @return
	 */
	public String doFilePathLog() {
		return Utils.getString(props, "do.file.log.path");
	}

	public String getRecourcePath() {
		return Utils.getString(props, "dpi.recource.path");
	}

	public String getSaveTempPath() {
		return Utils.getString(props, "save.temp.path");
	}

	public String getSftpIp() {
		return Utils.getString(props, "sftp.ip");
	}

	public int getSftpPort() {
		return Utils.getInt(props, "sftp.port");
	}

	public String getftpType() {
		return Utils.getString(props, "ftp.type");
	}

	public String getSftpFilePath() {
		return Utils.getString(props, "sftp.file.path");
	}

	public String getSftpUser() {
		return Utils.getString(props, "sftp.user");
	}

	public String getSftpPass() {
		return Utils.getString(props, "sftp.pass");
	}

	public boolean getFileIfDelete() {
		return Utils.getBoolean(props, "file.if.delete", false);
	}

	public String getTranClassName() {
		return Utils.getString(props, "tran.class.name");
	}

	public String dpiProtoclId() {
		return Utils.getString(props, "3g.dpi.protocl.id");
	}

	/**
	 * *****************************************************************************************************************
	 * 
	 * @return
	 */
	public String[] getVipList() {
		return Utils.getString(props, "vip.list").split(",");

	}

	/**
	 * *****************************************************************************************************************
	 * 
	 * @return
	 */
	public String getVipListStr() {
		return Utils.getString(props, "vip.list");

	}

	public String getProxyFilePath() {
		return Utils.getString(props, "proxy.file.path");
	}

	public String getSaveFilePath() {
		return Utils.getString(props, "weibo.file.path");
	}

	/**
	 * redis config
	 * 
	 * @return
	 */
	public String getRedisCenterHost() {
		return Utils.getString(props, "redis.center.host");
	}

	public int getRedisCenterPort() {
		return Utils.getInt(props, "redis.center.port");
	}

	public int getFreshCount() {
		return Utils.getInt(props, "weibo.fresh.count");
	}

	public String getRedisQueueKey() {
		return Utils.getString(props, "redis.queue.key");
	}

	/**
	 * 用户粉丝数大于多少不刷 过滤大v
	 * 
	 * @return
	 */
	public int getMaxFreshUserFans() {
		return Utils.getInt(props, "max.fresh.user.fans");
	}

	public int getFreshStopMinitues() {
		return Utils.getInt(props, "fresh.stop.min");
	}

	/**
	 * 
	 * @return
	 */
	public String getQQFreshType() {
		return Utils.getString(props, "qq.fresh.type");
		// return Utils.getInt(props, "qq.fresh.type");
	}

	public int getReadDataFrom() {
		return Utils.getInt(props, "read.data.from");
	}

	public String getDoUserAccount() {
		return Utils.getString(props, "do.user.account");
	}

	public String getHostString() {
		return Utils.getString(props, "local.host.ips");

	}

	public int getAtFriendSize() {
		return Utils.getInt(props, "at.friend.size");
	}

	public int getdfadfadfasdfa() {
		return Utils.getInt(props, "afadfadfa");
	}

	public String getVpnUser() {
		return Utils.getString(props, "vpn.user").trim();
	}

	public String getVpnPass() {
		return Utils.getString(props, "vpn.pass").trim();
	}

}
