package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JOptionPane;

/**
 * 配置文件操作
 * 
 * @author chongming
 *
 */
public class RenjuConfig {
	//英文版本
	public static final String RENJU_VERSION_EN = "en";
	//中文版本
	public static final String RENJU_VERSION_CN = "cn";
	
	//游戏配置
	private static Properties renjuConfigEn = null;
	private static Properties renjuConfigCn = null;
	private static Properties renjuPro = null;
	
	//用户版本
	private static String version = null;
	//配置文件根路径
	private static String configPath;
	/**
	 * 回播速度，单位秒
	 */
	private static int playSpeed;
	
	static {
		try {
			configPath = System.getProperty("user.dir") +  "\\config\\";
			renjuPro = initRenjuConfig("renju.properties");
			version = renjuPro.getProperty("renju.version");
			playSpeed = Integer.parseInt(renjuPro.getProperty("renju.play.speed")) * 1000;
		} catch (IOException e) {
			System.exit(-1);
		}
	}
	
	/**
	 * 获取用户版本
	 * @return
	 * @throws IOException
	 */
	public static String getVersion() throws IOException {
		return version;
	}
	
	/**
	 * 记录用户版本
	 * @param version
	 * @throws IOException
	 */
	public static void saveVersion(String version) throws IOException {
		renjuPro.setProperty("renju.version", version);
		FileWriter writer = new FileWriter(new File(configPath + "renju.properties"));
		renjuPro.store(writer, null);
		RenjuConfig.version = version;
	}
	
	/**
	 * 获取游戏配置文件
	 * @return
	 * @throws IOException
	 */
	public static Properties getRenjuConfig() throws IOException {
		if (RENJU_VERSION_CN.equals(version)) {
			JOptionPane.setDefaultLocale(Locale.CHINA);
			if (null == renjuConfigCn) {
				renjuConfigCn = initRenjuConfig("game/renju_des_cn.properties");
			} 
			return renjuConfigCn;
		}
		
		if (RENJU_VERSION_EN.equals(version)) {
			JOptionPane.setDefaultLocale(Locale.US);
			if (null == renjuConfigEn) {
				renjuConfigEn = initRenjuConfig("game/renju_des_en.properties");
			}
			return renjuConfigEn;
		}
		return null;
	}
	
	/**
	 * 获取游戏回播速度
	 * @return
	 */
	public static int getPlaySpeed() {
		return playSpeed;
	}
	
	/**
	 * 获取游戏获胜时的连子数
	 * @return
	 */
	public static int getWinPieceNum() {
		return Integer.parseInt(renjuPro.getProperty("renju.win.piece.num"));
	}
	
	/**
	 * 获取服务端口
	 * @return
	 */
	public static String getServicePort() {
		return renjuPro.getProperty("renju.service.port");
	}
	
	/**
	 * 保存服务端口
	 * @throws IOException 
	 */
	public static void saveServicePort(int servicePort) throws IOException {
		renjuPro.setProperty("renju.service.port", servicePort + "");
		FileWriter writer = new FileWriter(new File(configPath + "renju.properties"));
		renjuPro.store(writer, null);
	}
	
	/**
	 * 加载配置文件
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private static Properties initRenjuConfig(String fileName) throws IOException {
		Properties renjuConfig = new Properties();
		InputStream inStream = new FileInputStream(new File(configPath + fileName));
		renjuConfig.load(inStream);
		inStream.close();
		return renjuConfig;
	}
	
	/**
	 * 获取配置文件根目录config
	 * @return
	 */
	public static String getConfigPath() {
		return configPath;
	}
	
	/**
	 * 获取图片文件
	 */
	public static File getImage(String fileName) {
		return new File(configPath + "image/" + fileName);
	}

	/**
	 * 获取服务IP
	 * @return
	 */
	public static String getServerIp() {
		return renjuPro.getProperty("renju.server.ip");
	}
	
	/**
	 * 保存服务IP
	 * @throws IOException 
	 */
	public static void saveServerIP(String serverIp) throws IOException {
		renjuPro.setProperty("renju.server.ip", serverIp);
		FileWriter writer = new FileWriter(new File(configPath + "renju.properties"));
		renjuPro.store(writer, null);
	}

}
