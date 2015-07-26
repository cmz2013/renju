package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JOptionPane;
/**
 * 终端通讯操作，客户端和服务器端公用
 * 
 * @author chongming
 *
 */
public class NetService implements Runnable {
		public static final String CMD_START_COLOR = "start_color";
		public static final String CMD_START_YES = "start_yes";
		public static final String CMD_BACK = "back";
		public static final String CMD_START_NO = "start_no";
		public static final String CMD_BACK_YES = "back_yes";
		public static final String CMD_BACK_NO = "back_no";
		public static final String CMD_OPEN_YES =  "open_yes";
		public static final String CMD_OPEN_NO = "open_no";
		public static final String CMD_OPEN = "open";
		public static final String CMD_EXIT = "exit";
		public static final String CMD_OPEN_FAIL = "open_fail";
		public static final String CMD_OPEN_STEP_COLOR = "open_step_color";
		
		private static BufferedReader in;
		private  static PrintWriter out;
		
		public static NetService getNetService(Socket socket) throws IOException {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			return new NetService();
		}
		
		/**
		 * 发送消息
		 * 
		 * @param msg
		 */
		public static void send(String msg) throws Exception {
			out.println(msg);
			out.flush();
		}
		
		public void run() {
			try {
				while(true) {
					String line = in.readLine();
					if(null == line) break;
					if(line.startsWith(CMD_START_COLOR)) {
						while (GameOperate.isPlaying()) {
							Thread.sleep(1000);
						}
						
						int userColor = Integer.parseInt(line.substring(CMD_START_COLOR.length()));
						String startTipKey = GameOperate.getStartTipKey(userColor);
						int i = JOptionPane.showConfirmDialog(null, 
								RenjuConfig.getRenjuConfig().getProperty(startTipKey), 
								RenjuConfig.getRenjuConfig().getProperty("renju.ui.panel.title.tip"), 
								JOptionPane.YES_NO_OPTION);
						if(i==JOptionPane.YES_OPTION){
							String userColorDesKey = GameOperate.getUserColorDesKey(-userColor);
							RenjuFrame.getColorBox().setSelectedItem(
									RenjuConfig.getRenjuConfig().getProperty(userColorDesKey));
							//对方点击开始，对方先下棋
							GameOperate.gameStartPre(false);
							send(CMD_START_YES);
						} else {
							send(CMD_START_NO);
						}
					} else if(CMD_START_NO.equals(line)) {
						GameOperate.showInfoMessage("renju.game.info.refuse");
						RenjuFrame.getColorBox().setEnabled(GameOperate.isGameEnd());
						GameOperate.restoreStatus();
					} else if(CMD_START_YES.equals(line)) {
						GameOperate.gameStartPre(true);
						GameOperate.restoreStatus();
					} else if(CMD_BACK.equals(line)) {
						int option = JOptionPane.showConfirmDialog(null,
								RenjuConfig.getRenjuConfig().getProperty("renju.game.info.huiqi"),
								RenjuConfig.getRenjuConfig().getProperty("renju.ui.panel.title.tip"),
								JOptionPane.YES_NO_OPTION);
						if(JOptionPane.YES_OPTION == option) {
							GameOperate.localBack(); 
							send(CMD_BACK_YES);
						} else {
							send(CMD_BACK_NO);
						}
					} else if(CMD_BACK_YES.equals(line)) { 
						//悔棋方悔棋
						GameOperate.localBack();
						GameOperate.restoreStatus();
					} else if(CMD_BACK_NO.equals(line)) {
						GameOperate.showInfoMessage("renju.game.info.refuse");
						GameOperate.restoreStatus();
					} else if(CMD_OPEN.equals(line)) {
						while (GameOperate.isPlaying()) {
							Thread.sleep(1000);
						}
						int option = JOptionPane.showConfirmDialog(null,
								RenjuConfig.getRenjuConfig().getProperty("renju.game.info.open"),
								RenjuConfig.getRenjuConfig().getProperty("renju.ui.panel.title.tip"),
								JOptionPane.YES_NO_OPTION);
						if(JOptionPane.YES_OPTION == option) {
							GameOperate.setStatus();
							send(CMD_OPEN_YES);
						} else {
							send(CMD_OPEN_NO);
						}
					} else if(CMD_OPEN_YES.equals(line)) {
						GameOperate.localOpenGame();
					} else if (CMD_OPEN_FAIL.equals(line)) {
						GameOperate.gameStartPre(false);
						GameOperate.restoreStatus();
						GameOperate.showErrorMessage("renju.game.info.fail");
					} else if (line.startsWith(CMD_OPEN_STEP_COLOR)) {
						GameOperate.localOpenGame(line.substring(CMD_OPEN_STEP_COLOR.length()));
					}  else if(CMD_OPEN_NO.equals(line)) {
						GameOperate.showInfoMessage("renju.game.info.refuse");
						GameOperate.restoreStatus();
					}  else if (CMD_EXIT.equals(line)) {
						GameOperate.showInfoMessage("renju.game.info.exit");
						System.exit(0);
					} else {
						//对方走棋，在本地回显对方棋子
						String[] arr = line.split(",");
						int row = Integer.parseInt(arr[0]);
						int col = Integer.parseInt(arr[1]);
						GameOperate.localPlayChess(row,col); 
					}
				}
			} catch(Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
			} 
		}
	}