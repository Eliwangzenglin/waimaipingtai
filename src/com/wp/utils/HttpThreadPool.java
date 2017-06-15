package com.wp.utils;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import redis.clients.jedis.Jedis;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class HttpThreadPool {
	private static CloseableHttpClient httpclient = null;
	private static Log log = Log.getLoger();
	private static CookieStore cookies = new BasicCookieStore();
	public static List<String> proxyMap =null;
	private static Random random = new java.util.Random();// 定义随机类
	public static void initPool() {

		ConnectionSocketFactory plainsf = PlainConnectionSocketFactory
				.getSocketFactory();
		LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory
				.getSocketFactory();
		Registry<ConnectionSocketFactory> registry = RegistryBuilder
				.<ConnectionSocketFactory> create().register("http", plainsf)
				.register("https", sslsf).build();
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
				registry);
		// 设置线程数最大100,如果超过100为请求个数
		cm.setMaxTotal(100);
		// 将每个路由基础的连接增加到20
		cm.setDefaultMaxPerRoute(20);
		// 请求重试处理
		HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
			public boolean retryRequest(IOException exception,
					int executionCount, HttpContext context) {
				if (executionCount >= 1) {// 如果已经重试了1次，就放弃
					return false;
				}
				if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
					return false;
				}
				if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
					return false;
				}
				if (exception instanceof InterruptedIOException) {// 超时
					return false;
				}
				if (exception instanceof UnknownHostException) {// 目标服务器不可达
					return false;
				}
				if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
					return false;
				}
				if (exception instanceof SSLException) {// ssl握手异常
					return false;
				}

				HttpClientContext clientContext = HttpClientContext
						.adapt(context);
				HttpRequest request = clientContext.getRequest();
				// 如果请求是幂等的，就再次尝试
				if (!(request instanceof HttpEntityEnclosingRequest)) {
					return true;
				}
				return false;
			}
		};
		Registry<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder
				.<CookieSpecProvider> create()
				.register(CookieSpecs.STANDARD, new BrowserCompatSpecFactory())
				.build();

		String proxyIP = "proxy.abuyun.com";
		int proxtport = 9010;
		String proxyUser = "HA89436664P3M91P";
		String proxyPass = "DA83F50776E87E63";
		BasicHeader header = new BasicHeader("Proxy-Switch-Ip", "yes");
		List<Header> defaultHeader = new ArrayList<Header>();
		defaultHeader.add(header);
		// 创建认证，并设置认证范围
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(proxyIP, 9010),// 可以访问的范围
				new UsernamePasswordCredentials(proxyUser, proxyPass));// 用户名和密码
		// 代理的设置
		HttpHost proxy = new HttpHost(proxyIP, proxtport);
		RequestConfig config = RequestConfig.custom().setConnectTimeout(5000)
				.setSocketTimeout(5000).setConnectionRequestTimeout(5000)
				.setProxy(proxy).build();
		httpclient = HttpClients.custom()
				.setDefaultCredentialsProvider(credsProvider)
				.setConnectionManager(cm).setDefaultHeaders(defaultHeader)
				.setRetryHandler(httpRequestRetryHandler)
				.setDefaultCookieStore(cookies).setDefaultRequestConfig(config)
				.build();
		// http.setConfig(config);

		/*
		 * httpclient = HttpClients.custom().setConnectionManager(cm)
		 * .setRetryHandler(httpRequestRetryHandler)
		 * .setDefaultCookieStore(cookies).build();
		 */
	}

	private static RequestConfig getAbuyunRequestConfig() {
		String proxyIP = "proxy.abuyun.com";
		int proxtport = 9010;
		String proxyUser = "HA89436664P3M91P";
		String proxyPass = "DA83F50776E87E63";
		BasicHeader header = new BasicHeader("Proxy-Switch-Ip", "yes");
		List<Header> defaultHeader = new ArrayList<Header>();
		defaultHeader.add(header);
		// 创建认证，并设置认证范围
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(proxyIP, 9010),// 可以访问的范围
				new UsernamePasswordCredentials(proxyUser, proxyPass));// 用户名和密码
		// 代理的设置
		HttpHost proxy = new HttpHost(proxyIP, proxtport);

		RequestConfig config = RequestConfig.custom().setConnectTimeout(5000)
				.setSocketTimeout(5000).setConnectionRequestTimeout(5000)
				.setProxy(proxy).build();
		return config;
	}

	private static String getProxyHost(CloseableHttpResponse response) {
		if (response == null || "".equals(response)) {
			return null;
		}
		for (org.apache.http.Header h : response.getAllHeaders()) {
			if ("X-Outbound-Ip".equals(h.getName())) {
				return h.getValue();
			}
		}
		return null;
	}

	public static CloseableHttpClient getHttpclient() {
		if (httpclient == null) {
			initPool();
		}
		return httpclient;
	}

	public static void closeClient() {
		try {
			if (httpclient != null)
				httpclient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取爬虫代理，使用过的代理，将不在使用
	 * 
	 * @return
	 */
	public static RequestConfig getHttpConfigProxyOne(int useCount) {
		Jedis jedis = RedisServer.getJedis(null);
		Map<String, String> proxyMap = jedis.hgetAll("proxy");
		String hostPort = getProxyRandom();
		String[] key = hostPort.split(":");
		log.loger.info("--->proxy,host =" + hostPort.trim());
		String proxyip = key[0];
		int proxyport = Integer.parseInt(key[1].trim());
		HttpHost proxy = new HttpHost(proxyip, proxyport);
		RequestConfig config = RequestConfig.custom().setConnectTimeout(5000)
				.setExpectContinueEnabled(true)
				.setCookieSpec(CookieSpecs.IGNORE_COOKIES)
				.setSocketTimeout(5000).setConnectionRequestTimeout(5000)
				.setProxy(proxy).build();
		RedisServer.redisServer.closeJedis(jedis);
		return config;
	}

	/**
	 * 大螞蟻代理
	 * 
	 * @return
	 */
	private static RequestConfig getHttpConfigAntProxy(HttpPost httppost) {
		HttpHost proxy = new HttpHost("127.0.0.1", 1123, "http");
		RequestConfig config = RequestConfig.custom().setConnectTimeout(5000)
				.setExpectContinueEnabled(true)
				// .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
				.setSocketTimeout(5000).setConnectionRequestTimeout(5000)
				.setProxy(proxy).build();
		return config;
	}

	public static String getProxyAuthHeader() {
		// 定义申请获得的appKey和appSecret
		String appkey = "123";
		String secret = "31233dsadadasda";

		// 创建参数表
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("app_key", appkey);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("GMT+8"));// 使用中国时间，以免时区不同导致认证错误
		paramMap.put("timestamp", format.format(new Date()));

		// 对参数名进行排序
		String[] keyArray = paramMap.keySet().toArray(new String[0]);
		Arrays.sort(keyArray);

		// 拼接有序的参数名-值串
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(secret);
		for (String key : keyArray) {
			stringBuilder.append(key).append(paramMap.get(key));
		}

		stringBuilder.append(secret);
		String codes = stringBuilder.toString();
		// MD5编码并转为大写， 这里使用的是Apache codec
		String sign = DigestUtils.md5Hex(codes).toUpperCase();
		paramMap.put("sign", sign);

		String params = "sign=" + sign + "&app_key=" + appkey + "&timestamp="
				+ format.format(new Date());
		// 拼装请求头Proxy-Authorization的值，这里使用 guava 进行map的拼接
		String authHeader = "MYH-AUTH-MD5 " + params;
		return authHeader;

	}

	private static String getProxyRandom() {
		/*Iterator<String> iter = proxyMap.keySet().iterator();
		int max = proxyMap.size();
		if (max == 0) {
			max = 1;
		}
		int min = 0;
		Random random = new Random();
		int s = random.nextInt(max) % (max - min + 1) + min;
		if(s<1000){
			String hostip = CheckProxyThrable.getGoubanjia();
			if(hostip!=null&&!"".equals(hostip)){
				return hostip.trim();
			}
		}
		
		int count = 0;
		while (iter.hasNext()) {
			String host = iter.next();
			if (count == s) {
				return host;
			}
			count++;
		}*/
		if(proxyMap.size()==0){
			log.loger.info("-----------------------.proxy size =0 .please check .");
			return null;
		}
		int size = random.nextInt(proxyMap.size());
		return proxyMap.get(size);
	}

	/**
	 * 搜狐IP地址查询接口（IP）：http://pv.sohu.com/cityjson 1616
	 * IP地址查询接口（IP+地址）：http://w.1616.net/chaxun/iptolocal.php 126（地址）:
	 * http://ip.ws.126.net/ipquery
	 * 
	 * @return
	 */
	public static String getHost(Jedis jedis) {
		try {
			String url = "http://pv.sohu.com/cityjson";
			HttpGet httpGet = new HttpGet(url);
			String responseStr = executeClient(httpGet, false, "UTF-8", jedis);
			int start = responseStr.indexOf("{");
			JSONObject json = new JSONObject(responseStr.substring(start,
					responseStr.length() - 1));
			return json.getString("cip");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static String getHost(String host) {
		try {
			String url = "http://pv.sohu.com/cityjson";
			HttpGet httpGet = new HttpGet(url);
			String responseStr = executeClient(httpGet, host);
			log.loger.info(responseStr);
			int start = responseStr.indexOf("{");
			JSONObject json = new JSONObject(responseStr.substring(start,
					responseStr.length() - 1));
			return json.getString("cip");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static String checkHostProxy(String ip, int port) {
		CloseableHttpResponse response = null;
		try {
			String url = "http://pv.sohu.com/cityjson";
			HttpGet httpGet = new HttpGet(url);
			HttpHost proxy = new HttpHost(ip, port, "http");
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(3000).setExpectContinueEnabled(true)
					.setSocketTimeout(3000).setProxy(proxy)
					.setConnectionRequestTimeout(3000).build();
			httpGet.setConfig(config);
			response = HttpThreadPool.getHttpclient().execute(httpGet);
			HttpEntity entity = response.getEntity();
			String responseStr = EntityUtils.toString(entity, "UTF-8");
			System.out.println(responseStr);
			int start = responseStr.indexOf("{");
			JSONObject json = new JSONObject(responseStr.substring(start,
					responseStr.length() - 1));
			return json.getString("cip");
		} catch (Exception ex) {
			// ex.printStackTrace();

		} finally {
			try {
				if (response != null) {
					EntityUtils.consumeQuietly(response.getEntity());
					response.getEntity().getContent().close();
					response.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			/*
			 * try { closeableHttpClient.close(); } catch (IOException e) {
			 * e.printStackTrace(); }
			 */
		}
		return null;
	}

	/**
	 * response: 获取微博用户的 id， 关注数，粉丝数
	 * 
	 * @param uid
	 * @param vip
	 * @param obj
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws Exception
	 */
	public static String getWeiboUserInfoCheckProxy(String ip, int port) {
		// 创建HttpClientBuilder

		CloseableHttpResponse response = null;
		try {
			// 依次是目标请求地址，端口号,协议类型
			HttpGet httpPost = new HttpGet("http://m.weibo.cn/u/472768272");
			HttpHost proxy = new HttpHost(ip, port, "http");
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(5000).setExpectContinueEnabled(true)
					.setSocketTimeout(5000).setProxy(proxy)
					.setConnectionRequestTimeout(5000).build();
			httpPost.setConfig(config);
			response = HttpThreadPool.getHttpclient().execute(httpPost);
			HttpEntity entity = response.getEntity();
			String responseStr = EntityUtils.toString(entity, "UTF-8");
			if (responseStr.contains("passport.weibo.cn")) {
				return responseStr;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// 释放资源
				if (response != null) {
					response.close();
				}

			} catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * 
	 * @param httppost
	 * @param isProxy
	 * @param charset
	 *            true 使用代理，false 不使用代理
	 * @return responseStr
	 */

	public static String executeAntProxy(HttpPost httppost, boolean tag,
			String charset) {
		CloseableHttpResponse response = null;
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = null;
		closeableHttpClient = httpClientBuilder.build();
		try {
			HttpHost proxy = new HttpHost("123.56.242.140", 8123, "http");
			RequestConfig config = RequestConfig.custom()
					.setCookieSpec(CookieSpecs.STANDARD)
					.setConnectTimeout(5000).setSocketTimeout(5000)
					.setProxy(proxy).build();
			int runCount = 0;
			String responseStr = "";
			while (true) {
				if (runCount > 20) {
					log.loger
							.error("-------abuyun >>>>>exe 20 count not succ ....return ");
					break;
				}
				try {
					httppost.setConfig(config);
					httppost.setHeader("Proxy-Authorization",
							getProxyAuthHeader());
					response = closeableHttpClient.execute(httppost);

				} catch (Exception ex) {
					ex.printStackTrace();
					continue;
				}
				if (response == null) {
					runCount++;
					continue;
				}
				HttpEntity entity = response.getEntity();
				responseStr = EntityUtils.toString(entity, charset);
				if (responseStr.contains("many requests")) {
					runCount++;
					log.loger.info("ant reponse = " + responseStr);
					Thread.sleep(500);
					continue;
				}
				if (!"".equals(responseStr)) {
					// String host = getProxyHost(response);
					// log.loger.info("end execute ant  httpost....proxy = "
					// + host);
					break;
				} else {
					runCount++;
					// log.loger.error(" responseStr  is kong  do again .... "+
					// runCount);
					//Thread.sleep(1000);
					/*try {
						if (response != null) {
							EntityUtils.consumeQuietly(response.getEntity());
							response.getEntity().getContent().close();
							response.close();
						}
						httppost.releaseConnection();
					} catch (Exception e) {
						e.printStackTrace();
					}*/
				}
			}
			return responseStr;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		} finally {
			try {
				if (response != null) {
					EntityUtils.consumeQuietly(response.getEntity());
					response.getEntity().getContent().close();
					response.close();
				}
				httppost.releaseConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				closeableHttpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 
	 * @param httppost
	 * @param isProxy
	 * @param charset
	 *            true 使用代理，false 不使用代理
	 * @return responseStr
	 */

	public static String executeAntProxy(HttpGet httppost, boolean tag,
			String charset) {
		CloseableHttpResponse response = null;
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = null;
		closeableHttpClient = httpClientBuilder.build();
		try {
			HttpHost proxy = new HttpHost("127.0.0.1", 13123, "http");
			RequestConfig config = RequestConfig.custom()
					.setCookieSpec(CookieSpecs.STANDARD)
					.setConnectTimeout(5000).setSocketTimeout(5000)
					.setProxy(proxy).build();
			int runCount = 0;
			String responseStr = "";
			while (true) {
				if (runCount > 20) {
					log.loger
							.error("-------ant >>>>>exe 20 count not succ ....return ");
					break;
				}
				try {
					httppost.setConfig(config);
					httppost.setHeader("Proxy-Authorization",
							getProxyAuthHeader());
					response = closeableHttpClient.execute(httppost);
				} catch (Exception ex) {
					ex.printStackTrace();
					continue;
				}
				if (response == null) {
					runCount++;
					continue;
				}
				HttpEntity entity = response.getEntity();
				responseStr = EntityUtils.toString(entity, charset);
				if (responseStr.contains("many requests")) {
					runCount++;
					Thread.sleep(500);
					continue;
				}
				if (!"".equals(responseStr)) {
					String host = getProxyHost(response);
					log.loger.info("end execute ant  httpost....proxy = "
							+ host);
					break;
				} else {
					runCount++;
					// log.loger.error(" responseStr  is kong  do again .... "+
					// runCount);
					//Thread.sleep(2000);
				}
			}
			return responseStr;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		} finally {
			try {
				if (response != null) {
					EntityUtils.consumeQuietly(response.getEntity());
					response.getEntity().getContent().close();
					response.close();
				}
				httppost.releaseConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				closeableHttpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 阿布云代理
	 * 
	 * @param httppost
	 * @param isProxy
	 * @return responseStr
	 */
	public static String executeAbuyunClient(HttpPost http, boolean isProxy,
			String charset) {
		CloseableHttpResponse response = null;
		CloseableHttpClient closeableHttpClient = null;
		try {
			String proxyIP = "proxy.abuyun.com";
			int proxtport = 9010;
			String proxyUser = "8964dP3fM91P";
			String proxyPass = "DA83F5077E87E63";
			BasicHeader header = new BasicHeader("Proxy-Switch-Ip", "yes");
			List<Header> defaultHeader = new ArrayList<Header>();
			defaultHeader.add(header);
			// 创建认证，并设置认证范围
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(proxyIP, 9010),// 可以访问的范围
					new UsernamePasswordCredentials(proxyUser, proxyPass));// 用户名和密码
			// 代理的设置

			HttpHost proxy = new HttpHost(proxyIP, proxtport);
			RequestConfig config = RequestConfig
					.custom()
					.setConnectTimeout(5000)
					// .setExpectContinueEnabled(true)
					.setCookieSpec(CookieSpecs.IGNORE_COOKIES)
					.setSocketTimeout(5000).setConnectionRequestTimeout(5000)
					.setProxy(proxy).build();
			closeableHttpClient = HttpClients.custom()
					.setDefaultCredentialsProvider(credsProvider)
					.setDefaultHeaders(defaultHeader).build();

			http.setConfig(config);
			int runCount = 0;
			String responseStr = "";
			while (true) {
				try {
					if (runCount > 20) {
						log.loger
								.error("-------abuyun >>>>>exe 20 count not succ ....return ");
						break;
					}
					response = closeableHttpClient.execute(http);
				} catch (Exception ex) {
					ex.printStackTrace();
					continue;
				}
				if (response == null) {
					runCount++;
					log.loger.error(" do abuyun cont =  " + runCount);
					continue;
				}
				if (responseStr.contains("Too Many")) {
					runCount++;
					Thread.sleep(500);
					continue;
				}
				HttpEntity entity = response.getEntity();
				responseStr = EntityUtils.toString(entity, charset);
				if (!"".equals(responseStr)) {
					break;
				} else {
					log.loger.error("-------exetue run count = " + runCount);
					runCount++;
					//Thread.sleep(1000);
				}
			}
			return responseStr;
		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			try {
				if (response != null) {
					EntityUtils.consumeQuietly(response.getEntity());
					response.getEntity().getContent().close();
					response.close();
				}
				// http.releaseConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				closeableHttpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	/**
	 * 阿布云代理
	 * 
	 * @param httppost
	 * @param isProxy
	 * @return responseStr
	 */
	public static String executeAbuyunClient(HttpGet http, boolean isProxy,
			String charset) {
		CloseableHttpResponse response = null;
		CloseableHttpClient closeableHttpClient = null;
		try {
			String proxyIP = "proxy.abuyun.com";
			int proxtport = 9010;
			String proxyUser = "HA894das4P3M91P";
			String proxyPass = "DA83F50gaddas7E63";
			BasicHeader header = new BasicHeader("Proxy-Switch-Ip", "yes");
			List<Header> defaultHeader = new ArrayList<Header>();
			defaultHeader.add(header);
			// 创建认证，并设置认证范围
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(proxyIP, 9010),// 可以访问的范围
					new UsernamePasswordCredentials(proxyUser, proxyPass));// 用户名和密码
			// 代理的设置

			HttpHost proxy = new HttpHost(proxyIP, proxtport);
			RequestConfig config = RequestConfig
					.custom()
					.setConnectTimeout(5000)
					// .setExpectContinueEnabled(true)
					.setCookieSpec(CookieSpecs.IGNORE_COOKIES)
					.setSocketTimeout(5000).setConnectionRequestTimeout(5000)
					.setProxy(proxy).build();
			closeableHttpClient = HttpClients.custom()
					.setDefaultCredentialsProvider(credsProvider)
					.setDefaultHeaders(defaultHeader).build();
			http.setConfig(config);
			int runCount = 0;
			String responseStr = "";
			while (true) {
				try {
					if (runCount > 20) {
						log.loger
								.error("-------abuyun>>>>>exe 20 count not succ ....return ");
						break;
					}
					response = closeableHttpClient.execute(http);
				} catch (Exception ex) {
					ex.printStackTrace();
					continue;
				}
				if (response == null) {
					runCount++;
					continue;
				}
				if (responseStr.contains("Too Many")) {
					runCount++;
					Thread.sleep(500);
					continue;
				}
				HttpEntity entity = response.getEntity();
				responseStr = EntityUtils.toString(entity, charset);
				if (!"".equals(responseStr)) {
					break;
				} else {
					runCount++;
					//Thread.sleep(1000);
				}

			}
			return responseStr;
		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			try {
				if (response != null) {
					EntityUtils.consumeQuietly(response.getEntity());
					response.getEntity().getContent().close();
					response.close();
				}
				// http.releaseConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				closeableHttpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	/**
	 * 使用代理
	 * 
	 * @param httppost
	 * @param isProxy
	 * @return responseStr
	 */
	public static String executeClient(HttpGet http, boolean isProxy,
			String charset, Jedis jedis) {
		CloseableHttpResponse response = null;
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = null;


		try {
			closeableHttpClient = httpClientBuilder.build();
			// HttpHost proxy = new HttpHost("123.56.242.140", 8123,"http");
			RequestConfig config = null;
			int runCount = 0;
			String responseStr = "";
			while (true) {
				try {
					if (runCount > 20) {
						break;
					}
					if (isProxy) {
						config = getHttpConfigProxy(jedis);
					} else {
						config = getHttpConfigNoProxy();
					}
					http.setConfig(config);
					response = closeableHttpClient.execute(http);
					if (response == null) {
						runCount++;
						continue;
					}
					HttpEntity entity = response.getEntity();
					responseStr = EntityUtils.toString(entity, charset);
					if (!"".equals(responseStr)) {
						break;
					} else {
						runCount++;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					return "";
				}
			}
			return responseStr;

		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			try {
				if (response != null) {
					EntityUtils.consumeQuietly(response.getEntity());
					response.getEntity().getContent().close();
					response.close();
				}
				http.releaseConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				closeableHttpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	/**
	 * 使用代理
	 * 
	 * @param httppost
	 * @param isProxy
	 * @return responseStr
	 */
	public static String executeClient(HttpPost http, boolean isProxy,
			String charset,Jedis jedis) {
		CloseableHttpResponse response = null;
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = null;
		try {
			closeableHttpClient = httpClientBuilder.build();
			RequestConfig config = null;
			int runCount = 0;
			String responseStr = "";
			while (true) {
				try {
					if (runCount > 2) {
						log.loger
								.error("common proxy >>>>>exe 20 count not succ ....return ");
						break;
					}
					if (isProxy) {
						config = getHttpConfigProxy(jedis);
					} else {
						config = getHttpConfigNoProxy();
					}
					http.setConfig(config);
					response = closeableHttpClient.execute(http);
					if (response == null) {
						runCount++;
						continue;
					}
					HttpEntity entity = response.getEntity();
					responseStr = EntityUtils.toString(entity, charset);
					if (!"".equals(responseStr.trim())) {
						break;
					} else {
						runCount++;
						log.loger.error("------->>>>>  count  " + runCount);
						//Thread.sleep(1000);
					}
				} catch (Exception ex) {					
					ex.printStackTrace();
				}
			}
			return responseStr;
		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			try {
				if (response != null) {
					EntityUtils.consumeQuietly(response.getEntity());
					response.getEntity().getContent().close();
					response.close();
				}
				http.releaseConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				closeableHttpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	/**
	 * 阿布云代理 webClient模擬瀏覽器方式執行
	 * 
	 * @param httppost
	 * @param isProxy
	 * @return responseStr
	 */
	public static String executeAbuyunWebClient(String url, String cookie) {
		WebClient webClient = null;
		try {
			String proxyIP = "proxy.abuyun.com";
			int proxtport = 9010;
			String proxyUser = "HA894dasd4P3M91P";
			String proxyPass = "Dfas5077das87E63";
			// 创建认证，并设置认证范围
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(proxyIP, proxtport),// 可以访问的范围
					new UsernamePasswordCredentials(proxyUser, proxyPass));// 用户名和密码
			// 代理的设置
			webClient = new WebClient(BrowserVersion.BEST_SUPPORTED,
					"proxy.abuyun.com", 9010);
			// webClient = new WebClient(BrowserVersion.BEST_SUPPORTED );
			webClient.setCredentialsProvider(credsProvider);
			webClient.addRequestHeader("Proxy-Switch-Ip", "yes");
			// 设置webClient的相关参数
			webClient.getCookieManager().setCookiesEnabled(true);
			webClient.getOptions().setJavaScriptEnabled(true);

			Utils.sethtmlCookies(webClient, "mail.qq.com", cookie);
			webClient.getOptions().setCssEnabled(false);
			webClient
					.setAjaxController(new NicelyResynchronizingAjaxController());
			// webClient.getOptions( ).setTimeout(5000);//
			webClient.getOptions().setThrowExceptionOnScriptError(false);

			int runCount = 0;
			String responseStr = "";
			while (true) {
				HtmlPage rootPage = null;
				try {
					if (runCount > 20) {
						break;
					}
					// 模拟浏览器打开一个目标网址
					rootPage = webClient.getPage(url);

					Thread.sleep(500);// 主要是这个线程的等待 因为js加载也是需要时间的
					if (rootPage == null) {
						continue;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					if (rootPage != null) {
						rootPage.cleanUp();
					}
				}
				responseStr = rootPage.asXml();
				if (!"".equals(responseStr)) {
					break;
				} else {
					runCount++;
					Thread.sleep(1000);
				}
			}
			return responseStr;
		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			try {
				webClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	/**
	 * 阿布云代理 webClient模擬瀏覽器方式執行
	 * 
	 * @param httppost
	 * @param isProxy
	 * @return responseStr
	 */
	public static String executeAntWebClient(String url, String cookie) {
		WebClient webClient = null;
		try {
			// String[] host= getHttpProxy();
			String proxyIP = "127.0.0.1";
			int proxtport = 8123;
			// 代理的设置
			webClient = new WebClient(BrowserVersion.BEST_SUPPORTED, proxyIP,
					proxtport);
			// webClient = new WebClient(BrowserVersion.BEST_SUPPORTED );
			webClient.addRequestHeader("Proxy-Authorization",
					getProxyAuthHeader());
			// 设置webClient的相关参数
			webClient.getCookieManager().setCookiesEnabled(true);
			webClient.getOptions().setJavaScriptEnabled(true);

			Utils.sethtmlCookies(webClient, "mail.qq.com", cookie);
			webClient.getOptions().setCssEnabled(false);
			webClient
					.setAjaxController(new NicelyResynchronizingAjaxController());
			// webClient.getOptions( ).setTimeout(5000);//
			webClient.getOptions().setThrowExceptionOnScriptError(false);

			int runCount = 0;
			String responseStr = "";
			while (true) {
				HtmlPage rootPage = null;
				try {
					if (runCount > 20) {
						break;
					}
					// 模拟浏览器打开一个目标网址
					rootPage = webClient.getPage(url);

					Thread.sleep(500);// 主要是这个线程的等待 因为js加载也是需要时间的
					if (rootPage == null) {
						continue;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					if (rootPage != null) {
						rootPage.cleanUp();
					}
				}
				responseStr = rootPage.asXml();
				if (!"".equals(responseStr)) {
					break;
				} else {
					runCount++;
					Thread.sleep(1000);
				}
			}
			return responseStr;
		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			try {
				webClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	/**
	 * 阿布云代理 webClient模擬瀏覽器方式執行
	 * 
	 * @param httppost
	 * @param isProxy
	 * @return responseStr
	 */
	public static String executeWebClient(String url, String cookie) {
		WebClient webClient = null;
		try {
			LogFactory.getFactory().setAttribute(
					"org.apache.commons.logging.Log",
					"org.apache.commons.logging.impl.NoOpLog");
			int runCount = 0;
			String responseStr = "";
			while (true) {
				String[] host = getHttpProxy();
				String proxyIP = host[0];
				int proxtport = Integer.parseInt(host[1]);
				// 代理的设置
				webClient = new WebClient(BrowserVersion.BEST_SUPPORTED,
						proxyIP, proxtport);
				// webClient = new WebClient(BrowserVersion.BEST_SUPPORTED );
				// webClient.addRequestHeader("Proxy-Authorization",
				// getProxyAuthHeader());
				// 设置webClient的相关参数
				webClient.getCookieManager().setCookiesEnabled(true);
				webClient.getOptions().setJavaScriptEnabled(true);

				Utils.sethtmlCookies(webClient, "mail.qq.com", cookie);
				webClient.getOptions().setCssEnabled(false);
				webClient
						.setAjaxController(new NicelyResynchronizingAjaxController());
				// webClient.getOptions( ).setTimeout(5000);//
				webClient.getOptions().setThrowExceptionOnScriptError(false);
				HtmlPage rootPage = null;
				try {
					if (runCount > 20) {
						break;
					}
					// 模拟浏览器打开一个目标网址
					rootPage = webClient.getPage(url);
					Thread.sleep(10000);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				if (rootPage == null) {
					continue;
				}
				responseStr = rootPage.asXml();
				if (!"".equals(responseStr)) {
					break;
				} else {
					runCount++;
					Thread.sleep(1000);
				}
			}
			return responseStr;
		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			try {
				webClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	public static String[] getHttpProxy() {
		Jedis jedis = RedisServer.getJedis(null);
		//Map<String, String> proxyMap = jedis.hgetAll("proxy");
		String hostPort = getProxyRandom();
		String[] key = hostPort.trim().split(":");
		RedisServer.redisServer.closeJedis(jedis);
		return key;
	}

	public static String getCookie(String lineTxt, Jedis jedis) {
		String clientuin = lineTxt.split("#_#")[0];
		String clientkey = lineTxt.split("#_#")[1];
		if ("".equals(clientuin) || "".equals(clientkey)) {
			return null;
		}
		String url = "http://ptlogin2.qq.com/jump?clientuin="
				+ clientuin
				+ "&keyindex=9&pt_aid=549000912"
				+ "&daid=5&u1=http%3A%2F%2Fqzs.qq.com%2Fqzone%2Fv5%2Floginsucc.html%3Fpara%3Dizone"
				+ "&clientkey=" + clientkey + "&pt_3rd_aid=0&ptopt=1&style=40";
		// String url =
		// "http://ptlogin2.qq.com/jump?clientuin="+clientuin+"&clientkey="+clientkey+"&keyindex=9&pt_aid=549000912&daid=5&pt_qzone_sig=1&u1=http%3A%2F%2Fqzs.qq.com%2Fqzone%2Fv5%2Floginsucc.html%3Fpara%3Dizone";
		HttpGet http = new HttpGet(url);
		log.loger.info(url);
		http.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		http.setHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36");
		/** 读取服务器返回过来的json字符串数据 **/
		String strResult = HttpThreadPool.executeAbuyunClient(http, true,
				"UTF-8");
		// String strResult = HttpThreadPool.executeClient(http, false,
		// "UTF-8",jedis);
		// log.loger.info(strResult);
		if (!strResult.startsWith("ptui_qlogin_CB")) {
			if (strResult.length() < 200) {
				log.loger.info("--1--" + strResult);
			}
			return null;
		}
		int begin = strResult.indexOf("http://ptlogin4.qzone.qq.com");
		if (begin < 0) {
			log.loger.info("--2--" + strResult);
			return null;
		}
		int mend = strResult.lastIndexOf("',");
		String loginUrl = strResult.substring(begin, mend);
		http = new HttpGet(loginUrl);
		http.setHeader("Upgrade-Insecure-Requests", "1");
		http.setHeader("Host", "ptlogin4.qzone.qq.com");

		CloseableHttpResponse response = null;
		CookieStore cookieStore = new BasicCookieStore();
		CloseableHttpClient closeableHttpClient = null;
		try {
			String proxyIP = "proxy.abuyun.com";
			int proxtport = 9010;
			String proxyUser = "HA8943dasd4P3M91P";
			String proxyPass = "DA83dasdas6E87E63";
			BasicHeader header = new BasicHeader("Proxy-Switch-Ip", "yes");
			List<Header> defaultHeader = new ArrayList<Header>();
			defaultHeader.add(header);
			// 创建认证，并设置认证范围
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(proxyIP, 9010),// 可以访问的范围
					new UsernamePasswordCredentials(proxyUser, proxyPass));// 用户名和密码
			// 代理的设置
			HttpHost proxy = new HttpHost(proxyIP, proxtport);
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(5000).setSocketTimeout(5000)
					.setConnectionRequestTimeout(5000).setProxy(proxy).build();
			closeableHttpClient = HttpClients.custom()
					.setDefaultCredentialsProvider(credsProvider)
					.setDefaultHeaders(defaultHeader)
					.setDefaultCookieStore(cookieStore).build();
			http.setConfig(config);
			int runCount = 0;
			String responseStr = "";
			while (true) {
				try {
					if (runCount > 20) {
						log.loger
								.error("------->>>>>exe 20 count not succ ....return ");
						break;
					}
					response = closeableHttpClient.execute(http);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				if (response == null) {
					runCount++;
					continue;
				}
				return getCookieStr();
			}

		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			try {
				if (response != null) {
					EntityUtils.consumeQuietly(response.getEntity());
					response.getEntity().getContent().close();
					response.close();
				}
				// http.releaseConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				closeableHttpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}
	private static  String  getAccessTocken(String superTocken){
		long e=0;
		long key=4294967296l;
		for(int i=0;i<superTocken.length();i++){
			   long  b   = superTocken.charAt(i);
			e=(33*e+b)%key;
		} 
		if(e>0){
			return Long.toString(e);
		}else{
			return null;
		}
		
	}
	/**
	 * supertoken=4207453336
	 * @return
	 */
	private static String getSupertoken() {
		List<Cookie> cookielist = cookies.getCookies();
		String supertoken = "";
		
		try {
			for (int i = 0; i < cookielist.size(); i++) {
				// - [version: 0][name: p_skey][value:
				// NF58j6N-OSNb*5B2YF9wGI2LuwBgo0ACIhGBrYzq3rs_][domain:
				// qzone.qq.com][path: /][expiry: null]				
				String cookie = cookielist.get(i).toString();
				log.loger.info(cookie);
				// String cookie =
				// "[version: 0][name: p_skey][value: NF58j6N-OSNb*5B2YF9wGI2LuwBgo0ACIhGBrYzq3rs_][domain: qzone.qq.com][path: /][expiry: null]";
				String key ="supertoken][value:";
				int start = cookie.indexOf(key);
				if (start > 0) {
					log.loger.info("supertoken = " + cookie);
					String sub = cookie.substring(start);
					int end = sub.indexOf("][domain");
					supertoken = sub.substring(key.length(), end).trim();
					return supertoken;
				} 
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return supertoken;
	}
	private static String getCookieStr() {
		List<Cookie> cookielist = cookies.getCookies();
		String pskey = "";
		String skey = "";
		String uin = "";
		try {
			for (int i = 0; i < cookielist.size(); i++) {
				 
				String cookie = cookielist.get(i).toString();
				log.loger.info(cookie);
				int start = cookie.indexOf("p_skey][value:");
				if (start > 0) {
					if(cookie.contains("qzone")){
						log.loger.info("p_skey = " + cookie);
						String sub = cookie.substring(start);
						int end = sub.indexOf("][domain");
						pskey = sub.substring(15, end).trim();
					}
				} 
				start = cookie.indexOf("name: skey][value:");
				if (start > 0) {
					log.loger.info("skey = " + cookie);
					String sub = cookie.substring(start);
					int end = sub.indexOf("][domain");
					skey = sub.substring(18, end).trim();
				}
				start = cookie.indexOf("name: uin][value:");
				if (start > 0) {
					log.loger.info("uin = " + cookie);
					String sub = cookie.substring(start);
					int end = sub.indexOf("][domain");
					uin = sub.substring(17, end).trim();
				}
			}
			if (!"".equals(pskey) && !"".equals(skey) && !"".equals(uin)) {
				return uin + "#_#" + skey + "#_#" + pskey;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return "";
	}
	/**
	 * 使用代理 count 代理使用次数 vip 这个号使用的代理的次数
	 * 
	 * @param httppost
	 * @param isProxy
	 * @return responseStr
	 */
	/*public static String executeClientOne(HttpPost http, boolean isProxy,
			String charset, int count, String vip, String goodKeyCode,
			Jedis jedis) {
		CloseableHttpResponse response = null;
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = null;
		try {
			closeableHttpClient = httpClientBuilder.build();
			RequestConfig config = null;
			int runCount = 0;
			String responseStr = "";
			String host = "";
			while (true) {
				try {
					if (runCount > 20) {
						log.loger
								.error(">>>>>exe 20 count not succ ....return ");
						break;
					}
					host = getProxyHost(count, vip, jedis);
					if (host == null) {
						log.loger
								.info("not find 88888 proxy host ,sleep 5s...");
						//Thread.sleep(5000);
						continue;
					}
					config = getHttpConfig(host);
					http.setConfig(config);
					response = closeableHttpClient.execute(http);
					if (response == null) {
						runCount++;
						continue;
					}
					HttpEntity entity = response.getEntity();
					responseStr = EntityUtils.toString(entity, charset);
					if (!"".equals(responseStr.trim())) {
						break;
					} else {
						runCount++;
					}
				} catch (Exception ex) {
					log.loger.error("excep:" + ex.getMessage());
					if (ex.getMessage().contains("out")
							|| ex.getMessage().contains("refused")) {
						delProxyHost(host, jedis);
					}
				}
			}
			if (responseStr != null && responseStr.contains(goodKeyCode)) {
				incrHost(vip, host);
			}
			return responseStr;
		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			try {
				if (response != null) {
					EntityUtils.consumeQuietly(response.getEntity());
					response.getEntity().getContent().close();
					response.close();
				}
				http.releaseConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				closeableHttpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}*/

	/**
	 * 使用代理 count 代理使用次数 vip 这个号使用的代理的次数
	 * 
	 * @param httppost
	 * @param isProxy
	 * @return responseStr
	 */
	/*public static String executeClientOne(HttpGet http, boolean isProxy,
			String charset, int count, String vip, String goodKeyCode,
			Jedis jedis) {
		CloseableHttpResponse response = null;
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = null;
		try {
			closeableHttpClient = httpClientBuilder.build();
			RequestConfig config = null;
			int runCount = 0;
			String responseStr = "";
			String host = "";
			while (true) {
				try {
					if (runCount > 20) {
						log.loger
								.error(">>>>>exe 20 count not succ ....return ");
						break;
					}
					host = getProxyHost(count, vip, jedis);
					if (host == null) {
						log.loger
								.info("not find 88888 proxy host ,sleep 5s...");
						Thread.sleep(5000);
						continue;
					}
					config = getHttpConfig(host);
					http.setConfig(config);
					response = closeableHttpClient.execute(http);
					if (response == null) {
						runCount++;
						continue;
					}
					HttpEntity entity = response.getEntity();
					responseStr = EntityUtils.toString(entity, charset);
					if (!"".equals(responseStr.trim())) {
						break;
					} else {
						runCount++;
					}
				} catch (Exception ex) {
					log.loger.error("excep:" + ex.getMessage());
					if (ex.getMessage().contains("out")
							|| ex.getMessage().contains("refused")) {
						delProxyHost(host, jedis);
					}

				}
			}
			if (responseStr != null && responseStr.contains(goodKeyCode)) {
				incrHost(vip, host);
			}
			return responseStr;
		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			try {
				if (response != null) {
					EntityUtils.consumeQuietly(response.getEntity());
					response.getEntity().getContent().close();
					response.close();
				}
				http.releaseConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				closeableHttpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}*/

	/**
	 * 获取爬虫代理的ip，根据使用次数进行获取。 【爬虫】代理 2015888888
	 * 
	 * @param count
	 * @param vip
	 * @return
	 */
	/*private static String getProxyHost(int count, String vip, Jedis jedis) {
		try {
			Map<String, String> proxyMap = jedis
					.hgetAll(StaticInfo.proxy_8888_all);
			Iterator<String> iter = proxyMap.keySet().iterator();
			while (iter.hasNext()) {
				String host = iter.next().trim();
				String usedCount = jedis.hget(StaticInfo.proxy_8888_used, vip
						+ "." + host);
				// log.loger.info(vip+"."+host+ " used count = "+ usedCount);
				if (usedCount == null || Integer.valueOf(usedCount) < count) {
					return host;
				} else {
					continue;
				}
			}
			// 没有找到 获取最新的ip，加载代理到最新 队列里面
			CheckProxyThrable checkProxy = new CheckProxyThrable("", 7);
			checkProxy.initProxyServersFrom2015888888("", jedis);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}*/

	/**
	 * 获取爬虫代理的ip，根据使用次数进行获取。 【爬虫】代理 2015888888
	 * 
	 * @param count
	 * @param vip
	 * @return
	 */
	/*private static String delProxyHost(String host, Jedis jedis) {

		try {
			jedis.hdel(StaticInfo.proxy_8888_all, host);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}*/

	/**
	 * 获取爬虫代理的ip，根据使用次数进行获取。 【爬虫】代理 2015888888
	 * 
	 * @param count
	 * @param vip
	 * @return
	 */
	/*private static String incrHost(String vip, String host) {
		Jedis jedis = RedisServer.getJedis(null);
		try {
			long usedCount = jedis.hincrBy(StaticInfo.proxy_8888_used, vip
					+ "." + host, 1);
			log.loger.info(vip + "." + host + " used count = " + usedCount);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			RedisServer.redisServer.closeJedis(jedis);
		}
		return null;
	}*/

	private static RequestConfig getHttpConfig() {

		RequestConfig config = RequestConfig.custom().setConnectTimeout(6000)
				.setExpectContinueEnabled(true).setSocketTimeout(6000)
				.setConnectionRequestTimeout(6000).build();
		return config;
	}

	public static RequestConfig getHttpConfigNoProxy() {
		RequestConfig config = RequestConfig.custom().setConnectTimeout(5000)
				.setExpectContinueEnabled(true)
				.setCookieSpec(CookieSpecs.IGNORE_COOKIES)
				.setSocketTimeout(5000).setConnectionRequestTimeout(5000)
				.setCircularRedirectsAllowed(true)
				.build();
		return config;
	}

	public static RequestConfig getHttpConfigProxy(Jedis jedis) {
		// Jedis jedis = RedisServer.getJedis(null);
		//Map<String, String> proxyMap = jedis.hgetAll("proxy");
		/*if(proxyMap==null){
			if(jedis!=null){
				proxyMap = jedis.hgetAll("proxy");
			}
		}*/
		String hostPort = getProxyRandom();
		
		String[] key = hostPort.split(":");
		log.loger.info("--->proxy,host =" + hostPort.trim());
		String proxyip = key[0];
		int proxyport = Integer.parseInt(key[1].trim());
		HttpHost proxy = new HttpHost(proxyip, proxyport);
		RequestConfig config = RequestConfig.custom().setConnectTimeout(5000)
				.setExpectContinueEnabled(true)
				// .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
				.setSocketTimeout(5000).setConnectionRequestTimeout(5000)
				.setProxy(proxy)
				.setCircularRedirectsAllowed(true)
				.build();
		// RedisServer.redisServer.closeJedis(jedis);
		return config;
	}

	public static RequestConfig getHttpConfigProxy(String hostPort) {

		String[] key = hostPort.split(":");
		log.loger.info("<<<<<--->proxy,host =" + hostPort.trim());
		String proxyip = key[0];
		int proxyport = Integer.parseInt(key[1].trim());
		HttpHost proxy = new HttpHost(proxyip, proxyport);
		RequestConfig config = RequestConfig.custom().setConnectTimeout(5000)
				.setExpectContinueEnabled(true)
				.setCookieSpec(CookieSpecs.IGNORE_COOKIES)
				.setSocketTimeout(5000).setConnectionRequestTimeout(5000)
				.setProxy(proxy).build();
		return config;
	}

	public static RequestConfig getHttpConfig(String hostPort) {
		String[] key = hostPort.split(":");
		String proxyip = key[0];
		int proxyport = Integer.parseInt(key[1].trim());
		HttpHost proxy = new HttpHost(proxyip, proxyport);
		RequestConfig config = RequestConfig.custom().setConnectTimeout(5000)
				.setExpectContinueEnabled(true)
				.setCookieSpec(CookieSpecs.IGNORE_COOKIES)
				.setSocketTimeout(5000).setConnectionRequestTimeout(5000)
				.setProxy(proxy).build();
		return config;
	}

	/**
	 * 使用代理
	 * 
	 * @param httppost
	 * @param isProxy
	 * @return responseStr
	 */
	public static String executeClient(HttpGet http, String host) {
		CloseableHttpResponse response = null;
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = null;
		try {
			closeableHttpClient = httpClientBuilder.build();
			// HttpHost proxy = new HttpHost("123.56.242.140", 8123,"http");
			RequestConfig config = null;
			int runCount = 0;
			String responseStr = "";
			while (true) {
				try {
					config = getHttpConfigProxy(host);
					http.setConfig(config);
					response = closeableHttpClient.execute(http);
					if (response == null) {
						runCount++;
						continue;
					}
					HttpEntity entity = response.getEntity();
					responseStr = EntityUtils.toString(entity, "utf-8");

					return responseStr;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			try {
				if (response != null) {
					EntityUtils.consumeQuietly(response.getEntity());
					response.getEntity().getContent().close();
					response.close();
				}
				http.releaseConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				closeableHttpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	public static String getCookieUrl(String lineTxt, Jedis jedis) {

		cookies.clear();
		// log.loger.info(lineTxt);
		if (!lineTxt.startsWith("http://ptlogin2.qq.com")) {
			return null;
		}
		int start = lineTxt.indexOf("aid=7000201");
		if (start > 0) {
			return null;
		}
		if (lineTxt.contains("mail.qq")) {
			return getCookieMail(lineTxt, jedis);
		} if (lineTxt.contains("ptlogin2.qq.com/cardtoqzone?ptlang=2052&")) {
			return getCookieCardQzone(lineTxt);
		}else {
			return getCookieCoomon(lineTxt, jedis);
		}
		/*
		 * else if( lineTxt.indexOf("&keyindex=9")>0){ return
		 * getCookieJson(lineTxt, jedis); }
		 */
	}
	/**
	 * 这种数据是第一次请求获取cookie，第二次请求获取qzone的cookie
	 * @param lineTxt
	 * @param jedis
	 * @return
	 */
	private static String getCookieCardQzone(String lineTxt) {
		String url = lineTxt;
		
		HttpGet httpOne = new HttpGet(url);
		// log.loger.info(url);
		httpOne.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		httpOne.setHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36");
		/** 读取服务器返回过来的json字符串数据 **/
		String loginUrl = "http://user.qzone.qq.com";
		HttpGet httpTwo = new HttpGet(loginUrl);
		httpTwo.setHeader("Upgrade-Insecure-Requests", "1");
		httpTwo.setHeader("Host", "user.qzone.qq.com");
		httpTwo.setHeader("Referer", "http://qzone.qq.com/");
		CloseableHttpResponse response = null;
		CloseableHttpClient closeableHttpClient=null;
		RequestConfig config=getAbuyunConfig();
		try {
			closeableHttpClient = getAbuyunClient();
			httpOne.setConfig(config);
			response = closeableHttpClient.execute(httpOne);
			httpTwo.setConfig(config);
			response = closeableHttpClient.execute(httpTwo);
			return getCookieStr();
		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			try {
				if (response != null) {
					EntityUtils.consumeQuietly(response.getEntity());
					response.getEntity().getContent().close();
					response.close();
				}
				// http.releaseConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				closeableHttpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	/**
	 * 这种数据是第一次请求获取cookie，第二次请求获取qzone的cookie
	 * @param lineTxt
	 * @param jedis
	 * @return
	 */
	private static String getCookieNew(String lineTxt) {
		
		CloseableHttpResponse response = null;
		CloseableHttpClient closeableHttpClient=null;
		RequestConfig config=getAbuyunConfig();
		closeableHttpClient = getAbuyunClient();
		String url = lineTxt;
		
		/** 读取服务器返回过来的json字符串数据 **/
		
		String myCookie="";
		try {
			HttpGet httpOne = new HttpGet(url);
			httpOne.setHeader("Host", "ptlogin2.qq.com");
			httpOne.setHeader("Accept-Encoding", "gzip, deflate, sdch");
			httpOne.setHeader(
					"User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36");
			httpOne.setConfig(config);
			while(true){
				try{
					response = closeableHttpClient.execute(httpOne);
					HttpEntity entity = response.getEntity();
					String httpStr = EntityUtils.toString(entity); 
					if(httpStr.contains("Many Requests")|| "".equals(httpStr)){
						//Thread.sleep(500);
						log.loger.info(".....");
						continue;
					}else{
						break;
					}	
				}catch(Exception ex){
					ex.printStackTrace();
					Thread.sleep(500);
				}		
			}
			
			myCookie=getCookieStr();
			if(!"".equals(myCookie)){
				return myCookie;
			}
			
			String superkey=getSupertoken();
			if("".equals(superkey)){
				return ""; 
			}
			
			String accessTocken= getAccessTocken(superkey);
			if(accessTocken==null){
				return "";
			}
			String superCookie=getSuperCookie();
			//superCookie="tvfe_boss_uuid=1ae14928c9b325bd; mobileUV=1_158235c796d_4cb1b; ptui_loginuin=150112342; eas_sid=V1h457i8h6o0J7e3a648S718V2; pgv_pvi=9276167168; RK=8PvTq5amHS; o_cookie=3543662630; pac_uid=1_3543662630; pt_guid_sig=90567489605f2c755d4cb4080a4ac82a93a59d44f408fe9285f065300782063f; pgv_pvid=1532397484; ETK=; superuin=o0342157884; superkey=iSjOgmbANS3Ew0G5IF7bqF5aEkFi8YpBPGVkMlFl1z0_; supertoken=2097833468; pt_recent_uins=9183f60d725640c2d8b58aff5ced3cf5b06f3c171dc6e65a65a3b65b7eeb7e785223767fdd1030cde061d365e9f235064ef61876b583a986; ptisp=ctc; ptnick_342157884=e78e8be88085e5bd92e69da5; u_342157884=@0jz6JIhdK:1480589215:1480589215:e78e8be88085e5bd92e69da5:1; ptcz=4d73e6b1f849bcafc5922e527cdf1366b864ef47ab9dc975e46d95d6c093ac75; pt2gguin=o0342157884; uin=o0342157884; skey=@0jz6JIhdK; pt_local_token=0.10341153797831648; pgv_info=ssid=s5951617380; qrsig=lF8-qv*lSHyJ2gytLtf0AtD8uN7pNwzorkGzY9kVe0*UMntmUMLfQus3OBdv4WgC; dlock=5_1480589678_1_ ";
			String loginUrl = "http://ptlogin2.qq.com/pt4_auth?daid=5&appid=549000912&auth_token="+accessTocken;
			//String loginUrl = "http://ptlogin2.qq.com/pt4_auth?daid=5&appid=549000912&auth_token=1277795538";
			log.loger.info(loginUrl);
			HttpGet httpTwo = new HttpGet(loginUrl); 
			httpTwo.setHeader("Upgrade-Insecure-Requests", "1");
			httpTwo.setHeader("Host", "ptlogin2.qq.com");
			httpTwo.setHeader("Accept-Encoding", "gzip, deflate, sdch");
			httpTwo.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			httpTwo.setHeader("Cookie",superCookie);
			log.loger.info(superCookie);
			httpTwo.setHeader(
					"User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36");
			httpTwo.setConfig(config);
			String httpStr="";
			while(true){
				try{
					response = closeableHttpClient.execute(httpTwo);
					HttpEntity entity = response.getEntity();
					httpStr = EntityUtils.toString(entity);
					log.loger.info("reponse =" + httpStr);
					if(httpStr.contains("Many Requests")|| "".equals(httpStr)){
						Thread.sleep(500);
						continue;
					}else{
						break;
					}	
				}catch(Exception ex){
					ex.printStackTrace();
				}		
			}
			if (!httpStr.startsWith("ptui_auth_CB")) {
				return null;
			}
			int begin = httpStr.indexOf("http://ptlogin4.qzone.qq.com");
			if (begin < 0) {
				return null;
			}
			int mend = httpStr.lastIndexOf("')");
			loginUrl = httpStr.substring(begin, mend);
			log.loger.info(loginUrl);
			//HttpGet httpThree = new HttpGet(loginUrl);
			URI r= new URI(loginUrl);
			httpTwo.setURI(r);
			httpTwo.setHeader("Upgrade-Insecure-Requests", "1");
			httpTwo.setHeader("Host", "ptlogin4.qzone.qq.com");
			httpTwo.setConfig(config);
			while(true){
				try{
					response = closeableHttpClient.execute(httpTwo);
					HttpEntity entity = response.getEntity();
					httpStr = EntityUtils.toString(entity); 
					if(httpStr.contains("Many Requests")|| "".equals(httpStr)){
						Thread.sleep(500);
						continue;
					}else{
						break;
					}	
				}catch(Exception ex){
					ex.printStackTrace();
				}		
			}
			
			
			loginUrl ="http://qzone.qq.com/";
			log.loger.info(loginUrl);
			//HttpGet httpThree = new HttpGet(loginUrl);
			r= new URI(loginUrl);
			httpTwo.setURI(r);
			httpTwo.setHeader("Upgrade-Insecure-Requests", "1");
			httpTwo.setConfig(config);
			while(true){
				try{
					response = closeableHttpClient.execute(httpTwo);
					HttpEntity entity = response.getEntity();
					httpStr = EntityUtils.toString(entity); 
					if(httpStr.contains("Many Requests")|| "".equals(httpStr)){
						Thread.sleep(500);
						continue;
					}else{
						break;
					}	
				}catch(Exception ex){
					ex.printStackTrace();
				}		
			}
			
			return getCookieStr();
		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			try {
				if (response != null) {
					EntityUtils.consumeQuietly(response.getEntity());
					response.getEntity().getContent().close();
					response.close();
				}
				// http.releaseConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				closeableHttpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	/**
	 * [etl]2016-12-01 17:58:23,873 INFO-[version: 0][name: superkey][value: fYKXDQgiQzaresgt2fkzTqyPLhNzxGsFD-p596016wA_][domain: ptlogin2.qq.com][path: /][expiry: null]
	   [etl]2016-12-01 17:58:23,873 INFO-[version: 0][name: supertoken][value: 513758954][domain: ptlogin2.qq.com][path: /][expiry: null]
	   [etl]2016-12-01 17:58:23,873 INFO-[version: 0][name: superuin][value: o0108570140][domain: ptlogin2.qq.com][path: /][expiry: null]
	 * @return
	 */
	private static String getSuperCookie() {
		List<Cookie> cookielist = cookies.getCookies();
		String superuin = "";
		String supertoken = "";
		String superkey = "";
		try {
			for (int i = 0; i < cookielist.size(); i++) {
				 
				String cookie = cookielist.get(i).toString();
				String key="superuin][value:";
				int start = cookie.indexOf(key);
				if (start > 0) {
					log.loger.info("superuin = " + cookie);
					String sub = cookie.substring(start);
					int end = sub.indexOf("][domain");
					superuin = sub.substring(key.length(), end).trim();
				}  
				key="superkey][value:";
				start = cookie.indexOf(key);
				if (start > 0) {
					log.loger.info("superkey = " + cookie);
					String sub = cookie.substring(start);
					int end = sub.indexOf("][domain");
					superkey = sub.substring(key.length(), end).trim();
				} 
				key="supertoken][value:";
				start = cookie.indexOf(key);
				if (start > 0) {
					log.loger.info("uin = " + cookie);
					String sub = cookie.substring(start);
					int end = sub.indexOf("][domain");
					supertoken = sub.substring(key.length(), end).trim();
				}
			}
			if (!"".equals(supertoken) && !"".equals(superkey) && !"".equals(superuin)) {
				return "superuin="+superuin+"; superkey="+superkey+"; supertoken="+supertoken+"; ";
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "";
	}

	private static RequestConfig  getAbuyunConfig() { 
		String proxyIP = "proxy.abuyun.com";
		int proxtport = 9010;
		// 代理的设置
		HttpHost proxy = new HttpHost(proxyIP, proxtport);
		return RequestConfig.custom()
				.setConnectTimeout(5000).setSocketTimeout(4000)
				.setConnectionRequestTimeout(5000)
				.setProxy(proxy)
				.build();
	}

	private static CloseableHttpClient getAbuyunClient() {
		String proxyIP = "proxy.abuyun.com";
		int proxtport = 9010;
		String proxyUser = "HA894dsaP3M91P";
		String proxyPass = "Ddas83F5dsaE63";
		BasicHeader header = new BasicHeader("Proxy-Switch-Ip", "yes");
		List<Header> defaultHeader = new ArrayList<Header>();
		defaultHeader.add(header);
		// 创建认证，并设置认证范围
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(proxyIP, 9010),// 可以访问的范围
				new UsernamePasswordCredentials(proxyUser, proxyPass));// 用户名和密码
		CloseableHttpClient closeableHttpClient = HttpClients.custom()
				.setDefaultCredentialsProvider(credsProvider)
				.setDefaultHeaders(defaultHeader)
				.setDefaultCookieStore(cookies).build();
		return closeableHttpClient;
	}

	private static String getCookieCoomon(String lineTxt, Jedis jedis) {
		String url = lineTxt; // 参数
		// url
		// ="http://ptlogin2.qq.com/jump?clientuin=1026466937&clientkey=ACE1310664FA3A64D88288EDC2F075A31FB4BA65B39A4031182796D5F7F760DC981FF4107AE37A7F36B423F9BC5252C92DE49FF63F7C36DF&keyindex=19&pt_mq=0&u1=http%3A%2F%2Fbuluo.qq.com%2Fmobile%2Fdetail.html%3Fbid%3D10039%26pid%3D9058172-1478745394%26_bid%3D128%26_wv%3D1027%23from%3Dgrp_sub_obj%26gid%3D101320398%26_wv%3D1027";
		if (!url.contains("ptlogin2.qq.com/jump")) {
			return null;
		}

		// log.loger.info(lineTxt); 1&u1=http%3A%2F%2F
		if (url.indexOf("keyindex=") > 0) {
			Map<String, Object> resMap = new HashMap<String, Object>();
			// header
			// log.loger.info(url);
			String newUrl = getNewUrl(url);
			if (newUrl == null) {
				return null;
			}
			Map<String, Object> headers = new HashMap<String, Object>();

			String reponse = doGet(newUrl, resMap, headers, jedis);
			// List<Cookie> cookielist = cookies.getCookies();
			String cookie = getCookieStr();
			if (cookie != null && !"".equals(cookie)) {
				log.loger.info(url);
			}
			// log.loger.info(cookie);
			return cookie;
			/*
			 * if(cookielist.size()>0){ log.loger.info("newUrl = " + newUrl);
			 * //log.loger.info("oldUrl = " + lineTxt); String cookie
			 * =getCookieStr(); log.loger.info(cookie); return cookie; }
			 */
		} else {
			return null;
		}

		// keyindex=19&u1=

		// String cookie =getCookieStr();
		// if(cookie!=null&& !"".equals(cookie)){
		// log.loger.info(lineTxt);
		// log.loger.info(cookie);
		// }
		// return null;
	}

	/**
	 * =19&pt_mq=0&u1=http%3A%2F%2Fm.ke.qq.com%2Findex.html%3F_bid%3D167%
	 * 26_wv%3D5121%23from%3Dios_dongtai
	 * 
	 * */

	private static String getNewUrl(String url) {
		int start = url.indexOf("u1=http%3A%2F%2F");
		if (start < 0) {
			start = url.indexOf("u1=https%3A%2F%2F");
			if (start < 0) {
				return null;
			}
		}
		String sub = url.substring(start);
		int end = sub.indexOf("com%2F");
		String subStr = sub.substring(0, end);
		String newUrl = url.replace(subStr, "u1=http%3A%2F%2Fqzone.qq.");
		return newUrl;
	}

	public static String getCookieJson(String lineTxt, Jedis jedis) {

		String url = lineTxt;
		if (!lineTxt.contains("ptlogin2.qq.com/jump")) {
			return null;
		}
		HttpGet http = new HttpGet(url);
		// log.loger.info(url);
		http.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		http.setHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36");
		/** 读取服务器返回过来的json字符串数据 **/
		// String strResult = HttpThreadPool.executeAbuyunClient(http, true,
		// "UTF-8");
		String strResult = HttpThreadPool.executeClient(http, true, "UTF-8",
				jedis);
		// log.loger.info(strResult);
		if (!strResult.startsWith("ptui_qlogin_CB")) {
			if (strResult.length() < 200) {
				log.loger.info("--1--" + strResult);
			}
			return null;
		}
		int begin = strResult.indexOf("http://ptlogin4.qzone.qq.com");
		if (begin < 0) {
			log.loger.info("--2--" + strResult);
			return null;
		}
		int mend = strResult.lastIndexOf("',");
		String loginUrl = strResult.substring(begin, mend);
		http = new HttpGet(loginUrl);
		http.setHeader("Upgrade-Insecure-Requests", "1");
		http.setHeader("Host", "ptlogin4.qzone.qq.com");

		CloseableHttpResponse response = null;
		CookieStore cookieStore = new BasicCookieStore();
		CloseableHttpClient closeableHttpClient = null;
		try {
			String proxyIP = "proxy.abuyun.com";
			int proxtport = 9010;
			String proxyUser = "HA89436664P3M91P";
			String proxyPass = "DA83F50776E87E63";
			BasicHeader header = new BasicHeader("Proxy-Switch-Ip", "yes");
			List<Header> defaultHeader = new ArrayList<Header>();
			defaultHeader.add(header);
			// 创建认证，并设置认证范围
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(proxyIP, 9010),// 可以访问的范围
					new UsernamePasswordCredentials(proxyUser, proxyPass));// 用户名和密码
			// 代理的设置
			HttpHost proxy = new HttpHost(proxyIP, proxtport);
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(5000).setSocketTimeout(5000)
					.setConnectionRequestTimeout(5000).setProxy(proxy).build();
			closeableHttpClient = HttpClients.custom()
					.setDefaultCredentialsProvider(credsProvider)
					.setDefaultHeaders(defaultHeader)
					.setDefaultCookieStore(cookieStore).build();
			http.setConfig(config);
			int runCount = 0;
			String responseStr = "";
			while (true) {
				try {
					if (runCount > 2) {
						log.loger
								.error("------->>>>>exe 20 count not succ ....return ");
						break;
					}
					response = closeableHttpClient.execute(http);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				if (response == null) {
					runCount++;
					continue;
				}

				return getCookieStr();
			}

		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			try {
				if (response != null) {
					EntityUtils.consumeQuietly(response.getEntity());
					response.getEntity().getContent().close();
					response.close();
				}
				// http.releaseConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				closeableHttpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	public static String getCookieMail(String lineTxt, Jedis jedis) {
		String url = lineTxt; // 参数
		url = url.replace("w.mail.qq.com", "qzone.qq.com");
		Map<String, Object> resMap = new HashMap<String, Object>();
		// header
		Map<String, Object> headers = new HashMap<String, Object>();
		String reponse = doGet(url, resMap, headers, jedis);
		String cookie = getCookieStr();
		if (cookie != null) {
			// log.loger.info("mail cookei url  = " + url);
			return cookie;
		}

		if (reponse != null && reponse.contains("<script>location.replace")) {
			reponse = reponse.replace("<script>location.replace(\"", "");
			reponse = reponse.replace("\");</script>", "");
		} else {
			return null;
		}

		String urlOne = reponse;
		String jsontwo = doGet(urlOne, resMap, headers, jedis);
		String urlthree = "http://qzone.qq.com";
		String jsonthree = doGet(urlthree, resMap, headers, jedis);
		cookie = getCookieStr();
		/*
		 * if(cookie!=null){ log.loger.info("mail cookei url  = " + urlOne); }
		 */
		return cookie;
	}

	public static String getCookieBuluo(String lineTxt, Jedis jedis) {
		String url = lineTxt; // 参数
		Map<String, Object> resMap = new HashMap<String, Object>();
		// header
		Map<String, Object> headers = new HashMap<String, Object>();
		String reponse = doGet(url, resMap, headers, jedis);
		return getCookieStr();
	}

	public static String doGet(String apiUrl, Map<String, Object> params,
			Map<String, Object> headers, Jedis jedis) {
		String httpStr = null;
		URIBuilder a = null;
		HttpGet httpGet = null;

		try {
			a = new URIBuilder(apiUrl);
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				a.addParameter(entry.getKey(), entry.getValue().toString());
			}
			httpGet = new HttpGet(a.build());
			// httpGet.setHeader("Upgrade-Insecure-Requests", "1");
			// httpGet.setHeader("Host", "ptlogin4.qzone.qq.com");
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
			return null;
		}

		CloseableHttpResponse response = null;

		try {

			for (Map.Entry<String, Object> entry : headers.entrySet()) {
				NameValuePair pair = new BasicNameValuePair(entry.getKey(),
						entry.getValue().toString());
				httpGet.setHeader(pair.getName(), pair.getValue());
			}
			// httpGet.setConfig(getAbuyunRequestConfig());
			// httpGet.setConfig(getHttpConfigProxy(jedis));
			int count = 0;
			while (true) {
				try {
					response = httpclient.execute(httpGet);
					break;
				} catch (Exception ex) {
					ex.printStackTrace();
					count = count + 1;
				}
				if (count > 2) {
					break;
				}
			}
			if (response == null) {
				log.loger.info("response is null ....");
				return httpStr;
			}
			// printResponse(response);
			// printCookie();
			HttpEntity entity = response.getEntity();

			httpStr = EntityUtils.toString(entity);
		} catch (Exception e) {
			// e.printStackTrace();
		} finally {
			if (response != null) {
				try {
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return httpStr;
	}

	public static void printCookie() {
		// 读取cookie并保存文件
		List<Cookie> cookielist = cookies.getCookies();
		if (cookielist.isEmpty()) {
			System.out.println("None");
		} else {
			for (int i = 0; i < cookielist.size(); i++) {
				System.out.println("- " + cookielist.get(i).toString());
			}
		}
	}

	public static void printResponse(HttpResponse httpResponse)
			throws ParseException, IOException {
		// 获取响应消息实体
		HttpEntity entity = httpResponse.getEntity();
		// 响应状态
		// System.out.println("status:" + httpResponse.getStatusLine());
		// System.out.println("headers:");
		HeaderIterator iterator = httpResponse.headerIterator();
		while (iterator.hasNext()) {
			// System.out.println("\t" + iterator.next());
		}
		if (cookies.getCookies().size() > 0)
			System.out.println(cookies.getCookies().get(0).getName());
		Header[] headers = httpResponse.getHeaders("Set-Cookie");
		for (Header header : headers) {
			System.out
					.println(";" + header.getName() + "=" + header.getValue());
		}

		// 判断响应实体是否为空
		// if (entity != null) {
		// String responseString = EntityUtils.toString(entity);
		// System.out.println("response length:" + responseString.length());
		// System.out.println("response content:"
		// + responseString.replace("\r\n", ""));
		// }
	}

	/**
	 * 
	 * @param httppost
	 * @param isProxy
	 * @param charset
	 *            true 使用代理，false 不使用代理
	 * @return responseStr
	 */

	public static String executeRandomProxy(HttpGet http, boolean tag,
			String charset) {
		
		/*
		if (result <2) {
			return HttpThreadPool.executeAntProxy(http, true, charset);
		} else if (result>=2 && result<= 12)  {
			return HttpThreadPool.executeAbuyunClient(http, true, charset);
		}else {
			return HttpThreadPool.executeClient(http, true, charset,null);
		}*/
		int result = random.nextInt(20);// 返回[0,10)集合中的整数，注意不包括10
		
		/*long time=System.currentTimeMillis();
		String num= String.valueOf(time);
		String sub=num.substring(num.length()-1, num.length());
		int result=Integer.parseInt(sub);*/
		if(proxyMap.size()==0){
			 if (result<1)  {
				 return HttpThreadPool.executeAntProxy(http, true, charset);
			} else {
				return HttpThreadPool.executeAbuyunClient(http, true, charset);
			}
		}else{
			if (result <1) {
				return HttpThreadPool.executeAntProxy(http, true, charset);
			} else if (result>=1 && result<= 6)  {
				return HttpThreadPool.executeAbuyunClient(http, true, charset);
			}else {
				return HttpThreadPool.executeClient(http, tag, charset,null);
			}
		}
	}

	public static String executeRandomProxy(HttpPost http, boolean tag,
			String charset) {		
		int result = random.nextInt(20);// 返回[0,10)集合中的整数，注意不包括10
		
		/*long time=System.currentTimeMillis();
		String num= String.valueOf(time);
		String sub=num.substring(num.length()-1, num.length());
		int result=Integer.parseInt(sub);*/
		if(proxyMap.size()==0){
			 if (result<1)  {
				 return HttpThreadPool.executeAntProxy(http, true, charset);
			} else {
				return HttpThreadPool.executeAbuyunClient(http, true, charset);
			}
		}else{
			if (result <1) {
				return HttpThreadPool.executeAntProxy(http, true, charset);
			} else if (result>=1 && result<= 6)  {
				return HttpThreadPool.executeAbuyunClient(http, true, charset);
			}else {
				return HttpThreadPool.executeClient(http, tag, charset,null);
			}
		}
		
	}

}
