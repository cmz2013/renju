package core;

import java.io.File;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * 服务器端
 * 
 * @author chongming
 *
 */
public class RenjuServer extends RenjuFrame  implements Serializable {
	private static final long serialVersionUID = 1268259973565348555L;

	public RenjuServer(int port) throws Exception{
		super();
		terminalTitleKeys.add("renju.ui.panel.title.server");
		terminalTitleKeys.add("renju.game.server.info.init");
		setUiText();
		GameOperate.setGameEnd(true);
		startButton.setEnabled(false);
		backButton.setEnabled(false);
		saveButton.setEnabled(false);
		playButton.setEnabled(false);
		openButton.setEnabled(false);
		
		final ServerSocket serverSocket = new ServerSocket(port);
		
		/**
		 * 获取第一个连接服务器的客户端
		 */
		new Thread() {public void run() {
			try {
				//阻塞方法，监听客户端连接
				Socket socket = serverSocket.accept();
				//服务端不再接受其他客户端的连接
				serverSocket.close();
				new Thread(NetService.getNetService(socket)).start();
				
				terminalTitleKeys.remove(terminalTitleKeys.size() - 1);
				setFrameTitle();
				startButton.setEnabled(true);
				if (new File(GameOperate.getGameFilePath()).exists()) {
					openButton.setEnabled(true);
				}
			} catch(Exception e) {}
		}}.start();
	}
}
