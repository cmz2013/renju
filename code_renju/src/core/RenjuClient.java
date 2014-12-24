package core;

import java.io.File;
import java.io.Serializable;
import java.net.Socket;

/**
 * 客户端
 * @author chongming
 *
 */
public class RenjuClient extends RenjuFrame implements Serializable {
	private static final long serialVersionUID = 8537069469820517709L;

	public RenjuClient(String ip, int port) throws Exception{
		super();
		terminalTitleKeys.add("renju.ui.panel.title.client");
		setUiText();
		GameOperate.setGameEnd(true);
		backButton.setEnabled(false);
		saveButton.setEnabled(false);
		playButton.setEnabled(false);

		if (!new File(GameOperate.getGameFilePath()).exists()) {
			openButton.setEnabled(false);
		}
		
		//阻塞方法，获取连接
		Socket socket = new Socket(ip,port);
		new Thread(NetService.getNetService(socket)).start();
	}
}
