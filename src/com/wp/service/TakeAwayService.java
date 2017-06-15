package com.wp.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import redis.clients.jedis.Jedis;

import com.wp.utils.HttpThreadPool;
import com.wp.utils.Log;
import com.wp.utils.RedisServer;

/**
 * 获取百度外卖商家信息
 *
 */
public class TakeAwayService implements Runnable {

	private boolean IS_CHANGE_SHOP = false;// 判断本次循环商家是否变更过

	public static final String INTE_PARAM_URL = "http://waimai.baidu.com/waimai?qt=shoplist";// 获取接口参数地址

	private static final String HOSTS = "http://waimai.baidu.com";// 接口域名

	private static final String SHOP_KEY = "baidu.takeaway.shop";// 缓存key

	private static Log log = Log.getLoger();

	/**
	 * 获取目标商家信息
	 * 
	 * @param lat
	 * @param lng
	 */
	public String[] getDesShopInfo(String lng, String lat) {
		this.IS_CHANGE_SHOP = false;
		String interParam = getInterParam(lat, lng);
		if (!StringUtils.isEmpty(interParam)) {
			return getShopInfos(0, interParam);
		}
		return null;
	}

	/**
	 * @param lng
	 *            经度
	 * @param lat
	 *            纬度
	 * @return
	 */
	public String[] getLngLat(Map<String, String> key_shopId) {
		String[] strAry = new String[4];
		List<BigDecimal> latList = new ArrayList<BigDecimal>();
		List<BigDecimal> lngList = new ArrayList<BigDecimal>();
		try {
			if (key_shopId != null && key_shopId.size() > 0) {
				Set<String> bSet = key_shopId.keySet();
				for (String shopId : bSet) {
					String shopInfo = key_shopId.get(shopId);
					if (!StringUtils.isEmpty(shopInfo)) {
						String[] sis = shopInfo.split("#_#");
						latList.add(new BigDecimal(sis[5]));
						lngList.add(new BigDecimal(sis[6]));
					}
				}
			}
			if (latList != null && latList.size() > 0 && lngList != null
					&& lngList.size() > 0) {
				strAry[0] = String.valueOf(Collections.max(lngList));// 最大经度
				strAry[1] = String.valueOf(Collections.min(lngList));// 最小经度
				strAry[2] = String.valueOf(Collections.max(latList));// 最大纬度
				strAry[3] = String.valueOf(Collections.min(latList));// 最小纬度
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strAry;
	}

	public Map<String, String> HandleShopInfos(
			Map<String, String> shopMapInfos, int page, String tUrl) {
		HttpGet httpGet = null;
		Jedis redius = null;
		String url = HOSTS + tUrl + "?display=json&page=" + page
				+ "&count=40&sortby=distance";
		try {
			httpGet = new HttpGet(url);
			String result = HttpThreadPool.executeClient(httpGet, false,
					"utf-8", null);
			if (!StringUtils.isEmpty(result)) {
				JSONObject jo = JSONObject.fromObject(result);
				JSONObject joResult = jo.has("result") ? jo
						.getJSONObject("result") : null;
				if (joResult != null) {
					redius = RedisServer.bizRedisPool.getResource();
					JSONArray shop_info = joResult.has("shop_info") ? joResult
							.getJSONArray("shop_info") : null;
					if (shop_info != null && shop_info.size() > 0) {
						for (int i = 0; i < shop_info.size(); i++) {
							JSONObject shop = (JSONObject) shop_info.get(i);
							String saled_month = shop.has("saled_month") ? shop
									.getString("saled_month") : "0";
							String shop_id = shop.has("shop_id") ? shop
									.getString("shop_id") : null;
							String average_score = shop.has("average_score") ? shop
									.getString("average_score") : "0";
							String shop_lng = shop.has("shop_lng") ? shop
									.getString("shop_lng") : null;
							String shop_lat = shop.has("shop_lat") ? shop
									.getString("shop_lat") : null;
							String category = shop.has("category") ? shop
									.getString("category") : null;
							String shop_name = shop.has("shop_name") ? shop
									.getString("shop_name") : null;
							String fr = shop_id + "#_#" + shop_name + "#_#"
									+ category + "#_#" + average_score + "#_#"
									+ saled_month + "#_#" + shop_lat + "#_#"
									+ shop_lng;
							shopMapInfos.put(shop_id, fr);
							if (redius.hexists(SHOP_KEY, shop_id)) {
								continue;
							}
							redius.hset(SHOP_KEY, shop_id, fr);
							this.IS_CHANGE_SHOP = true;
						}
						RedisServer.bizRedisPool.returnResource(redius);
						HandleShopInfos(shopMapInfos, page + 1, tUrl);
					}

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			log.loger.info("获取商家出错："+e.getMessage());
		} finally {
			if (redius != null) {
				RedisServer.bizRedisPool.returnResource(redius);
			}
		}
		return shopMapInfos;
	}

	/**
	 * 获取店铺信息
	 * 
	 * @param page
	 */
	public String[] getShopInfos(int page, String tUrl) {
		Map<String, String> shopMapInfos = new HashMap<String, String>();
		Map<String, String> map = HandleShopInfos(shopMapInfos, page, tUrl);
		if (this.IS_CHANGE_SHOP) {
			return getLngLat(map);
		} else {
			return null;
		}

	}

	// 获取接口路径参数
	public String getInterParam(String lat, String lng) {
		HttpGet httpGet = null;
		try {
			String url = INTE_PARAM_URL + "&lat=" + lat + "&lng=" + lng;
			httpGet = new HttpGet(url);
			String result = HttpThreadPool.executeClient(httpGet, false,
					"utf-8", null);
			if (!StringUtils.isEmpty(result)) {
				Document desHtml = Jsoup.parse(result);
				Element a = desHtml.getElementById("f-close-btn");
				String tempUrl = a.attr("href");
				if (!StringUtils.isEmpty(tempUrl)) {
					return tempUrl;
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public void run() {
		while (true) {
			doGetLoc();
		}
	}

	public void doQuerySJ() {
		String[] nextLoc = getDesShopInfo("1.353761351E7", "4827572.6");
		if (nextLoc != null) {
			System.out.println(nextLoc[0] + "#_#" + nextLoc[1] + "#_#"
					+ nextLoc[2] + "#_#" + nextLoc[3]);
		} else {
			System.out.println("null");
		}
	}

	public void doGetCity(String lng, String lat) {
		double lngRes = Double.parseDouble(lng) / 100000;
		double latRes = Double.parseDouble(lat) / 100000;
		String baiduUrl = "http://api.map.baidu.com/geocoder/v2/?location="
				+ latRes + "," + lngRes
				+ "&output=json&pois=1&ak=YXWgBvybLGadcBaRAKs6fDz5jGxIwRap";
		System.out.println(baiduUrl);
		HttpGet httpGet = null;
		try {
			httpGet = new HttpGet(baiduUrl);
			String result = HttpThreadPool.executeClient(httpGet, false,
					"utf-8", null);
			if (!StringUtils.isEmpty(result)) {
				System.out.println(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

	}

	/**
	 * 获取商家信息存入换粗
	 */
	public void doGetLoc() {
		Jedis jedis = null;
		try {
			jedis = RedisServer.bizRedisPool.getResource();
			String stringStr = jedis.rpop("baidu.takeaway.loc");// 缓存值格式:lng#_#lat#_#MaxLng#_#MinLng#_#MaxLat#_#MinLat
			if (stringStr == null) {
				String lngLat = jedis.rpop("baidu.takeaway.lng.lat");
				String[] tempLngLat = { "13521046.53", "3643996.82" };// 给个默认值
				if (!StringUtils.isEmpty(lngLat)) {
					tempLngLat = lngLat.split("#_#");
				}
				String[] nextLoc = getDesShopInfo(tempLngLat[0], tempLngLat[1]);
				if (nextLoc != null && nextLoc.length > 3) {
					double nextBigLng = Double.parseDouble(nextLoc[0]);
					double nextSmallLng = Double.parseDouble(nextLoc[1]);
					double nextBigLat = Double.parseDouble(nextLoc[2]);
					double nextSmallLat = Double.parseDouble(nextLoc[3]);
					jedis.lpush("baidu.takeaway.loc", "" + nextBigLng + "#_#"
							+ nextBigLat + "#_#" + nextBigLng + "#_#"
							+ nextSmallLng + "#_#" + nextBigLat + "#_#"
							+ nextSmallLat);
					jedis.lpush("baidu.takeaway.loc", "" + nextSmallLng + "#_#"
							+ nextBigLat + "#_#" + nextBigLng + "#_#"
							+ nextSmallLng + "#_#" + nextBigLat + "#_#"
							+ nextSmallLat);
					jedis.lpush("baidu.takeaway.loc", "" + nextBigLng + "#_#"
							+ nextSmallLat + "#_#" + nextBigLng + "#_#"
							+ nextSmallLng + "#_#" + nextBigLat + "#_#"
							+ nextSmallLat);
					jedis.lpush("baidu.takeaway.loc", "" + nextSmallLng + "#_#"
							+ nextSmallLat + "#_#" + nextBigLng + "#_#"
							+ nextSmallLng + "#_#" + nextBigLat + "#_#"
							+ nextSmallLat);
				}
				return;
			}
			String[] nowLoc = stringStr.split("#_#");
			if (nowLoc == null || nowLoc.length < 4) {
				return;
			}

			double preMaxLng = Double.parseDouble(nowLoc[2]);
			double preMinLng = Double.parseDouble(nowLoc[3]);
			double preMaxLat = Double.parseDouble(nowLoc[4]);
			double preMinLat = Double.parseDouble(nowLoc[5]);
			String[] nextLoc = getDesShopInfo(nowLoc[0], nowLoc[1]);
			if (nextLoc != null) {
				double nextBigLng = Double.parseDouble(nextLoc[0]);
				double nextSmallLng = Double.parseDouble(nextLoc[1]);
				double nextBigLat = Double.parseDouble(nextLoc[2]);
				double nextSmallLat = Double.parseDouble(nextLoc[3]);
				// 得到四个点，分别是
				// nextBigLng,nextBigLat---nextBigLng,nextSmallLat---nextSmallLng,nextBigLat---nextSmallLng,nextSmallLat
				if (nextBigLng >= preMinLng && nextBigLng <= preMaxLng
						&& nextBigLat >= preMinLat && nextBigLat <= preMaxLat) {
					// 落在上一次的区域中
				} else {// 不落在上一次区域中
					String strResult = "" + nextBigLng + "#_#" + nextBigLat
							+ "#_#" + nextBigLng + "#_#" + nextSmallLng + "#_#"
							+ nextBigLat + "#_#" + nextSmallLat;
					jedis.lpush("baidu.takeaway.loc", strResult);
				}
				if (nextBigLng >= preMinLng && nextBigLng <= preMaxLng
						&& nextSmallLat >= preMinLat
						&& nextSmallLat <= preMaxLat) {
					// 落在上一次的区域中
				} else {// 不落在上一次区域中
					String strResult = "" + nextBigLng + "#_#" + nextSmallLat
							+ "#_#" + nextBigLng + "#_#" + nextSmallLng + "#_#"
							+ nextBigLat + "#_#" + nextSmallLat;
					jedis.lpush("baidu.takeaway.loc", strResult);
				}
				if (nextSmallLng >= preMinLng && nextSmallLng <= preMaxLng
						&& nextBigLat >= preMinLat && nextBigLat <= preMaxLat) {
					// 落在上一次的区域中
				} else {// 不落在上一次区域中
					String strResult = "" + nextSmallLng + "#_#" + nextBigLat
							+ "#_#" + nextBigLng + "#_#" + nextSmallLng + "#_#"
							+ nextBigLat + "#_#" + nextSmallLat;
					jedis.lpush("baidu.takeaway.loc", strResult);
				}
				if (nextSmallLng >= preMinLng && nextSmallLng <= preMaxLng
						&& nextSmallLat >= preMinLat
						&& nextSmallLat <= preMaxLat) {
					// 落在上一次的区域中
				} else {// 不落在上一次区域中
					String strResult = "" + nextSmallLng + "#_#" + nextSmallLat
							+ "#_#" + nextBigLng + "#_#" + nextSmallLng + "#_#"
							+ nextBigLat + "#_#" + nextSmallLat;
					jedis.lpush("baidu.takeaway.loc", strResult);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			RedisServer.bizRedisPool.returnResource(jedis);
		}
	}

}
