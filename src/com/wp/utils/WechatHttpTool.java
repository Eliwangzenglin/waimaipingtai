package com.wp.utils;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class WechatHttpTool {
	public static int no_sesion_count = 0;
	private Log log = Log.getLoger();
	private CookieStore cookies = new BasicCookieStore(); 
	
	public CookieStore getCookies() {
		return cookies;
	}


	public void setCookies(CookieStore cookies) {
		this.cookies = cookies;
	}
 
	public static void main(String[] args) {

	}
 
    
	
	/**
	 * 微信阅读
	 * 
	 **/

	public    String doPostRead(String url,int porxyCode,String cookie,int soucre,String biz,String oldReadNum) {
		String api = "http://mp.weixin.qq.com/mp/getappmsgext";
		 	List<NameValuePair> params = getPairParam(url,soucre,biz);
		UrlEncodedFormEntity entity;
		try {
			entity = new UrlEncodedFormEntity(params, "utf-8");
			HttpPost httpPost = new HttpPost(api);
			httpPost.setHeader("X-Requested-With", "com.tencent.mm");
			httpPost.setHeader(
					"User-Agent",
					"Mozilla/5.0 (Linux; Android 4.4.4; MI 4LTE Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 MicroMessenger/6.0.0.50_r844973.501 NetType/WIFI");
			httpPost.setHeader("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			httpPost.setHeader("Connection", "close");
			//String cookie = getCookieStr();
			// log.loger.info("cookie== " + cookie);
			httpPost.setHeader("Cookie", cookie);
			httpPost.setEntity(entity);
			String response = doPost(httpPost,porxyCode,false);
			log.loger.info(response);
			if(response.contains("频繁")){
				log.loger.info("do error .频繁......." );
			}
			if(response.contains("内容因违规无法查") || response.contains("内容已被发布者删")){
				return "-100";
			}
			//String response = doPostClient(httpPost,porxyCode);			
			// log.loger.info(oldReadNum+","+biz+","+response);
			if (response != null && response.startsWith("{")
					&& response.indexOf("read_num") > 0) {
				if(response.contains("ret\":302")){
					return "-1";
				}
				JSONObject jsonObj = new JSONObject(response);
				// {"advertisement_info":[],"advertisement_num":0,"reward_head_imgs":[],"appmsgstat":{"ret":0,"like_num":2,"read_num":172,"show":true,"real_read_num":0,"is_login":true,"liked":false}}
				String startReadNum = "0";
				Object read_num = jsonObj.getJSONObject("appmsgstat").get(
						"read_num");
				if (null != read_num && read_num.toString().length() > 0) {
					startReadNum = read_num.toString();
				}
				return startReadNum;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * 2651424198 MzA3OTczNTMzNQ==   {"super_vote_item":[{"item_idx_list":{"item_idx":[14]},"vote_id":"443956687"}],"super_vote_id":"443956686"}
	 * */
	public boolean doPostVote(String orderBiz, String uin, String key,String voteJson,String cookie,String mid) {
		String api = "http://mp.weixin.qq.com/mp/newappmsgvote"; 
		 	List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("action", "vote"));
			params.add(new BasicNameValuePair("__biz", orderBiz));
			params.add(new BasicNameValuePair("uin", uin));
			params.add(new BasicNameValuePair("key", key));
			params.add(new BasicNameValuePair("f", "json"));
			params.add(new BasicNameValuePair("json",voteJson));
			//log.loger.info(voteJson);
			//params.add(new BasicNameValuePair("json", "{\"super_vote_item\":[{\"vote_id\":"+vote_id+",\"item_idx_list\":{\"item_idx\":[\""+itemNum+"\"]}}],\"super_vote_id\":"+super_vote_id+"\"})"));
			params.add(new BasicNameValuePair("idx", "1"));
			params.add(new BasicNameValuePair("mid", mid));			
		try {
			UrlEncodedFormEntity entity=new UrlEncodedFormEntity(params, "utf-8");			
			HttpPost httpPost = new HttpPost(api);
			 
			httpPost.setHeader("X-Requested-With", "com.tencent.mm");
			httpPost.setHeader(
					"User-Agent",
					"Mozilla/5.0 (Linux; Android 4.4.4; MI 4LTE Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 MicroMessenger/6.0.0.50_r844973.501 NetType/WIFI");
			httpPost.setHeader("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			httpPost.setHeader("Connection", "close"); 
			httpPost.setHeader("Cookie", cookie);
			httpPost.setEntity(entity);
			String response = doPost(httpPost,0,false); 
			//String response =HttpThreadPool.executeAbuyunClient(httpPost, true, "utf-8"); 
			//||response.indexOf("freq")>0 
			if(response.indexOf("no session")>0    ){
				this.no_sesion_count=no_sesion_count+1;
			}
			log.loger.info(response);
			if (response != null && response.startsWith("{")
					&& response.indexOf("errmsg\":\"ok")>0 && response.indexOf("ret")> 0) {
				
				return   true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean doPostVoteNew(String orderBiz, String uin, 
			String key,String voteJson,String cookie,String mid,String wxticket,String wxticketkey) {
		String api = "http://mp.weixin.qq.com/mp/newappmsgvote"; 
		 	List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("action", "vote"));
			params.add(new BasicNameValuePair("__biz", orderBiz));
			params.add(new BasicNameValuePair("uin", uin));
			params.add(new BasicNameValuePair("key", key));
			params.add(new BasicNameValuePair("f", "json"));
			params.add(new BasicNameValuePair("json",voteJson));
			//log.loger.info(voteJson);
			//params.add(new BasicNameValuePair("json", "{\"super_vote_item\":[{\"vote_id\":"+vote_id+",\"item_idx_list\":{\"item_idx\":[\""+itemNum+"\"]}}],\"super_vote_id\":"+super_vote_id+"\"})"));
			params.add(new BasicNameValuePair("idx", "1"));
			params.add(new BasicNameValuePair("mid", mid));			
		try {
			UrlEncodedFormEntity entity=new UrlEncodedFormEntity(params, "utf-8");			
			HttpPost httpPost = new HttpPost(api);			 
			httpPost.setHeader("X-Requested-With", "com.tencent.mm");
			httpPost.setHeader(
					"User-Agent",
					"Mozilla/5.0 (Linux; Android 4.4.4; MI 4LTE Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 MicroMessenger/6.0.0.50_r844973.501 NetType/WIFI");
			httpPost.setHeader("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			httpPost.setHeader("Connection", "close"); 
			//cookie=cookie+"wxticket="+wxticket+";wxticketkey="+wxticketkey;
			log.loger.info(cookie);
			httpPost.setHeader("Cookie", cookie);
			httpPost.setEntity(entity);
			String response = doPost(httpPost,0,false); 
			log.loger.info(response);
			if (response != null && response.startsWith("{")
					&& response.indexOf("errmsg\":\"ok")>0 && response.indexOf("ret")> 0) {				
				return   true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean doPostVoteNewNNNNNNNBBB(String orderBiz, String uin, 
			String key,String voteJson,String cookie,String mid,String pass_ticket,String wxticketkey) {
		
		String api = "http://mp.weixin.qq.com/mp/newappmsgvote"; 
		 	List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("action", "vote"));
			params.add(new BasicNameValuePair("__biz", orderBiz));
			params.add(new BasicNameValuePair("uin", uin));
			params.add(new BasicNameValuePair("key", key));
			params.add(new BasicNameValuePair("f", "json"));
			params.add(new BasicNameValuePair("pass_ticket", pass_ticket));
			params.add(new BasicNameValuePair("json",voteJson));
			//log.loger.info(voteJson);
			//params.add(new BasicNameValuePair("json", "{\"super_vote_item\":[{\"vote_id\":"+vote_id+",\"item_idx_list\":{\"item_idx\":[\""+itemNum+"\"]}}],\"super_vote_id\":"+super_vote_id+"\"})"));
			params.add(new BasicNameValuePair("idx", "1"));
			params.add(new BasicNameValuePair("mid", mid));			
		try {
			UrlEncodedFormEntity entity=new UrlEncodedFormEntity(params, "utf-8");			
			HttpPost httpPost = new HttpPost(api);			 
			httpPost.setHeader("X-Requested-With", "com.tencent.mm");
			httpPost.setHeader(
					"User-Agent",
					"Mozilla/5.0 (Linux; Android 4.4.4; MI 4LTE Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 MicroMessenger/6.0.0.50_r844973.501 NetType/WIFI");
			httpPost.setHeader("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			httpPost.setHeader("Connection", "close"); 
			//cookie=cookie+"wxticket="+wxticket+";wxticketkey="+wxticketkey;
			log.loger.info(cookie);
			httpPost.setHeader("Cookie", cookie);
			httpPost.setEntity(entity);
			String response = doPost(httpPost,0,false); 
			log.loger.info(response);
			if (response != null && response.startsWith("{")
					&& response.indexOf("errmsg\":\"ok")>0 && response.indexOf("ret")> 0) {				
				return   true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * 点赞接口
	 * */
	public   boolean doPostZan(String url,int porxyCode,String cookie) {
		String api = "http://mp.weixin.qq.com/mp/appmsg_like";
		List<NameValuePair> params = getPairParamZan(url);
		UrlEncodedFormEntity entity;
		try {
			entity = new UrlEncodedFormEntity(params, "utf-8");
			HttpPost httpPost = new HttpPost(api);
			httpPost.setHeader("X-Requested-With", "com.tencent.mm");
			httpPost.setHeader(
					"User-Agent",
					"Mozilla/5.0 (Linux; Android 4.4.4; MI 4LTE Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 MicroMessenger/6.0.0.50_r844973.501 NetType/WIFI");
			httpPost.setHeader("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			httpPost.setHeader("Connection", "close");
			httpPost.setHeader("Cookie",cookie);
			httpPost.setEntity(entity);
			String response = doPost(httpPost,porxyCode,false);
			//log.loger.info("zan," + response);
			//{"base_resp":{"ret":0,"errmsg":"ok"}}
			if (response != null && response.startsWith("{")
					&& response.indexOf("errmsg\":\"ok") > 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public   List<NameValuePair> getPairParamZan(String url) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("__biz", getObject(url, "__biz=")));
		params.add(new BasicNameValuePair("mid", getObject(url, "mid=")));
		params.add(new BasicNameValuePair("idx", getObject(url, "idx=")));
		params.add(new BasicNameValuePair("like", "1")); // 1:点赞 ，0：取消赞
		params.add(new BasicNameValuePair("f", "json"));
		params.add(new BasicNameValuePair("appmsgid", getObject(url,
				"appmsgid=")));
		params.add(new BasicNameValuePair("itemidx", ""));
		params.add(new BasicNameValuePair("uin", getObject(url, "uin=")));
		params.add(new BasicNameValuePair("key", getObject(url, "key=")));
		params.add(new BasicNameValuePair("pass_ticket", getObject(url,
				"pass_ticket=")));
		params.add(new BasicNameValuePair("wxtoken", getObject(url, "wxtoken=")));
		params.add(new BasicNameValuePair("devicetype", "android-19"));
		params.add(new BasicNameValuePair("clientversion", "26000032&x5=0"));
		return params;
	}
	/**
	 * 这种数据是第一次请求获取cookie，第二次请求获取qzone的cookie
	 * 
	 * @param lineTxt
	 * @param jedis
	 * @return
	 */
	private   String doPost(HttpPost httpOne,int proxyCode,boolean setCookie) {

		CloseableHttpResponse response = null;
		CloseableHttpClient closeableHttpClient = null;
		RequestConfig config = getConfig();
		String responseStr = "";
		try {
			cookies.clear();
			//closeableHttpClient = getAbuyunClient(proxyCode,setCookie);
			closeableHttpClient = HttpUtil.getHttpClient(cookies);
			httpOne.setConfig(config);
			try {
				//responseStr = HttpThreadPool.executeRandomProxy(httpOne,false, "UTF-8");
				 response = closeableHttpClient.execute(httpOne);
				 HttpEntity entity = response.getEntity();
				responseStr = EntityUtils.toString(entity, "utf-8"); 					
			} catch (Exception ex) {
			//	ex.printStackTrace();
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
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
		return null;
	}
	public   String doGet(HttpGet httpOne) {
		CloseableHttpResponse response = null;
		CloseableHttpClient closeableHttpClient = null;
		RequestConfig config = getConfig();
		String responseStr = "";
		try {
			cookies.clear();
			//closeableHttpClient = getAbuyunClient(proxyCode,setCookie);
			closeableHttpClient = HttpUtil.getHttpClient(cookies);
			httpOne.setConfig(config);
			try {
				//responseStr = HttpThreadPool.executeRandomProxy(httpOne,false, "UTF-8");
				 response = closeableHttpClient.execute(httpOne);
				 HttpEntity entity = response.getEntity();
				responseStr = EntityUtils.toString(entity, "utf-8"); 					
			} catch (Exception ex) {
			//	ex.printStackTrace();
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
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
		return null;
	}
	private static RequestConfig getConfig() {
			return RequestConfig.custom().setConnectTimeout(1000)
				.setSocketTimeout(1000).setConnectionRequestTimeout(1000)
				.build();
	}
	public     String getCookieNewForerror(String url,int proxyCode) {
		//log.loger.info(url);
		HttpGet httpOne = new HttpGet(url);
		CloseableHttpResponse response = null;
		CloseableHttpClient closeableHttpClient = null;
		RequestConfig config = getConfig();
		//closeableHttpClient = getAbuyunClient(proxyCode,true);
		closeableHttpClient = HttpUtil.getHttpClient(cookies);
		
		/** 读取服务器返回过来的json字符串数据 **/
 
		try {
			httpOne.setHeader("Connection", "close");
			httpOne.setConfig(config);	 
			cookies.clear();
			response = closeableHttpClient.execute(httpOne);
			HttpEntity entity = response.getEntity();
			String httpStr = EntityUtils.toString(entity);
			
			log.loger.info(httpStr);
			String mycookie= getCookieStr();
			if("".equals(mycookie)){
				if(httpStr.indexOf("内容已被发布者删除")>0){
					return "-100";
				}
			}else{
				return mycookie;
			}
			
		} catch (Exception ex) {
			//ex.printStackTrace();
		} finally {
			try {
				if (response != null) {
					EntityUtils.consumeQuietly(response.getEntity());
					response.getEntity().getContent().close();
					response.close();
				}
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
		return null;
	}
	public     String getCookieNew(String url,int proxyCode) {
		HttpGet httpOne = new HttpGet(url);
		CloseableHttpResponse response = null;
		CloseableHttpClient closeableHttpClient = null;
		RequestConfig config = getConfig();
		closeableHttpClient = HttpUtil.getHttpClient(cookies);
		
		/** 读取服务器返回过来的json字符串数据 **/
 
		try {
			httpOne.setHeader("Connection", "close");
			httpOne.setConfig(config);	 
			cookies.clear();
			response = closeableHttpClient.execute(httpOne);
			HttpEntity entity = response.getEntity();
			String responseStr = EntityUtils.toString(entity);			
			String mycookie= getCookieStr();
			if("".equals(mycookie)){
				if(responseStr.indexOf("此内容因违规无法查看")>0 || responseStr.indexOf("内容已被发布者删除")>0 
						|| responseStr.indexOf("临时链接已失效")>0 ){
					return "-100";
				}
			}else{
				return mycookie;
			}
			
		} catch (Exception ex) {
			//ex.printStackTrace();
		} finally {
			try {
				if (response != null) {
					EntityUtils.consumeQuietly(response.getEntity());
					response.getEntity().getContent().close();
					response.close();
				}
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
		return null;
	}
	public     String getCookieByCookie(String cookie,String url,int proxyCode) {
		//log.loger.info(url);
		HttpGet httpOne = new HttpGet(url);
		CloseableHttpResponse response = null;
		CloseableHttpClient closeableHttpClient = null;
		RequestConfig config = getConfig();
		//closeableHttpClient = getAbuyunClient(proxyCode,true);
		closeableHttpClient = HttpUtil.getHttpClient(cookies);
		/** 读取服务器返回过来的json字符串数据 **/
		try {
			cookies.clear();
			httpOne.setHeader("Connection", "close");
			httpOne.setHeader("Cookie", cookie);
			httpOne.setConfig(config);			
			//String httpStr = "";
			response = closeableHttpClient.execute(httpOne);
			 HttpEntity entity = response.getEntity();
			String responseStr = EntityUtils.toString(entity, "utf-8"); 
			if(responseStr.indexOf("此内容因违规无法查看")>0 || responseStr.indexOf("内容已被发布者删除")>0 
					|| responseStr.indexOf("临时链接已失效")>0 ){
				return "-2";
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
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
		return null;
	}
	public   String getCookieStr() {
		StringBuffer stringCookie = new StringBuffer();
		List<Cookie> cookielist = cookies.getCookies();
		try {
			for (int i = 0; i < cookielist.size(); i++) { 
				stringCookie.append(
						cookielist.get(i).getName() + "="
								+ cookielist.get(i).getValue()).append(";");
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return stringCookie.toString();
	}

	/**
	 * 
	 * @param line
	 * @param key
	 * @return
	 */
	private static String getObject(String line, String key) {
		int start = line.indexOf(key);
		if (start < 0) {
			return null;
		}
		String sub = line.substring(start);
		int end = sub.indexOf("&");
		if (end < key.length()) {
			return null;
		}
		return sub.substring(key.length(), end);
	}
 
	public static Map<String, Object> parse_qs(String url) {

		Map<String, Object> map = new HashMap<String, Object>(0);
		if (StringUtils.isBlank(url)) {
			return map;
		}
		String[] params = url.split("&");
		for (int i = 0; i < params.length; i++) {
			String[] p = params[i].split("=");
			if (p.length == 2) {
				map.put(p[0], p[1]);
			}
		}
		return map;
	}
	/**
	 * # vote_id 723673042 和super_vote_id 这个两个需要从另外链接提取，再下单的地方直接提取  这个下单的时候告我投哪个票 ：item_idx
	 * @param url
	 * @param source
	 * @param targetBiz
	 * @return
	 */
	public   List<NameValuePair> getPairParam(String url,int source,String targetBiz) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("__biz", getObject(url, "__biz=")));
		params.add(new BasicNameValuePair("appmsg_type", "9"));
		params.add(new BasicNameValuePair("mid", getObject(url, "mid=")));
		params.add(new BasicNameValuePair("sn", getObject(url, "sn=")));
		params.add(new BasicNameValuePair("idx", getObject(url, "idx=")));
		/**
		 * * 公众号会话    0
		 * 好友转发 1
		 * 朋友圈     2
		 * 历史消息 37  
		 */
		//int myrandom=random.nextInt(10); 
		params.add(new BasicNameValuePair("scene", source+""));
		params.add(new BasicNameValuePair("f", "json"));
		params.add(new BasicNameValuePair("reward_uin_count", "1"));
		params.add(new BasicNameValuePair("is_need_ad", "0"));
		params.add(new BasicNameValuePair("msg_daily_idx", "1"));
		if(targetBiz==null|| "".equals(targetBiz) || "null".equals(targetBiz)){
			params.add(new BasicNameValuePair("key", getObject(url, "key=")));
			params.add(new BasicNameValuePair("uin", getObject(url, "uin=")));
			params.add(new BasicNameValuePair("pass_ticket", getObject(url,	"pass_ticket=")));
		}
		params.add(new BasicNameValuePair("devicetype", "android-19"));
		params.add(new BasicNameValuePair("clientversion", "26000032&x5=0"));
		params.add(new BasicNameValuePair("is_only_read", "1"));
		params.add(new BasicNameValuePair("is_temp_url", "0"));
		return params;
	}
	public     String doGet(String url,int proxyCode) {
		//log.loger.info(url);
		HttpGet httpOne = new HttpGet(url);
		CloseableHttpResponse response = null;
		CloseableHttpClient closeableHttpClient = null;
		RequestConfig config = getConfig();
		//closeableHttpClient = getAbuyunClient(proxyCode,true);
		closeableHttpClient = HttpUtil.getHttpClient(cookies);
		
		/** 读取服务器返回过来的json字符串数据 **/
 
		try {
			httpOne.setHeader("Connection", "close");
			httpOne.setConfig(config);	 
			cookies.clear();
			response = closeableHttpClient.execute(httpOne);
			//HttpEntity entity = response.getEntity();
			//httpStr = EntityUtils.toString(entity);
			/*if ("".equals(httpStr)) {
				Thread.sleep(500);
				continue;
			} else {
				break;
			}*/
			return getCookieStr();
		} catch (Exception ex) {
			//ex.printStackTrace();
		} finally {
			try {
				if (response != null) {
					EntityUtils.consumeQuietly(response.getEntity());
					response.getEntity().getContent().close();
					response.close();
				}
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
		return null;
	}
}
