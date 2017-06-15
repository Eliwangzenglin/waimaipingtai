package com.wp.utils;

import org.apache.http.client.CookieStore; 
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory; 
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager; 


import java.security.NoSuchAlgorithmException; 

import javax.net.ssl.SSLContext;

/**
 * 请求连接池的方式
 * @author laolu
 *
 */
public class HttpUtil {
	private static PoolingHttpClientConnectionManager cm;
	private CookieStore cookies = new BasicCookieStore();

	public CookieStore getCookies() {
		return cookies;
	}

	public void setCookies(CookieStore cookies) {
		this.cookies = cookies;
	}
	static {
		LayeredConnectionSocketFactory sslsf = null;
		try {
			sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
				.<ConnectionSocketFactory> create().register("https", sslsf)
				.register("http", new PlainConnectionSocketFactory()).build();
		cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		cm.setMaxTotal(1200);//最大连接数
		cm.setDefaultMaxPerRoute(600);//每个网站的并发数
	}

	public static CloseableHttpClient getHttpClient(CookieStore cookies) {
		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultCookieStore(cookies).setConnectionManager(cm)
				.build();
		return httpClient;
	}

}