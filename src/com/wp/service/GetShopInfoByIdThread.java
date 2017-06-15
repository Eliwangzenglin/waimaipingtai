package com.wp.service;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;

import com.mchange.v3.decode.DecodeUtils;
import com.wp.utils.HttpThreadPool;
import com.wp.utils.Log;
import com.wp.utils.RedisServer;
import com.wp.utils.Utils;

/**
 *获取商家信息 
 *
 */
public class GetShopInfoByIdThread implements Runnable {

	Log log = Log.getLoger();

	private int count = 0;

	private static final String SHOPINFOURL = "https://waimai.baidu.com/waimai/shop/";

	private static final String FILAPATH = "/home/crawer-data/baidu/";

	private static final String BAIDU_WAIMAI_FORDER_SEQ = "baidu.waimai.forder.seq";

	private static JSONArray citys = JSONArray
			.fromObject("[{\"name\":\"安国\",\"cityId\":\"2040\"},{\"name\":\"安平\",\"cityId\":\"2641\"},{\"name\":\"安庆\",\"cityId\":\"130\"},{\"name\":\"鞍山\",\"cityId\":\"320\"},{\"name\":\"安顺\",\"cityId\":\"263\"},{\"name\":\"安阳\",\"cityId\":\"267\"},{\"name\":\"白城\",\"cityId\":\"51\"},{\"name\":\"保定\",\"cityId\":\"307\"},{\"name\":\"宝鸡\",\"cityId\":\"171\"},{\"name\":\"包头\",\"cityId\":\"229\"},{\"name\":\"宝应\",\"cityId\":\"1740\"},{\"name\":\"北京\",\"cityId\":\"131\"},{\"name\":\"北镇\",\"cityId\":\"1946\"},{\"name\":\"蚌埠\",\"cityId\":\"126\"},{\"name\":\"本溪\",\"cityId\":\"227\"},{\"name\":\"宾\",\"cityId\":\"451\"},{\"name\":\"滨州\",\"cityId\":\"235\"},{\"name\":\"亳州\",\"cityId\":\"188\"},{\"name\":\"沧州\",\"cityId\":\"149\"},{\"name\":\"长春\",\"cityId\":\"53\"},{\"name\":\"常德\",\"cityId\":\"219\"},{\"name\":\"长葛\",\"cityId\":\"1262\"},{\"name\":\"长沙\",\"cityId\":\"158\"},{\"name\":\"长治\",\"cityId\":\"356\"},{\"name\":\"常州\",\"cityId\":\"348\"},{\"name\":\"巢湖\",\"cityId\":\"251\"},{\"name\":\"朝阳\",\"cityId\":\"280\"},{\"name\":\"潮州\",\"cityId\":\"201\"},{\"name\":\"承德\",\"cityId\":\"207\"},{\"name\":\"成都\",\"cityId\":\"75\"},{\"name\":\"郴州\",\"cityId\":\"275\"},{\"name\":\"赤峰\",\"cityId\":\"297\"},{\"name\":\"茌平\",\"cityId\":\"2170\"},{\"name\":\"池州\",\"cityId\":\"299\"},{\"name\":\"重庆\",\"cityId\":\"132\"},{\"name\":\"楚雄彝族自治州\",\"cityId\":\"105\"},{\"name\":\"滁州\",\"cityId\":\"189\"},{\"name\":\"慈溪\",\"cityId\":\"1879\"},{\"name\":\"大安\",\"cityId\":\"466\"},{\"name\":\"大连\",\"cityId\":\"167\"},{\"name\":\"大理\",\"cityId\":\"111\"},{\"name\":\"丹东\",\"cityId\":\"282\"},{\"name\":\"大庆\",\"cityId\":\"50\"},{\"name\":\"大同\",\"cityId\":\"355\"},{\"name\":\"达州\",\"cityId\":\"369\"},{\"name\":\"德惠\",\"cityId\":\"471\"},{\"name\":\"灯塔\",\"cityId\":\"1528\"},{\"name\":\"德阳\",\"cityId\":\"74\"},{\"name\":\"德州\",\"cityId\":\"372\"},{\"name\":\"定州\",\"cityId\":\"1232\"},{\"name\":\"东莞\",\"cityId\":\"119\"},{\"name\":\"东海\",\"cityId\":\"2663\"},{\"name\":\"东沙群岛\",\"cityId\":\"120\"},{\"name\":\"东营\",\"cityId\":\"174\"},{\"name\":\"敦化\",\"cityId\":\"476\"},{\"name\":\"鄂尔多斯\",\"cityId\":\"283\"},{\"name\":\"繁昌\",\"cityId\":\"1542\"},{\"name\":\"肥东\",\"cityId\":\"1950\"},{\"name\":\"肥西\",\"cityId\":\"2298\"},{\"name\":\"丰城\",\"cityId\":\"1769\"},{\"name\":\"丰\",\"cityId\":\"1331\"},{\"name\":\"佛山\",\"cityId\":\"138\"},{\"name\":\"抚顺\",\"cityId\":\"184\"},{\"name\":\"阜新\",\"cityId\":\"59\"},{\"name\":\"阜阳\",\"cityId\":\"128\"},{\"name\":\"福州\",\"cityId\":\"300\"},{\"name\":\"抚州\",\"cityId\":\"226\"},{\"name\":\"赣州\",\"cityId\":\"365\"},{\"name\":\"公主岭\",\"cityId\":\"484\"},{\"name\":\"广州\",\"cityId\":\"257\"},{\"name\":\"故城\",\"cityId\":\"1921\"},{\"name\":\"桂林\",\"cityId\":\"142\"},{\"name\":\"贵阳\",\"cityId\":\"146\"},{\"name\":\"哈尔滨\",\"cityId\":\"48\"},{\"name\":\"海口\",\"cityId\":\"125\"},{\"name\":\"海伦\",\"cityId\":\"422\"},{\"name\":\"邯郸\",\"cityId\":\"151\"},{\"name\":\"杭州\",\"cityId\":\"179\"},{\"name\":\"合肥\",\"cityId\":\"127\"},{\"name\":\"黑山\",\"cityId\":\"523\"},{\"name\":\"衡水\",\"cityId\":\"208\"},{\"name\":\"河源\",\"cityId\":\"200\"},{\"name\":\"菏泽\",\"cityId\":\"353\"},{\"name\":\"淮安\",\"cityId\":\"162\"},{\"name\":\"淮北\",\"cityId\":\"253\"},{\"name\":\"淮南\",\"cityId\":\"250\"},{\"name\":\"黄冈\",\"cityId\":\"271\"},{\"name\":\"黄山\",\"cityId\":\"252\"},{\"name\":\"黄石\",\"cityId\":\"311\"},{\"name\":\"呼和浩特\",\"cityId\":\"321\"},{\"name\":\"珲春\",\"cityId\":\"509\"},{\"name\":\"惠州\",\"cityId\":\"301\"},{\"name\":\"葫芦岛\",\"cityId\":\"319\"},{\"name\":\"霍邱\",\"cityId\":\"2498\"},{\"name\":\"湖州\",\"cityId\":\"294\"},{\"name\":\"将乐\",\"cityId\":\"1133\"},{\"name\":\"江门\",\"cityId\":\"302\"},{\"name\":\"建瓯\",\"cityId\":\"2310\"},{\"name\":\"吉安\",\"cityId\":\"318\"},{\"name\":\"蛟河\",\"cityId\":\"478\"},{\"name\":\"焦作\",\"cityId\":\"211\"},{\"name\":\"贾汪区\",\"cityId\":\"1760\"},{\"name\":\"嘉祥\",\"cityId\":\"1402\"},{\"name\":\"嘉兴\",\"cityId\":\"334\"},{\"name\":\"嘉峪关\",\"cityId\":\"33\"},{\"name\":\"吉林\",\"cityId\":\"55\"},{\"name\":\"济南\",\"cityId\":\"288\"},{\"name\":\"晋城\",\"cityId\":\"290\"},{\"name\":\"景德镇\",\"cityId\":\"225\"},{\"name\":\"景\",\"cityId\":\"2558\"},{\"name\":\"荆州\",\"cityId\":\"157\"},{\"name\":\"金华\",\"cityId\":\"333\"},{\"name\":\"济宁\",\"cityId\":\"286\"},{\"name\":\"金坛\",\"cityId\":\"2118\"},{\"name\":\"金乡\",\"cityId\":\"2880\"},{\"name\":\"晋中\",\"cityId\":\"238\"},{\"name\":\"锦州\",\"cityId\":\"166\"},{\"name\":\"九江\",\"cityId\":\"349\"},{\"name\":\"九台\",\"cityId\":\"479\"},{\"name\":\"鸡西\",\"cityId\":\"46\"},{\"name\":\"冀州\",\"cityId\":\"2046\"},{\"name\":\"句容\",\"cityId\":\"1322\"},{\"name\":\"莒南\",\"cityId\":\"2582\"},{\"name\":\"开封\",\"cityId\":\"210\"},{\"name\":\"开原\",\"cityId\":\"1046\"},{\"name\":\"昆明\",\"cityId\":\"104\"},{\"name\":\"莱芜\",\"cityId\":\"124\"},{\"name\":\"廊坊\",\"cityId\":\"191\"},{\"name\":\"兰州\",\"cityId\":\"36\"},{\"name\":\"连云港\",\"cityId\":\"347\"},{\"name\":\"聊城\",\"cityId\":\"366\"},{\"name\":\"辽阳\",\"cityId\":\"351\"},{\"name\":\"辽源\",\"cityId\":\"183\"},{\"name\":\"临汾\",\"cityId\":\"368\"},{\"name\":\"凌海\",\"cityId\":\"1364\"},{\"name\":\"临朐\",\"cityId\":\"1823\"},{\"name\":\"临沂\",\"cityId\":\"234\"},{\"name\":\"丽水\",\"cityId\":\"292\"},{\"name\":\"溧水\",\"cityId\":\"2116\"},{\"name\":\"六安\",\"cityId\":\"298\"},{\"name\":\"六盘水\",\"cityId\":\"147\"},{\"name\":\"溧阳\",\"cityId\":\"1747\"},{\"name\":\"龙岩\",\"cityId\":\"193\"},{\"name\":\"娄底\",\"cityId\":\"221\"},{\"name\":\"庐江\",\"cityId\":\"1107\"},{\"name\":\"漯河\",\"cityId\":\"344\"},{\"name\":\"洛阳\",\"cityId\":\"153\"},{\"name\":\"吕梁\",\"cityId\":\"327\"},{\"name\":\"马鞍山\",\"cityId\":\"358\"},{\"name\":\"满城\",\"cityId\":\"1228\"},{\"name\":\"梅河口\",\"cityId\":\"501\"},{\"name\":\"梅州\",\"cityId\":\"141\"},{\"name\":\"蒙城\",\"cityId\":\"1100\"},{\"name\":\"绵阳\",\"cityId\":\"240\"},{\"name\":\"牡丹江\",\"cityId\":\"49\"},{\"name\":\"南昌\",\"cityId\":\"163\"},{\"name\":\"南充\",\"cityId\":\"291\"},{\"name\":\"南宫\",\"cityId\":\"2638\"},{\"name\":\"南和\",\"cityId\":\"1244\"},{\"name\":\"南京\",\"cityId\":\"315\"},{\"name\":\"南宁\",\"cityId\":\"261\"},{\"name\":\"南通\",\"cityId\":\"161\"},{\"name\":\"南阳\",\"cityId\":\"309\"},{\"name\":\"南平\",\"cityId\":\"133\"},{\"name\":\"宁波\",\"cityId\":\"180\"},{\"name\":\"宁德\",\"cityId\":\"192\"},{\"name\":\"宁晋\",\"cityId\":\"1238\"},{\"name\":\"农安\",\"cityId\":\"472\"},{\"name\":\"盘锦\",\"cityId\":\"228\"},{\"name\":\"磐石\",\"cityId\":\"489\"},{\"name\":\"攀枝花\",\"cityId\":\"81\"},{\"name\":\"沛\",\"cityId\":\"1746\"},{\"name\":\"平顶山\",\"cityId\":\"213\"},{\"name\":\"平乡\",\"cityId\":\"1666\"},{\"name\":\"萍乡\",\"cityId\":\"350\"},{\"name\":\"邳州\",\"cityId\":\"1329\"},{\"name\":\"浦城\",\"cityId\":\"2525\"},{\"name\":\"莆田\",\"cityId\":\"195\"},{\"name\":\"濮阳\",\"cityId\":\"209\"},{\"name\":\"黔东南苗族侗族自治州\",\"cityId\":\"342\"},{\"name\":\"黔南布依族苗族自治州\",\"cityId\":\"306\"},{\"name\":\"启东\",\"cityId\":\"1508\"},{\"name\":\"青岛\",\"cityId\":\"236\"},{\"name\":\"清河\",\"cityId\":\"1242\"},{\"name\":\"庆阳\",\"cityId\":\"135\"},{\"name\":\"青州\",\"cityId\":\"2169\"},{\"name\":\"秦皇岛\",\"cityId\":\"148\"},{\"name\":\"琼海\",\"cityId\":\"2358\"},{\"name\":\"齐齐哈尔\",\"cityId\":\"41\"},{\"name\":\"泉州\",\"cityId\":\"134\"},{\"name\":\"衢州\",\"cityId\":\"243\"},{\"name\":\"日照\",\"cityId\":\"173\"},{\"name\":\"瑞昌\",\"cityId\":\"1348\"},{\"name\":\"上虞\",\"cityId\":\"1467\"},{\"name\":\"三明\",\"cityId\":\"254\"},{\"name\":\"三亚\",\"cityId\":\"121\"},{\"name\":\"沙河\",\"cityId\":\"1667\"},{\"name\":\"上海\",\"cityId\":\"289\"},{\"name\":\"商丘\",\"cityId\":\"154\"},{\"name\":\"上饶\",\"cityId\":\"364\"},{\"name\":\"汕头\",\"cityId\":\"303\"},{\"name\":\"绍兴\",\"cityId\":\"293\"},{\"name\":\"邵阳\",\"cityId\":\"273\"},{\"name\":\"沈阳\",\"cityId\":\"58\"},{\"name\":\"深圳\",\"cityId\":\"340\"},{\"name\":\"石家庄\",\"cityId\":\"150\"},{\"name\":\"十堰\",\"cityId\":\"216\"},{\"name\":\"寿光\",\"cityId\":\"1412\"},{\"name\":\"双阳区\",\"cityId\":\"490\"},{\"name\":\"舒城\",\"cityId\":\"2598\"},{\"name\":\"朔州\",\"cityId\":\"237\"},{\"name\":\"泗洪\",\"cityId\":\"1326\"},{\"name\":\"四平\",\"cityId\":\"56\"},{\"name\":\"泗阳\",\"cityId\":\"1742\"},{\"name\":\"睢宁\",\"cityId\":\"1743\"},{\"name\":\"宿迁\",\"cityId\":\"277\"},{\"name\":\"苏州\",\"cityId\":\"224\"},{\"name\":\"宿州\",\"cityId\":\"370\"},{\"name\":\"泰安\",\"cityId\":\"325\"},{\"name\":\"泰兴\",\"cityId\":\"2894\"},{\"name\":\"太原\",\"cityId\":\"176\"},{\"name\":\"台州\",\"cityId\":\"244\"},{\"name\":\"泰州\",\"cityId\":\"276\"},{\"name\":\"郯城\",\"cityId\":\"1935\"},{\"name\":\"唐山\",\"cityId\":\"265\"},{\"name\":\"天津\",\"cityId\":\"332\"},{\"name\":\"铜川\",\"cityId\":\"232\"},{\"name\":\"通化\",\"cityId\":\"165\"},{\"name\":\"通辽\",\"cityId\":\"64\"},{\"name\":\"铜陵\",\"cityId\":\"337\"},{\"name\":\"铜仁地区\",\"cityId\":\"205\"},{\"name\":\"潍坊\",\"cityId\":\"287\"},{\"name\":\"威海\",\"cityId\":\"175\"},{\"name\":\"渭南\",\"cityId\":\"170\"},{\"name\":\"微山\",\"cityId\":\"2879\"},{\"name\":\"尉氏\",\"cityId\":\"2393\"},{\"name\":\"威\",\"cityId\":\"1665\"},{\"name\":\"汶上\",\"cityId\":\"1819\"},{\"name\":\"温州\",\"cityId\":\"178\"},{\"name\":\"武安\",\"cityId\":\"1246\"},{\"name\":\"乌海\",\"cityId\":\"123\"},{\"name\":\"武汉\",\"cityId\":\"218\"},{\"name\":\"芜湖\",\"cityId\":\"129\"},{\"name\":\"乌兰察布\",\"cityId\":\"168\"},{\"name\":\"乌鲁木齐\",\"cityId\":\"92\"},{\"name\":\"无为\",\"cityId\":\"1951\"},{\"name\":\"无锡\",\"cityId\":\"317\"},{\"name\":\"厦门\",\"cityId\":\"194\"},{\"name\":\"西安\",\"cityId\":\"233\"},{\"name\":\"湘潭\",\"cityId\":\"313\"},{\"name\":\"襄阳\",\"cityId\":\"156\"},{\"name\":\"仙桃\",\"cityId\":\"1713\"},{\"name\":\"咸阳\",\"cityId\":\"323\"},{\"name\":\"兴化\",\"cityId\":\"1324\"},{\"name\":\"邢台\",\"cityId\":\"266\"},{\"name\":\"星子\",\"cityId\":\"1762\"},{\"name\":\"新民\",\"cityId\":\"519\"},{\"name\":\"新泰\",\"cityId\":\"2455\"},{\"name\":\"新乡\",\"cityId\":\"152\"},{\"name\":\"信阳\",\"cityId\":\"214\"},{\"name\":\"新沂\",\"cityId\":\"2421\"},{\"name\":\"新余\",\"cityId\":\"164\"},{\"name\":\"新郑\",\"cityId\":\"2652\"},{\"name\":\"忻州\",\"cityId\":\"367\"},{\"name\":\"雄\",\"cityId\":\"1650\"},{\"name\":\"修水\",\"cityId\":\"2875\"},{\"name\":\"宣城\",\"cityId\":\"190\"},{\"name\":\"许昌\",\"cityId\":\"155\"},{\"name\":\"盱眙\",\"cityId\":\"1756\"},{\"name\":\"徐州\",\"cityId\":\"316\"},{\"name\":\"延安\",\"cityId\":\"284\"},{\"name\":\"延边朝鲜族自治州\",\"cityId\":\"54\"},{\"name\":\"盐城\",\"cityId\":\"223\"},{\"name\":\"扬州\",\"cityId\":\"346\"},{\"name\":\"延寿\",\"cityId\":\"452\"},{\"name\":\"烟台\",\"cityId\":\"326\"},{\"name\":\"宜昌\",\"cityId\":\"270\"},{\"name\":\"银川\",\"cityId\":\"360\"},{\"name\":\"营口\",\"cityId\":\"281\"},{\"name\":\"鹰潭\",\"cityId\":\"279\"},{\"name\":\"伊通满族自治\",\"cityId\":\"491\"},{\"name\":\"仪征\",\"cityId\":\"2424\"},{\"name\":\"尤溪\",\"cityId\":\"1977\"},{\"name\":\"岳阳\",\"cityId\":\"220\"},{\"name\":\"榆林\",\"cityId\":\"231\"},{\"name\":\"运城\",\"cityId\":\"328\"},{\"name\":\"榆树\",\"cityId\":\"469\"},{\"name\":\"玉田\",\"cityId\":\"2564\"},{\"name\":\"余姚\",\"cityId\":\"2215\"},{\"name\":\"枣强\",\"cityId\":\"1241\"},{\"name\":\"枣庄\",\"cityId\":\"172\"},{\"name\":\"张家口\",\"cityId\":\"264\"},{\"name\":\"樟树\",\"cityId\":\"2129\"},{\"name\":\"张掖\",\"cityId\":\"117\"},{\"name\":\"漳州\",\"cityId\":\"255\"},{\"name\":\"湛江\",\"cityId\":\"198\"},{\"name\":\"肇东\",\"cityId\":\"1020\"},{\"name\":\"洮南\",\"cityId\":\"467\"},{\"name\":\"肇庆\",\"cityId\":\"338\"},{\"name\":\"郑州\",\"cityId\":\"268\"},{\"name\":\"镇江\",\"cityId\":\"160\"},{\"name\":\"中牟\",\"cityId\":\"1680\"},{\"name\":\"中山\",\"cityId\":\"187\"},{\"name\":\"舟山\",\"cityId\":\"245\"},{\"name\":\"珠海\",\"cityId\":\"140\"},{\"name\":\"驻马店\",\"cityId\":\"269\"},{\"name\":\"株洲\",\"cityId\":\"222\"},{\"name\":\"淄博\",\"cityId\":\"354\"},{\"name\":\"自贡\",\"cityId\":\"78\"},{\"name\":\"资阳\",\"cityId\":\"242\"},{\"name\":\"邹城\",\"cityId\":\"1401\"}]");

	/**
	 * 获取新的商铺信息
	 */
	public void getNewShopInfo() {
		while (true) {
			Jedis redis = null;
			String shopId = null;
			try {
				redis = RedisServer.bizRedisPool.getResource();
				// 获取队列中的商家值
				shopId = redis.lpop("baidu.takeaway.allcrazy");
				if (!StringUtils.isEmpty(shopId)) {
					System.out.println("执行个数：" + count++);
					if (!redis.hexists("baidu.takeaway.shop.back", shopId)) {

						// 获取商铺信息
						String shopStr = getShopStr(shopId);
						if (!StringUtils.isEmpty(shopStr)) {
							String[] shopInfo = shopStr.split("#_#");
							if (shopInfo != null && shopInfo.length > 0) {
								redis.lpush("baidu.takeaway.shop.queue",
										shopStr);
							}
						}

					}
				}
			} catch (Exception e) {
				redis.rpush("baidu.takeaway.allcrazy", shopId);
				e.printStackTrace();

			} finally {
				if (redis != null) {
					RedisServer.bizRedisPool.returnResource(redis);
				}
			}
		}

	}

	/**
	 * 获取商铺信息
	 * 
	 * @param shopId
	 * @return
	 */
	public String getShopStr(String shopId) {
		// shop_id+"#_#"+shop_name+"#_#"+category+"#_#"+average_score+"#_#"+saled_month+"#_#"+shop_lat+"#_#"+shop_lng;
		String shopStr = null;
		if (shopId != null) {
			if (!StringUtils.isEmpty(shopId)) {
				HttpGet httpGet = null;
				try {
					httpGet = new HttpGet(SHOPINFOURL + shopId);
					String result = HttpThreadPool.executeClient(httpGet,
							false, "utf-8", null);
					if (result.contains("该商家无相应菜单")) {
						return null;
					}

					String[] shop_name = result.split("\"shop_name\":");
					if (shop_name != null && shop_name.length > 0) {
						String[] shopName = shop_name[1].split("\"");
						if (shopName != null && shopName.length > 2) {
							if (!StringUtils.isEmpty(shopName[1])) {
								shopStr = shopId + "#_#"
										+ decodeUnicode(shopName[1]) + "#_#"
										+ "餐饮" + "#_#";
							}
						}
					}
					String[] average_score = result.split("\"average_score\":");
					if (average_score != null && average_score.length > 0) {
						String[] averageScore = average_score[1].split(",\"");
						if (averageScore != null && averageScore.length > 2) {
							if (!StringUtils.isEmpty(averageScore[0])) {
								shopStr = shopStr + averageScore[0] + "#_#";
							}
						}
					}
					String[] saled_month = result.split("\"saled_month\":");
					if (saled_month != null && saled_month.length > 0) {
						String[] saledMonth = saled_month[1].split(",\"");
						if (saledMonth != null && saledMonth.length > 2) {
							if (!StringUtils.isEmpty(saledMonth[0])) {
								shopStr = shopStr + saledMonth[0] + "#_#";
							}
						}
					}

					String[] latLng = result.toString().split("shop_lat\":\"");
					if (latLng != null && latLng.length > 2) {
						String[] lats = latLng[1].split("\"");
						if (lats != null && lats.length > 2) {
							if (!StringUtils.isEmpty(lats[0])) {
								shopStr = shopStr + lats[0] + "#_#";
							} else {
								shopStr = shopStr + null + "#_#";
							}
						}
						String[] lng = result.toString().split("shop_lng\":\"");
						String[] lngs = lng[1].split("\"");
						if (lngs != null && lngs.length > 2) {
							if (!StringUtils.isEmpty(lngs[0])) {
								shopStr = shopStr + lngs[0];
							} else {
								shopStr = shopStr + null;
							}
						}
					} else {
						shopStr = shopStr + "#_#" + null + "#_#" + null;
					}
					return shopStr;

				} catch (Exception e) {
					return null;
				}
			}

		}

		return shopStr;
	}

	public void putShopToQueue() {

		Jedis redis = null;
		try {
			redis = RedisServer.bizRedisPool.getResource();
			// 获取队列中的商家值
			Map<String, String> map = redis.hgetAll("baidu.takeaway.shop");

			if (map != null && map.size() > 0) {
				Set<String> bSet = map.keySet();
				for (String shopId : bSet) {
					String shopInfoStr = map.get(shopId);
					if (!StringUtils.isEmpty(shopInfoStr)) {
						if (redis.hexists("baidu.takeaway.shop.back", shopId)) {
							redis.hdel("baidu.takeaway.shop", shopId);
						} else {
							redis.lpush("baidu.takeaway.shop.queue",
									shopInfoStr);
						}
					}
				}
			}
		} catch (Exception e) {

		} finally {
			if (redis != null) {
				RedisServer.bizRedisPool.returnResource(redis);
			}
		}

	}

	/**
	 * 获取城市名称
	 * 
	 * @param cityId
	 * @return
	 */
	public String cityName(String cityId) {
		for (int i = 0; i < citys.size(); i++) {
			JSONObject jo = (JSONObject) citys.get(i);
			String city_id = jo.has("cityId") ? jo.getString("cityId") : "";
			if (city_id.equals(cityId)) {
				return jo.has("name") ? jo.getString("name") : null;
			}
		}
		return null;
	}

	public void countShopInfos() {

		Jedis redis = null;
		try {
			redis = RedisServer.bizRedisPool.getResource();
			Map<String, String> map = redis.hgetAll("baidu.takeaway.shop.back");
			if (map != null && map.size() > 0) {
				Set<String> bSet = map.keySet();
				for (String shopId : bSet) {
					String shopStr = map.get(shopId);
					redis.hset("baidu.takeaway.shop", shopId, shopStr);
				}
			}
		} catch (Exception e) {
		} finally {
			if (redis != null) {
				RedisServer.bizRedisPool.returnResource(redis);
			}
		}
	}

	public void getMaxMin() {
		Jedis redis = null;
		try {
			redis = RedisServer.bizRedisPool.getResource();
			Map<String, String> map = redis.hgetAll("baidu.takeaway.shop");
			List<BigDecimal> lnglist = new ArrayList<BigDecimal>();
			List<BigDecimal> latlist = new ArrayList<BigDecimal>();
			if (map != null && map.size() > 0) {
				Set<String> bSet = map.keySet();
				for (String shopId : bSet) {
					String shopStr = map.get(shopId);
					if (!StringUtils.isEmpty(shopStr)) {
						String[] str = shopStr.split("#_#");
						lnglist.add(new BigDecimal(str[6]));
						latlist.add(new BigDecimal(str[5]));
					}
				}
			}

			System.out.println("lat：max:" + Collections.max(latlist) + "min:"
					+ Collections.min(latlist) + "lng:max:"
					+ Collections.max(lnglist) + "min:"
					+ Collections.min(lnglist));
		} catch (Exception e) {
		} finally {
			if (redis != null) {
				RedisServer.bizRedisPool.returnResource(redis);
			}
		}
	}

	public void createSeq() {
		Jedis redis = null;
		try {
			redis = RedisServer.bizRedisPool.getResource();
			for (int i = 0; i < 1000; i++) {
				redis.lpush("baidu.waimai.forder.seq", String.valueOf(i));
			}
		} catch (Exception e) {
		} finally {
			if (redis != null) {
				RedisServer.bizRedisPool.returnResource(redis);
			}
		}
	}

	@Override
	public void run() {
		while (true) {
			// 处理商铺信息
			handleShopInfos();
		}
	}

	// 处理商铺信息
	public void handleShopInfos() {
		Jedis redis = null;
		String shopInfoStr = null;
		try {
			redis = RedisServer.bizRedisPool.getResource();
			// 获取队列中的商家值
			  shopInfoStr = redis.rpop("baidu.takeaway.shop.queue");
			  if(StringUtils.isEmpty(shopInfoStr)){ 
				  return ; 
				  } 
			  String[] shopInfo = shopInfoStr.split("#_#"); 
			  if (shopInfo != null &&shopInfo.length > 0) { 
				  // 0:shop_id 1：shop_name 2:category 3：average_score // 4:saled_month 5：shop_lat 6: shop_lng 
				  // 获取商家信息: 
			  getShopInfo(shopInfo[0], shopInfo[1], shopInfo[3],shopInfo[4], shopInfo[5], shopInfo[6], redis);
			  redis.hset("baidu.takeaway.shop.back", shopInfo[0], shopInfoStr);
			  redis.hdel("baidu.takeaway.shop", shopInfo[0]); }
		} catch (Exception e) {
			if (!StringUtils.isEmpty(shopInfoStr)) {
				redis.rpush("baidu.takeaway.shop.queue", shopInfoStr);
			}
		} finally {
			if (redis != null) {
				RedisServer.bizRedisPool.returnResource(redis);
			}
		}
	}

	public void getShopInfo(String shopId, String shopName,
			String averageScore, String saledMonth, String lat, String lng,
			Jedis redis) throws Exception {
		JSONObject shopJo = new JSONObject();// 店鋪实体
		shopJo.put("bd_id", shopId);// 商户ID
		shopJo.put("name", shopName);// 商户名称
		shopJo.put("address", null);// 商户地址
		shopJo.put("longitude", lng);
		shopJo.put("latitude", lat);
		shopJo.put("rating", null);// 商户评分
		shopJo.put("recent_order_num", saledMonth);// 商户月销量
		shopJo.put("float_minimum_order_amount", null);// 商户起送价
		shopJo.put("opening_hours", null);// 商户营业时间
		shopJo.put("order_lead_time", null);// 商户平均送达时间
		// 获取店铺和产品信息
		shopJo = getshopInfo(shopJo);
		if (shopJo != null) {
			// 写入文件
			writeDataToFile(shopJo, redis);
		}

	}

	/**
	 * 写文件
	 * 
	 * @param shopJo
	 */
	public void writeDataToFile(JSONObject shopJo, Jedis redis)
			throws Exception {

		File file = null;
		FileWriter writer = null;
		try {
			String forderSeq = redis.lpop(BAIDU_WAIMAI_FORDER_SEQ);
			redis.rpush(BAIDU_WAIMAI_FORDER_SEQ, forderSeq);
			file = new File(FILAPATH + forderSeq);
			if (!file.exists()) {
				file.mkdirs();
			}
			String shopId = shopJo.has("bd_id") ? shopJo.getString("bd_id")
					: null;
			file = new File(FILAPATH + forderSeq + "/" + shopId + ".txt");
			writer = new FileWriter(file);
			writer.write(JSONArray.fromObject(shopJo).toString());
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	public static String decodeUnicode(String dataStr) {
		int start = 0;
		int end = 0;
		final StringBuffer buffer = new StringBuffer();
		while (start > -1) {
			end = dataStr.indexOf("\\u", start + 2);
			String charStr = "";
			if (end == -1) {
				charStr = dataStr.substring(start + 2, dataStr.length());
			} else {
				charStr = dataStr.substring(start + 2, end);
			}
			char letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
			buffer.append(new Character(letter).toString());
			start = end;
		}
		return buffer.toString();
	}

	// 获取店铺和产品信息
	public JSONObject getshopInfo(JSONObject shopJo) throws Exception {
		if (shopJo != null) {
			String shopId = shopJo.has("bd_id") ? shopJo.getString("bd_id")
					: null;
			if (!StringUtils.isEmpty(shopId)) {
				HttpGet httpGet = null;
				try {
					httpGet = new HttpGet(SHOPINFOURL + shopId);
					String result = HttpThreadPool.executeClient(httpGet,
							false, "utf-8", null);
					if (!StringUtils.isEmpty(result)) {
						Document desHtml = Jsoup.parse(result);
						String[] cityInfo = result.split("\"city_id\":");
						if (cityInfo != null && cityInfo.length > 0) {
							String[] tcity = cityInfo[1].split("\"");
							if (tcity != null && tcity.length > 2) {
								String cityName = cityName(tcity[1]);
								if (!StringUtils.isEmpty(cityName)) {
									shopJo.put("cityName", cityName);
									System.out.println(cityName);
								}
							}
						}
						// 获取营业时间
						try {
							Element stime = desHtml.getElementsByClass(
									"bussinessStatus").get(0);
							if (stime != null) {
								Document timeSpan = Jsoup.parse(stime
										.toString());
								String time = timeSpan.getElementsByTag("span")
										.get(0).text();
								shopJo.put("opening_hours", time);
								System.out.println("营业时间:" + time);
							}
						} catch (Exception e) {
							System.out.println("获取营业时间错误");
						}

						// 获取商家地址
						try {
							Elements sadress = desHtml
									.getElementsByClass("b-info");
							if (sadress != null && sadress.size() > 0) {
								Document adressdl = Jsoup.parse(sadress
										.toString());
								Element dl = adressdl.getElementsByTag("dl")
										.last();
								String adressdldd = dl.getElementsByTag("dd")
										.get(0).text();
								shopJo.put("address", adressdldd);
								System.out.println("商家地址:" + adressdldd);
							}
						} catch (Exception e) {
							System.out.println("获取商家地址错误");
						}

						// 获取商家平均送达时间
						try {
							Elements order_lead_time = desHtml
									.getElementsByClass("b-totime");
							if (order_lead_time != null
									&& order_lead_time.size() > 0) {
								String btime = Jsoup
										.parse(order_lead_time.get(0)
												.toString())
										.getElementsByClass("b-num").text();
								shopJo.put("order_lead_time", btime);
								System.out.println("商家平均送达时间:" + btime);
							}
						} catch (Exception e) {
							System.out.println("获取商家平均送达时间错误");
						}
						// 获取商户起送价
						try {
							Elements float_minimum_order_amount = desHtml
									.getElementsByClass("b-price");
							if (float_minimum_order_amount != null
									&& float_minimum_order_amount.size() > 0) {
								String btime = Jsoup
										.parse(float_minimum_order_amount
												.get(0).toString())
										.getElementsByClass("b-num").text();
								shopJo.put("float_minimum_order_amount", btime);
								System.out.println("商家商户起送价:" + btime);
							}
						} catch (Exception e) {
							System.out.println("获取商户起送价错误");
						}

						// 获取商户评分
						try {

							Elements rating = desHtml
									.getElementsByClass("b-info");
							if (rating != null && rating.size() > 0) {
								Document adressdl = Jsoup.parse(rating
										.toString());
								Element dl = adressdl.getElementsByTag("dl")
										.get(0);
								String adressdldd = dl
										.getElementsByClass("rate-con").get(0)
										.text();
								shopJo.put("rating",
										adressdldd.replace("分", ""));
								System.out.println("商户评分:"
										+ adressdldd.replace("分", ""));
							}
						} catch (Exception e) {
							System.out.println("获取商户评分错误");
						}

						// 获取产品列表信息
						JSONArray joGoodsArray = new JSONArray();
						try {
							Elements goodsList = desHtml
									.getElementsByClass("list-item");
							if (goodsList != null && goodsList.size() > 0) {
								for (int i = 0; i < goodsList.size(); i++) {
									Element goodsMenu = goodsList.get(i);
									JSONObject goods = new JSONObject();
									String item_id = goodsMenu.attr("data-sid")
											.substring(5);
									goods.put("id", item_id);// 菜品名称ID
									Document listHtml = Jsoup.parse(goodsMenu
											.toString());
									String goodsName = listHtml
											.getElementsByTag("h3").attr(
													"data-title");
									goods.put("name", goodsName);// 菜品名称
									String goodsPrice = listHtml
											.getElementsByClass("m-price")
											.text().replace("¥", "");
									if (StringUtils.isEmpty(goodsPrice)) {
										goodsPrice = listHtml
												.getElementsByClass("m-break")
												.text().replace("¥", "");
										goodsPrice = goodsPrice.split("餐厅")[0];
									}
									goodsPrice = goodsPrice.split("起")[0];
									goods.put("price",
											goodsPrice.split("   ")[0]);// 菜品价格
									String goodsSale = listHtml
											.getElementsByClass("sales-count")
											.last().text().replace("月售", "")
											.replace("份", "");
									goods.put("month_sales", goodsSale);
									System.out.println(goodsName + ":"
											+ goodsPrice + ":" + goodsSale);
									joGoodsArray.add(goods);
								}
							}

						} catch (Exception e) {
							log.loger.info("获取列表信息错误" + e.getMessage());
						}
						shopJo.put("goodsList", joGoodsArray);
					}
				} catch (Exception e) {
					return shopJo;
				}
			}

		}

		return shopJo;
	}

}
