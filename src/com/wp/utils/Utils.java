/**
 * Licensed to the /Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wp.utils;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.swing.plaf.synth.SynthSpinnerUI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.util.Cookie;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;

/**
 * common utilities
 * 
 * @author adyliu (imxylz@gmail.com)
 * @since 1.0
 */ 
public class Utils {
	/** 
	 * loading Properties from files
	 * 
	 * @param filename
	 *            file path
	 * @return properties
	 * @throws RuntimeException
	 *             while file not exist or loading fail
	 */
	public static Map<String, Object> cookieToMap(String value) {
		Map<String, Object>  map = new HashMap<>();
		String[] sss = value.split(";");
		for (int i = 0; i < sss.length; i++) {
			map.put(sss[i].split("=")[0].trim(), sss[i].split("=")[1].trim());
		}
		return map;
		}
	
	public static String StringToNum(String value) {
		String regEx="[^0-9]";   
		Pattern p = Pattern.compile(regEx);   
		Matcher m = p.matcher(value);   
			return m.replaceAll("").trim();
		}
	
	
	
	
	
	
	
	
	
	public static List<String> getStringAttr(String str,String attr){
		str = str.substring(str.indexOf(attr));
		List<String> list = new ArrayList<>();
		boolean isok = true;
		while(isok){
			
		
			if(str.indexOf(attr)<0){
				isok = false;
			}else{
				String ls="";
				ls = str.substring(str.indexOf(attr)+attr.length()+2,str.indexOf("\"", attr.length()+3));
				if(str.indexOf(attr,ls.length()+attr.length()+2)>0){
					str =str.substring(str.indexOf(attr,ls.length()+attr.length()+2));
				}else{
					
				isok = false;
				}
				
				
				list.add(ls);
			}
			
		}
			return list;
		
	}
public static void sethtmlCookies(WebClient webClient,String domain, String cookies) {
		
		String ll[] = cookies.split(";"); 
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < ll.length; i++) {
			if(ll[i].split("=").length==2){
				map.put(ll[i].split("=")[0], ll[i].split("=")[1]);
			}			
		}
		if (cookies != null && map.size() > 0) {
			webClient.getCookieManager().setCookiesEnabled(true);// enable
																	// cookies
			for (Entry<String, String> c : map.entrySet()) {
				Cookie cookie = new Cookie(domain, c.getKey(), c.getValue());
				webClient.getCookieManager().addCookie(cookie);
			}
		}
	} 
	//list去重
	public static void removeDuplicateWithOrder(List list) {  
	     Set set = new HashSet();  
	      List newList = new ArrayList();  
	   for (Iterator iter = list.iterator(); iter.hasNext();) {  
	          Object element = iter.next();  
	          if (set.add(element))  
	             newList.add(element);  
	       }   
	      list.clear();  
	      list.addAll(newList);   
	}  
	//获取指定html属性值
	public static List<String> match(String source, String element, String attr) {  
	    List<String> result = new ArrayList<String>();  
	    String reg = "<" + element + "[^<>]*?\\s" + attr + "=['\"]?(.*?)['\"]?(\\s.*?)?>";  
	    Matcher m = Pattern.compile(reg).matcher(source);  
	    while (m.find()) {  
	        String r = m.group(1);  
	        result.add(r);  
	    }  
	    return result;  
	}  
	
	
	private Log log = Log.getLoger();
	
	public static Properties loadProps(String filename) {
		Properties props = new Properties();
		FileInputStream fis = null;

		try {
			fis = new FileInputStream(filename);
			props.load(fis);
			return props;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			Closer.closeQuietly(fis);
		}

	}

	/**
	 * Get a property of type java.util.Properties or return the default if no
	 * such property is defined
	 */
	public static Properties getProps(Properties props, String name,
			Properties defaultProperties) {
		final String propString = props.getProperty(name);
		if (propString == null)
			return defaultProperties;
		String[] propValues = propString.split(",");
		if (propValues.length < 1) {
			throw new IllegalArgumentException(
					"Illegal format of specifying properties '" + propString
							+ "'");
		}
		Properties properties = new Properties();
		for (int i = 0; i < propValues.length; i++) {
			String[] prop = propValues[i].split("=");
			if (prop.length != 2)
				throw new IllegalArgumentException(
						"Illegal format of specifying properties '"
								+ propValues[i] + "'");
			properties.put(prop[0], prop[1]);
		}
		return properties;
	}

	/**
	 * Get a string property, or, if no such property is defined, return the
	 * given default value
	 * 
	 * @param props
	 *            the properties
	 * @param name
	 *            the key in the properties
	 * @param defaultValue
	 *            the default value if the key not exists
	 * @return value in the props or defaultValue while name not exist
	 */
	public static String getString(Properties props, String name,
			String defaultValue) {
		return props.containsKey(name) ? props.getProperty(name) : defaultValue;
	}

	public static String getString(Properties props, String name) {
		if (props.containsKey(name)) {
			return props.getProperty(name);
		}
		throw new IllegalArgumentException("Missing required property '" + name
				+ "'");
	}

	public static int getInt(Properties props, String name) {
		if (props.containsKey(name)) {
			return getInt(props, name, -1);
		}
		throw new IllegalArgumentException("Missing required property '" + name
				+ "'");
	}

	public static int getInt(Properties props, String name, int defaultValue) {
		return getIntInRange(props, name, defaultValue, Integer.MIN_VALUE,
				Integer.MAX_VALUE);
	}

	public static int getIntInRange(Properties props, String name,
			int defaultValue, int min, int max) {
		int v = defaultValue;
		if (props.containsKey(name)) {
			v = Integer.valueOf(props.getProperty(name));
		}
		if (v >= min && v <= max) {
			return v;
		}
		throw new IllegalArgumentException(name + " has value " + v
				+ " which is not in the range");
	}

	public static boolean getBoolean(Properties props, String name,
			boolean defaultValue) {
		if (!props.containsKey(name))
			return defaultValue;
		return "true".equalsIgnoreCase(props.getProperty(name));
	}

	private static Map<String, Integer> getCSVMap(String value,
			String exceptionMsg, String successMsg) {
		Map<String, Integer> map = new LinkedHashMap<String, Integer>();
		if (value == null || value.trim().length() < 3)
			return map;
		for (String one : value.trim().split(",")) {
			String[] kv = one.split(":");
			// FIXME: force positive number
			map.put(kv[0].trim(), Integer.valueOf(kv[1].trim()));
		}
		return map;
	}

	public static Map<String, Integer> getTopicRentionHours(
			String retentionHours) {
		String exceptionMsg = "Malformed token for topic.log.retention.hours in server.properties: ";
		String successMsg = "The retention hour for ";
		return getCSVMap(retentionHours, exceptionMsg, successMsg);
	}

	public static Map<String, Integer> getTopicFlushIntervals(
			String allIntervals) {
		String exceptionMsg = "Malformed token for topic.flush.Intervals.ms in server.properties: ";
		String successMsg = "The flush interval for ";
		return getCSVMap(allIntervals, exceptionMsg, successMsg);
	}


	public static Map<String, Integer> getTopicPartitions(String allPartitions) {
		String exceptionMsg = "Malformed token for topic.partition.counts in server.properties: ";
		String successMsg = "The number of partitions for topic  ";
		return getCSVMap(allPartitions, exceptionMsg, successMsg);
	}

	public static Map<String, Integer> getConsumerTopicMap(
			String consumerTopicString) {
		String exceptionMsg = "Malformed token for embeddedconsumer.topics in consumer.properties: ";
		String successMsg = "The number of consumer thread for topic  ";
		return getCSVMap(consumerTopicString, exceptionMsg, successMsg);
	}

	/**
	 * read data from channel to buffer
	 * 
	 * @param channel
	 *            readable channel
	 * @param buffer
	 *            bytebuffer
	 * @return read size
	 * @throws IOException
	 */
	public static int read(ReadableByteChannel channel, ByteBuffer buffer)
			throws IOException {
		int count = channel.read(buffer);
		if (count == -1)
			throw new EOFException(
					"Received -1 when reading from channel, socket has likely been closed.");
		return count;
	}

	/**
	 * Write a size prefixed string where the size is stored as a 2 byte short
	 * 
	 * @param buffer
	 *            The buffer to write to
	 * @param s
	 *            The string to write
	 */
	public static void writeShortString(ByteBuffer buffer, String s) {
		if (s == null) {
			buffer.putShort((short) -1);
		} else if (s.length() > Short.MAX_VALUE) {
			throw new IllegalArgumentException(
					"String exceeds the maximum size of " + Short.MAX_VALUE
							+ ".");
		} else {
			byte[] data = getBytes(s); // topic support non-ascii character
			buffer.putShort((short) data.length);
			buffer.put(data);
		}
	}

	public static String fromBytes(byte[] b) {
		return fromBytes(b, "UTF-8");
	}

	public static String fromBytes(byte[] b, String encoding) {
		if (b == null)
			return null;
		try {
			return new String(b, encoding);
		} catch (UnsupportedEncodingException e) {
			return new String(b);
		}
	}

	public static byte[] getBytes(String s) {
		return getBytes(s, "UTF-8");
	}

	public static byte[] getBytes(String s, String encoding) {
		if (s == null)
			return null;
		try {
			return s.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			return s.getBytes();
		}
	}

	/**
	 * Read an unsigned integer from the current position in the buffer,
	 * incrementing the position by 4 bytes
	 * 
	 * @param buffer
	 *            The buffer to read from
	 * @return The integer read, as a long to avoid signedness
	 */
	public static long getUnsignedInt(ByteBuffer buffer) {
		return buffer.getInt() & 0xffffffffL;
	}

	/**
	 * Read an unsigned integer from the given position without modifying the
	 * buffers position
	 * 
	 * @param buffer
	 *            The buffer to read from
	 * @param index
	 *            the index from which to read the integer
	 * @return The integer read, as a long to avoid signedness
	 */
	public static long getUnsignedInt(ByteBuffer buffer, int index) {
		return buffer.getInt(index) & 0xffffffffL;
	}

	/**
	 * Write the given long value as a 4 byte unsigned integer. Overflow is
	 * ignored.
	 * 
	 * @param buffer
	 *            The buffer to write to
	 * @param value
	 *            The value to write
	 */
	public static void putUnsignedInt(ByteBuffer buffer, long value) {
		buffer.putInt((int) (value & 0xffffffffL));
	}

	/**
	 * Write the given long value as a 4 byte unsigned integer. Overflow is
	 * ignored.
	 * 
	 * @param buffer
	 *            The buffer to write to
	 * @param index
	 *            The position in the buffer at which to begin writing
	 * @param value
	 *            The value to write
	 */
	public static void putUnsignedInt(ByteBuffer buffer, int index, long value) {
		buffer.putInt(index, (int) (value & 0xffffffffL));
	}

	/**
	 * Compute the CRC32 of the byte array
	 * 
	 * @param bytes
	 *            The array to compute the checksum for
	 * @return The CRC32
	 */
	public static long crc32(byte[] bytes) {
		return crc32(bytes, 0, bytes.length);
	}

	/**
	 * Compute the CRC32 of the segment of the byte array given by the
	 * specificed size and offset
	 * 
	 * @param bytes
	 *            The bytes to checksum
	 * @param offset
	 *            the offset at which to begin checksumming
	 * @param size
	 *            the number of bytes to checksum
	 * @return The CRC32
	 */
	public static long crc32(byte[] bytes, int offset, int size) {
		CRC32 crc = new CRC32();
		crc.update(bytes, offset, size);
		return crc.getValue();
	}

	/**
	 * Create a new thread
	 * 
	 * @param name
	 *            The name of the thread
	 * @param runnable
	 *            The work for the thread to do
	 * @param daemon
	 *            Should the thread block JVM shutdown?
	 * @return The unstarted thread
	 */
	public static Thread newThread(String name, Runnable runnable,
			boolean daemon) {
		Thread thread = new Thread(runnable, name);
		thread.setDaemon(daemon);
		return thread;
	}

	/**
	 * read bytes with a short sign prefix(mark the size of bytes)
	 * 
	 * @param buffer
	 *            data buffer
	 * @return string result(encoding with UTF-8)
	 * @see #writeShortString(ByteBuffer, String)
	 */
	public static String readShortString(ByteBuffer buffer) {
		short size = buffer.getShort();
		if (size < 0) {
			return null;
		}
		byte[] bytes = new byte[size];
		buffer.get(bytes);
		return fromBytes(bytes);
	}
 
	/**
	 * Register the given mbean with the platform mbean server, unregistering
	 * any mbean that was there before. Note, this method will not throw an
	 * exception if the registration fails (since there is nothing you can do
	 * and it isn't fatal), instead it just returns false indicating the
	 * registration failed.
	 * 
	 * @param mbean
	 *            The object to register as an mbean
	 * @param name
	 *            The name to register this mbean with
	 * @return true if the registration succeeded
	 */
	static boolean registerMBean(Object mbean, String name) {
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			synchronized (mbs) {
				ObjectName objName = new ObjectName(name);
				if (mbs.isRegistered(objName)) {
					mbs.unregisterMBean(objName);
				}
				mbs.registerMBean(mbean, objName);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
 

	/**
	 * Unregister the mbean with the given name, if there is one registered
	 * 
	 * @param name
	 *            The mbean name to unregister
	 * @see #registerMBean(Object, String)
	 */
	private static void unregisterMBean(String name) {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			synchronized (mbs) {
				ObjectName objName = new ObjectName(name);
				if (mbs.isRegistered(objName)) {
					mbs.unregisterMBean(objName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * open a readable or writeable FileChannel
	 * 
	 * @param file
	 *            file object
	 * @param mutable
	 *            writeable
	 * @return open the FileChannel
	 */
	@SuppressWarnings("resource")
	public static FileChannel openChannel(File file, boolean mutable)
			throws IOException {
		if (mutable) {
			return new RandomAccessFile(file, "rw").getChannel();
		}
		return new FileInputStream(file).getChannel();
	}

	public static List<String> getCSVList(String csvList) {
		if (csvList == null || csvList.length() == 0)
			return Collections.emptyList();
		List<String> ret = new ArrayList<String>(Arrays.asList(csvList
				.split(",")));
		Iterator<String> iter = ret.iterator();
		while (iter.hasNext()) {
			final String next = iter.next();
			if (next == null || next.length() == 0) {
				iter.remove();
			}
		}
		return ret;
	}

	/**
	 * create an instance from the className
	 * 
	 * @param className
	 *            full class name
	 * @return an object or null if className is null
	 */
	@SuppressWarnings("unchecked")
	public static <E> E getObject(String className) {
		if (className == null) {
			return (E) null;
		}
		try {
			return (E) Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static String toString(ByteBuffer buffer, String encoding) {
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return fromBytes(bytes, encoding);
	}

	public static File getCanonicalFile(File f) {
		try {
			return f.getCanonicalFile();
		} catch (IOException e) {
			return f.getAbsoluteFile();
		}
	}

	public static ByteBuffer serializeArray(long[] numbers) {
		int size = 4 + 8 * numbers.length;
		ByteBuffer buffer = ByteBuffer.allocate(size);
		buffer.putInt(numbers.length);
		for (long num : numbers) {
			buffer.putLong(num);
		}
		buffer.rewind();
		return buffer;
	}

	public static ByteBuffer serializeArray(int[] numbers) {
		int size = 4 + 4 * numbers.length;
		ByteBuffer buffer = ByteBuffer.allocate(size);
		buffer.putInt(numbers.length);
		for (int num : numbers) {
			buffer.putInt(num);
		}
		buffer.rewind();
		return buffer;
	}

	public static int[] deserializeIntArray(ByteBuffer buffer) {
		int size = buffer.getInt();
		int[] nums = new int[size];
		for (int i = 0; i < size; i++) {
			nums[i] = buffer.getInt();
		}
		return nums;
	}

	public static long[] deserializeLongArray(ByteBuffer buffer) {
		int size = buffer.getInt();
		long[] nums = new long[size];
		for (int i = 0; i < size; i++) {
			nums[i] = buffer.getLong();
		}
		return nums;
	}

	private final static char hexDigits[] = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * digest message with MD5
	 * 
	 * @param source
	 *            message
	 * @return 32 bit MD5 value (lower case)
	 */
	public static String md5(byte[] source) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(source);
			byte tmp[] = md.digest();
			char str[] = new char[32];
			int k = 0;
			for (byte b : tmp) {
				str[k++] = hexDigits[b >>> 4 & 0xf];
				str[k++] = hexDigits[b & 0xf];
			}
			return new String(str);

		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static void deleteDirectory(File dir) {
		if (!dir.exists())
			return;
		if (dir.isDirectory()) {
			File[] subs = dir.listFiles();
			if (subs != null) {
				for (File f : subs) {
					deleteDirectory(f);
				}
			}
		}
		if (!dir.delete()) {
			throw new IllegalStateException("delete directory failed: " + dir);
		}
	}

	/**
	 * 目前只支持aix
	 * 
	 * @param filePath
	 * @return
	 */
	public static long getLineByFilePath(String filePath) {
		File file =new File(filePath);
		long line = 0;
		if(file.exists()){
			
			if (!"AIX".equals(System.getProperty("os.name"))
					&& !"Linux".equals(System.getProperty("os.name"))) {
				return line;
			}
			try {
				String[] cmd=new String[3];
				cmd[0]="sh";
				cmd[1]="-c";
				if(filePath.endsWith(".gz")){
				 cmd[2]=
						"zcat "+ filePath+"| wc -l "  + " | awk '{print $1}'" ;
				}else {
					cmd[2]="wc -l "+ filePath  + " | awk '{print $1}'";
				}
				Process process = Runtime.getRuntime().exec(cmd);
				BufferedReader inputBufferedReader = new BufferedReader(
						new InputStreamReader(process.getInputStream()));
				String lineStr = "";
				while ((lineStr = inputBufferedReader.readLine()) != null) {
					line = Long.parseLong(lineStr.trim());
				}
				process.waitFor();
			} catch (Exception e) {
				return line;
			}
		}
		
		return line;
	}

	/**
	 * get file size
	 * 
	 * @param filePathName
	 * @return
	 */
	public static long getFileSizeCpuLog(String filePathName) {
		File file = new File(filePathName);
		FileInputStream fis;
		long size = 0;
		try {
			fis = new FileInputStream(file);
			size = fis.available();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return size;
	}

	/**
	 * 转换字符数到位long�?	 * 
	 * @param stringtime
	 * @return
	 */
	public static long string2long(String stringtime) {
		long result = 0;
		if ("".equals(stringtime) || stringtime == null
				|| stringtime.length() < 13) {
			return 0;
		}
		if (stringtime.contains(".")) {
			stringtime = stringtime.replace(".", "");
		}
		stringtime = stringtime.substring(0, 13);
		result = Long.parseLong(stringtime);
		return result;
	}

	/**
	 * 获取数据的标�?基本算法：形式如 m.baidu.com g.qzone.qq.com tp4.sinaimg.cn pingma.qq.com
	 * 去除第一个和�?���?��
	 * 
	 * @param host
	 * @return
	 */
	public static String getHostTag(String host, String a18isSEVTY) {
		if (!"".equals(host) && host != null) {
			int index=host.indexOf(":");
			if(index>0){
				host=host.substring(0, index);
			}
			host = host.replace("www.", "");
			host = host.replace(".com", "");
			host = host.replace(".cn", "");
			host = host.replace(".net", "");
			host = host.replace(".org", "");
		}
		return host;
	}

	/**
	 * 获取数据的标�?基本算法：形式如 m.baidu.com g.qzone.qq.com tp4.sinaimg.cn pingma.qq.com
	 * 去除第一个和�?���?��
	 * 
	 * @param host
	 * @return
	 */
	public static String getHostTag(String host) {
		String tag = "";
		if (!"".equals(host) && host != null) {
			String[] hostarray = host.split("\\.");
			if (hostarray.length == 3) {
				tag = hostarray[1];
			}
			if (hostarray.length >= 4) {
				tag = hostarray[1] + "." + hostarray[2];
			}
		}

		return tag;
	}

	/**
	 * 转换long to date time yyMMdd
	 * 
	 * @param stringtime
	 * @return
	 */
	public static String longParseDate(long starttime) {
		DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(starttime);
		String yeardatatime = formatter.format(calendar.getTime());
		return yeardatatime;
	}

	/**
	 * 转换long to date time yyMMddhhmmss
	 * 
	 * @param stringtime
	 * @return
	 */
	public static String longParseDateTime(long starttime) {
		DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(starttime);
		String yeardatatime = formatter.format(calendar.getTime());
		if (!"".equals(yeardatatime) && yeardatatime != null
				&& yeardatatime.length() > 4) {
			return yeardatatime.substring(4, yeardatatime.length());
		}
		return "000000000000";
	}

	/**
	 * 转换long to date time yyMMddhhmmss
	 * 
	 * @param stringtime
	 * @return
	 */
	public static String getNowHourMin() {
		long nowLong=System.currentTimeMillis();
		DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(nowLong);
		String yeardatatime = formatter.format(calendar.getTime());
		if (!"".equals(yeardatatime) && yeardatatime != null) {
			return yeardatatime.substring(yeardatatime.length()-4, yeardatatime.length());
		}else{
			return null;
		}
	}

	/**
	 * 转换long to date time yyyyMMddHHmmss 24 hours
	 * 
	 * @param stringtime
	 * @return
	 */
	public static String getParseDateTime(long starttime) {
		DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(starttime);
		String yeardatatime = formatter.format(calendar.getTime());
		return yeardatatime;
	}
	public static String getParseDate(long starttime) {
		DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(starttime);
		String yeardatatime = formatter.format(calendar.getTime());
		return yeardatatime;
	}
 
	/**
	 * len is the length of the mdn or phonenum 19bit len<5
	 */
	public static String randomForLen(int len) {
		String random = Long.toString(new SecureRandom().nextLong());
		return random.substring(random.length() - len, random.length());
	}

	/**
	 * len is the length of the mdn or phonenum 19bit
	 */
	public static String random(int len) {
		String random = Long.toString(new SecureRandom().nextLong());
		return random.substring(random.length() - 19 + len, random.length());
	}

	/**
	 * check date vilidate
	 * @param mydate 20131231 20140101
	 * @param startDate
	 * @return
	 */
	public static boolean checkDate(String mydate, String ftpCreateFileDate) {
		try {
			if (mydate == null || "".equals(mydate) || mydate.length() < 8
					|| ftpCreateFileDate == null
					|| "".equals(ftpCreateFileDate)) {
				return false;
			} else {
				if(mydate.equals(ftpCreateFileDate)){
					return true;
				}
				//befotre date
				SimpleDateFormat beforesdf = new SimpleDateFormat("yyyyMMdd");
				Date beforeolddate = beforesdf.parse(ftpCreateFileDate);
				Calendar beforecalendar = new GregorianCalendar();
				beforecalendar.setTime(beforeolddate);
				beforecalendar.add(Calendar.DATE, -1);// 把日期往后增加一�?整数�?���?负数�?��移动
				Date beforenewdate = beforecalendar.getTime(); // 这个时间就是日期�?��推一天的结果
				String beforetempdate= beforesdf.format(beforenewdate);
				if(mydate.equals(beforetempdate)){
					return true;
				}	
				//after date 
				SimpleDateFormat aftersdf = new SimpleDateFormat("yyyyMMdd");
				Date afterolddate = aftersdf.parse(ftpCreateFileDate);
				Calendar aftercalendar = new GregorianCalendar();
				aftercalendar.setTime(afterolddate);
				aftercalendar.add(Calendar.DATE, 1);// 把日期往后增加一�?整数�?���?负数�?��移动
				Date afternewdate = aftercalendar.getTime(); // 这个时间就是日期�?��推一天的结果
				String afterTempdate= aftersdf.format(afternewdate);
				if(mydate.equals(afterTempdate)){
					return true;
				}
				return false;							
			}
		} catch (Exception ex) {
			return false;
		}
	}

    
	/**
	 * 获取服务器的主机的IP
	 * 
	 * @return
	 */
	public static String getLocalHostIP() {
		InetAddress addr;
		String localIP=null;
		try {
			addr = InetAddress.getLocalHost();
			localIP = addr.getHostAddress();
		} catch (UnknownHostException e) {	
			e.printStackTrace();
		}
		return localIP;
	}
	
	/**
	 * 获取服务器的主机的host name
	 * @return
	 */
	public static String getLocalHostName() {
		InetAddress addr;
		String localIP=null;
		try {
			addr = InetAddress.getLocalHost();
			localIP = addr.getHostAddress();
		} catch (UnknownHostException e) {	
			e.printStackTrace();
		}
		return localIP;
	}
	
 
	/**
	 * Used to extract and return the extension of a given file.
	 * 
	 * @param f
	 *            Incoming file to get the extension of
	 * @return <code>String</code> representing the extension of the incoming
	 *         file.
	 */
	public static String getExtension(String f) {
		String ext = "";
		int i = f.lastIndexOf('.');

		if (i > 0 && i < f.length() - 1) {
			ext = f.substring(i + 1);
		}
		return ext;
	}

	/**
	 * 获取文件的名称，去掉后缀
	 * 
	 * @param f
	 * @return
	 */
	public static String getFileName(String f) {
		String fname = "";
		int i = f.lastIndexOf('.');
		if (i > 0 && i < f.length() - 1) {
			fname = f.substring(0, i);
		}
		return fname + ".txt";
	}
	
	/**
	 * <li>功能描述：时间相减得到天�?	 * 
	 * @param beginDateStr
	 * @param endDateStr
	 * @return long
	 * @author Administrator
	 */
	public  static long getDaySub(String beginDateStr, String endDateStr) {
		long day = 0;
		java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
				"yyyyMMdd");
		 Date beginDate=null;
		 Date endDate=null;
		try {
			beginDate = format.parse(beginDateStr);
			endDate = format.parse(endDateStr);
			day = (endDate.getTime() - beginDate.getTime())
					/ (24 * 60 * 60 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return day;
	}

	
}
