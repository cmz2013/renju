package core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * 游戏操作
 * 
 * @author chongming
 *
 */
public class GameOperate {
	/**
	 * 棋盘尺寸
	 */
	public static final int RENJU_PANEL_PIXEL = 15;
	/**
	 * 黑色棋子
	 */
	public static final int RENJU_BLACK = -1;
	/**
	 * 白色棋子
	 */
	public static final int RENJU_WHITE = 1;
	/**
	 * 游戏获胜需要的最少连珠数
	 */
	private static int winPieceNum = RenjuConfig.getWinPieceNum();;
	/**
	 * 连胜(> 0)或连败(< 0)次数
	 */
	private static int winCount = 0;
	/**
	 * 用户棋子颜色
	 */
	private static int userColor = RENJU_WHITE;	
	/**
	 * 游戏结束
	 */
	private static boolean gameEnd = true;
	/**
	 * 棋局已保存
	 */
	private static boolean saved = false;
	/**
	 * 是否轮到下棋
	 */
	private static boolean mySide = false;
	/**
	 * 走棋步骤
	 */
	private static List<String> gameSteps  = new ArrayList<String>();
	/**
	 * 当前棋局
	 */
	private static int[][] board  = new int[GameOperate.RENJU_PANEL_PIXEL]
	                                        						[GameOperate.RENJU_PANEL_PIXEL];
	/**
	 * 保存棋局的文件
	 */
	private static String gameFilePath = RenjuConfig.getConfigPath() + "game\\renju_game.xml";
	
	private static Random rand = new Random();;
	private static Boolean isContinue = false;
	private static Boolean isPlaying = false;
	
	/**
	 * 错误提示
	 * @param mesKey
	 */
	public static void showErrorMessage(String mesKey) {
		try {
			Properties renjuConfig = RenjuConfig.getRenjuConfig();
			JOptionPane.showMessageDialog(null, 
					renjuConfig.get(mesKey), 
					renjuConfig.getProperty("renju.ui.panel.title.tip"), 
					JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			System.exit(-1);
		}
	}
	
	/**
	 * 提示信息
	 * @param mesKey
	 * @throws IOException
	 */
	public static void showInfoMessage(String mesKey) throws IOException {
		Properties renjuConfig = RenjuConfig.getRenjuConfig();
		JOptionPane.showMessageDialog(null, 
				renjuConfig.get(mesKey), 
				renjuConfig.getProperty("renju.ui.panel.title.tip"), 
				JOptionPane.INFORMATION_MESSAGE);
	}
	 
	/**
	 * 保存棋局
	 * @throws IOException 
	 * 
	 */
	public static void saveGame() throws IOException  {
		FileWriter out = null;
		try {
			isContinue = true;
			int m = 0;
			
			/**
			 * 用dom写xml文档
			 */
			//创建工厂
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder =  factory.newDocumentBuilder(); 
			Document doc = builder.newDocument(); 
			Element game = doc.createElement("game");
			for(int i = 0; i < gameSteps.size(); i++) {
				Element step = doc.createElement("step");
				Element row = doc.createElement("row");
				Element col = doc.createElement("col");
				Element color = doc.createElement("color");
				
				int[] rc = getGameStep(i); 
				Text rowText = doc.createTextNode(rc[0] + "");
				Text colText = doc.createTextNode(rc[1]  + "");
				Text colorText = doc.createTextNode(
						board[rc[0] ][rc[1]] + "");

				step.setAttribute("id", ++m + "");
				game.appendChild(step);
				step.appendChild(row);
				step.appendChild(col);
				step.appendChild(color);
				row.appendChild(rowText);
				col.appendChild(colText);
				color.appendChild(colorText);
			}
			doc.appendChild(game);

			// 写文件
			Source xmlSource = new DOMSource(doc);
			TransformerFactory fty = TransformerFactory.newInstance();
			Transformer transformer = fty.newTransformer();
			File gameFile = new File(gameFilePath);
			out = new FileWriter(gameFile);
			Result result = new StreamResult(out);
			transformer.transform(xmlSource, result);
			setSaved(true);
			out.close();
			showInfoMessage("renju.game.info.save.success");
		} catch (Exception e1) {
			showErrorMessage("renju.game.info.save.fail");
		}  finally {
			isContinue = false;
		}
	}
	
	/**
	 * 监测棋局是否已获胜
	 * 
	 * @param row
	 * @param col
	 * @return
	 * @throws IOException 
	 */
	public static boolean isWin(int row,  int col) throws IOException {
		int color = 0;
		//监测方向
		int[][] directions = {{0, 1}, {1, 0}, {-1, 1}, {1, 1}};
		for (int i = 0; i < directions.length; i++) {
			color = check(row, col, directions[i][0], directions[i][1]);
			if (color != 0) {
				break;
			}
		}
		
		if(color != 0) {
			int userColor = getUserColor();
			if (userColor == color) {
				if (winCount > 0) {
					winCount++;
					//连续获胜的消息
					showInfoMessage(getGameWinsKey());
				} else if (winCount <= 0) {
					winCount = 1;
					//一次获胜的消息
					showInfoMessage(getGameWinKey());
				}
			} else {
				if (winCount < 0) {
					winCount--;
					//连续失败的消息
					showInfoMessage(getGameFailsDesKey());
				} else if (winCount >= 0) {
					winCount = -1;
					//一次失败的消息
					showInfoMessage(getGameFailDesKey());
				}
			}
			setGameEnd(true);
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 在指定方向上监测棋局是否已获胜
	 * 
	 * @param row
	 * @param col
	 * @param rowStep
	 * @param colStep
	 * @return
	 */
	private static int check( int row, int col, int rowStep, int colStep) {
		int count = 1;
		int r = row + rowStep;
		int c = col +  colStep;
		
		while(count < winPieceNum) {
			if(isNotCrossBorder(c, r) && (board[r][c] == board[row][col])) {
				count++;
				if(count == winPieceNum) {
					return  board[row][col];
				}
				r += rowStep;
				c += colStep;
			} else {
				break;
			}
		}
		
		rowStep =  -rowStep;
		colStep = -colStep;
		c = col + colStep;
		r = row + rowStep;
		while(count < winPieceNum) {
			if(isNotCrossBorder(c, r) && (board[r][c] == board[row][col])) {
				count++;
				if(count == winPieceNum) {
					return  board[row][col];
				}
				r += rowStep;
				c += colStep;
			} else {
				break;
			}
		}
		return 0;
	}

	/**
	 * 坐标是否越界
	 * 
	 * @param y
	 * @param x
	 * @return
	 */
	private static boolean isNotCrossBorder(int x, int y) {
		if (y >= 0 && x >= 0 
				&& y < RENJU_PANEL_PIXEL 
				&& x < RENJU_PANEL_PIXEL) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 一次获胜的信息key
	 * @return
	 */
	private static String getGameWinKey() {
		String key = "renju.game.result.win.des";
		key += rand.nextInt(3);
		return key;
	}
	
	/**
	 * 连续获胜的信息key
	 * @return
	 */
	private static String getGameWinsKey() {
		String key = "renju.game.result.win.des";
		key += 1;
		key += rand.nextInt(3);
		return key;
	}
	
	/**
	 * 一次失败的信息key
	 * @return
	 */
	private static String getGameFailDesKey() {
		String key = "renju.game.result.fail.des";
		key += rand.nextInt(3);
		return key;
	}
	
	/**
	 * 连续失败的信息key
	 * @return
	 */
	private static String getGameFailsDesKey() {
		String key = "renju.game.result.fail.des";
		key += 1;
		key += rand.nextInt(3);
		return key;
	}
	
	public static void initBoard() {
		
	}
	
	/**
	 * 在本地打开已保存的棋局
	 * @throws Exception 
	 */
	public static boolean localOpenGame() throws Exception {
		try {
			String steps = "";
			clearBoard();
			
			/*
			 * DOM解析器解析xml文档
			 */
			//创建工厂
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			Document doc = null;
			//用工厂创建解析器
			DocumentBuilder builder = factory.newDocumentBuilder();
			//用解析器进行解析
			doc = builder.parse(new File(gameFilePath));
			//提取xml文档数据，即board[][]、gameSteps填入数据
			print(doc); 
			
			for (int i = 0; i < gameSteps.size(); i++) {
				int[] rc = getGameStep(i);
				steps += gameSteps.get(i) + "," + board[rc[0]][rc[1]] +  ";";
			}
			
			RenjuFrame.getBoardPanel().repaint();
			RenjuFrame.getSaveButton().setEnabled(false);
			RenjuFrame.getOpenButton().setEnabled(false);
			
			steps +=  (-GameOperate.getUserColor());
			NetService.send(NetService.CMD_OPEN_STEP_COLOR + steps);
			GameOperate.restoreStatus();
			GameOperate.setGameEnd(false);
			openGameTurnTip(true);
			return true;
		} catch (Exception e) {
			gameStartPre(false);
			NetService.send(NetService.CMD_OPEN_FAIL);
			showErrorMessage("renju.game.info.option.fail");
			RenjuFrame.getColorBox().setEnabled(true);
			return false;
		}
	}
	
	public static void localOpenGame(String stepStr) throws Exception {
		String[] steps = stepStr.split(";");
		
		int userColor = Integer.parseInt(steps[steps.length - 1]);
		String userColorDesKey = GameOperate.getUserColorDesKey(userColor);
		
		RenjuFrame.getColorBox().setSelectedItem(
				RenjuConfig.getRenjuConfig().getProperty(userColorDesKey));
		
		clearBoard();
		for (int i = 0; i < steps.length - 1; i++) {
			String[] rcc = steps[i].split(",");
			board[Integer.parseInt(rcc[0])]
			      [Integer.parseInt(rcc[1])] = Integer.parseInt(rcc[2]);
			gameSteps.add(rcc[0] + "," +  rcc[1]);
		}
		
		RenjuFrame.getBoardPanel().repaint();
		
		RenjuFrame.getSaveButton().setEnabled(true);
		GameOperate.restoreStatus();
		GameOperate.setGameEnd(false);
		openGameTurnTip(false);
	}
	
	/**
	 * 设置等待状态
	 * @throws IOException 
	 */
	public static void setStatus() throws IOException {
		isContinue = true;
		RenjuFrame.getTerminalTitleKeys().add("renju.game.info.response");
		RenjuGame.getRenjuFrame().setFrameTitle();
	}
	
	/**
	 * 撤销等待状态
	 * @throws IOException 
	 */
	public static void restoreStatus() throws IOException {
		isContinue = false;
		RenjuFrame.getTerminalTitleKeys().remove(RenjuFrame.getTerminalTitleKeys().size() - 1);
		RenjuGame.getRenjuFrame().setFrameTitle();
	}
	
	/**
	 * 清空棋局
	 */
	public static void clearBoard() {
		for (String rcs : gameSteps) {
			String[] rc = rcs.split(",");
			board[Integer.parseInt(rc[0])][Integer.parseInt(rc[1])] = 0;
		}
		RenjuFrame.getBoardPanel().repaint();
		clearGameSteps();
	}

	/**
	 * 提取xml文档数据
	 * 
	 * @param e
	 */
	private static void print(Node e) {
		//用数字表示节点类型，不同类型不同处理
		short type = e.getNodeType(); 
		switch (type) {
		case Node.DOCUMENT_NODE: {
			NodeList games = e.getChildNodes(); 
			// 递归调用
			for (int i = 0; i < games.getLength(); i++) {
				print(games.item(i));
			}
			break;
		}
		case Node.ELEMENT_NODE: {
			int i = 0,j = 0;
			NodeList steps = e.getChildNodes();
			for (int l = 0; l < steps.getLength(); l++) {
				NodeList rcc = steps.item(l).getChildNodes();
				for (int n = 0; n < rcc.getLength(); n++) {
					Node node = rcc.item(n);
					if ("row".equals(node.getNodeName())) {
						i = Integer.parseInt(node.getTextContent());
					} else if ("col".equals(node.getNodeName())) {
						j = Integer.parseInt(node.getTextContent());
					} else if ("color".equals(node.getNodeName())) {
						board[i][j] = Integer.parseInt(node.getTextContent());
						gameSteps.add(i+","+j);
					}
				}
			}
			break;
		}
		default:
			break;
		}
	}
	
	/**
	 * 棋盘不空
	 * @return
	 */
	public static boolean isNotEmptyBoard() {
		return (gameSteps.size() > 0) ? true : false;
	}
	
	/**
	 * 退出游戏,删除棋局保存文件
	 * @throws IOException 
	 */
	public static void exitGame() throws Exception {
		Properties renjuConfig = RenjuConfig.getRenjuConfig();
		int option = JOptionPane.showConfirmDialog(null,  
				renjuConfig.getProperty("renju.game.info.isexit"), 
				renjuConfig.getProperty("renju.ui.panel.title.tip"), 
				JOptionPane.OK_CANCEL_OPTION);
		
		if(JOptionPane.OK_OPTION == option) {
			NetService.send(NetService.CMD_EXIT);
			System.exit(0);
		}
	}
	
	/**
	 * 根据用户选项设置用户棋子颜色
	 * @return
	 * @throws IOException
	 */
	public static void setUserColor() throws IOException  {
		String colorDes = (String) RenjuFrame.getColorBox().getSelectedItem();
		String whiteDes = RenjuConfig.getRenjuConfig().getProperty("renju.game.color.white");
		if (whiteDes.equals(colorDes)) {
			userColor = RENJU_WHITE;
		} else {
			userColor = RENJU_BLACK;
		}
	}
	
	/**
	 * 打开棋局走棋提示
	 * @param currentColor
	 * @param isActive
	 * @return
	 * @throws IOException 
	 */
	public static void openGameTurnTip(boolean isActive) throws IOException {
		Properties renjuConfig = RenjuConfig.getRenjuConfig();
		int[] rc = getGameLastStep();
		if (isActive) {
			if (board[rc[0]][rc[1]]  != userColor) {
				showInfoMessage("renju.game.info.turn.you");
				mySide = true;
			} else {
				showInfoMessage("renju.game.info.turn.he");
				mySide = false;
			}
		} else {
			String desKey = (-userColor == GameOperate.RENJU_WHITE) ? 
					"renju.game.info.color.white" : "renju.game.info.color.black";
			if (board[rc[0]][rc[1]]  != userColor) {
				JOptionPane.showMessageDialog(null,
						renjuConfig.getProperty(desKey) + "," +
						renjuConfig.getProperty("renju.game.info.turn.you"),
						renjuConfig.getProperty("renju.ui.panel.title.tip"),
						JOptionPane.INFORMATION_MESSAGE);
				mySide = true;
			} else {
				JOptionPane.showMessageDialog(null,
						renjuConfig.getProperty(desKey) + "," +
						renjuConfig.getProperty("renju.game.info.turn.he"),
						renjuConfig.getProperty("renju.ui.panel.title.tip"),
						JOptionPane.INFORMATION_MESSAGE);
				mySide =  false;
			}
		}
	}

	/**
	 * @param userColor
	 * @return 用户颜色Key
	 * @throws IOException 
	 */
	public static String getUserColorDesKey(int userColor) throws IOException {
		String desKey = (userColor == GameOperate.RENJU_WHITE) ? 
				"renju.game.color.white" : "renju.game.color.black";
		return desKey;
	}
	
	/**
	 * @param userColor
	 * @return 开局信息Key
	 * @throws IOException 
	 */
	public static String getStartTipKey(int userColor) throws IOException {
		String desKey = (userColor == GameOperate.RENJU_WHITE) ? 
				"renju.game.info.new.white" : "renju.game.info.new.black";
		return desKey;
	}

	/**
	 * 切换版本
	 * @param selVersion
	 * @throws IOException
	 */
	public static void switchVersion() throws IOException {
		Properties renjuConfig = RenjuConfig.getRenjuConfig();
		String version = null;
		String versionKey = null;
		if (RenjuConfig.getVersion().equals(RenjuConfig.RENJU_VERSION_CN)) {
			versionKey = "renju.ui.version.chinese"; 
			version = RenjuConfig.RENJU_VERSION_EN;
		} else {
			versionKey = "renju.ui.version.english";
			version = RenjuConfig.RENJU_VERSION_CN;
		}
		
		String selItem = (String) RenjuFrame.getVersionBox().getSelectedItem();
		if (!renjuConfig.getProperty(versionKey).equals(selItem)) {
			RenjuConfig.saveVersion(version);
		}
	}
	
	/**
	 * 悔棋操作
	 */
	public static void localBack() {
		// 从走棋步骤集合中移除最后一步棋
		int[] rc = getGameLastStep();
		board[rc[0]][rc[1]] = 0;
		gameSteps.remove(gameSteps.size() - 1);
		RenjuFrame.getBoardPanel().repaint();
		
		switchMySide();
		if (isEmptyBoard()) {
			RenjuFrame.getBackButton().setEnabled(false);
		}
	}
	
	/**
	 * 最后一步棋
	 * @return
	 */
	public static int[] getGameLastStep() {
		String s = gameSteps.get(gameSteps.size()-1);
		String[] arr = s.split(",");
		int row = Integer.parseInt(arr[0]);
		int col = Integer.parseInt(arr[1]);
		int[] rc = {row, col};
		return rc;
	}

	/**
	 * 棋盘空
	 * @return
	 */
	public static boolean isEmptyBoard() {
		return !isNotEmptyBoard();
	}

	/**
	 * 在本地下棋
	 * @param row
	 * @param col
	 * @return
	 * @throws IOException
	 */
	public static boolean localPlayChess(int row, int col) throws IOException {
		if(row<0 || col<0 || row>14 || col>14) return false;
		//有棋的位置不能再落子
		if(board[row][col] != 0) return false;  
		if (mySide) {
			board[row][col] = getUserColor();
		} else {
			board[row][col] = -getUserColor();
		}
		switchMySide();
		saveGameStep(row, col);
		//重绘当前面板,会执行paintComponent方法
		RenjuFrame.getBoardPanel().repaint();
		setSaved(false);
		if (!isWin(row, col) && isFirstStep()) {
			RenjuFrame.getBackButton().setEnabled(true);
		}
		return true;
	}
	
	/**
	 * 下棋操作
	 * @param row
	 * @param col
	 * @throws IOException
	 */
	public static void playChess(int row, int col) throws Exception {
		if(isGameEnd()) return;
		if(! isMySide()) return;
		if (isContinue) return;
		
		if (localPlayChess(row, col)) {
			NetService.send(row+","+col);
		}
	}
	
	/**
	 * 保存下棋步骤
	 * @param row
	 * @param col
	 */
	public static void saveGameStep(int row, int col) {
		getGameSteps().add(row+","+col);
	}

	/**
	 * 判断是否为第一次落子
	 * @return
	 */
	private static boolean isFirstStep() {
		return (gameSteps.size() == 1) ? true : false;
	}

	/**
	 * 准备开棋
	 * @param mySide
	 */
	public static void gameStartPre(boolean mySide) {
		clearBoard();
		setGameEnd(false);
		setSaved(false);
		setMySide(mySide);
	}
	
	/**
	 * 棋局回播操作
	 * @throws Exception
	 */
	public static void showSteps() {
		Thread play = new Thread() {
			public void run() {
				try {
					setPlaying(true);
					int[][] boardc = board;
					board = new int[GameOperate.RENJU_PANEL_PIXEL]
					                [GameOperate.RENJU_PANEL_PIXEL];
					
					RenjuFrame.getBoardPanel().repaint();
					for(int i = 0; i < getGameSteps().size(); i++) {
						int[] rc = getGameStep(i);
						board[rc[0]][rc[1]] = boardc[rc[0]][rc[1]];
						Thread.sleep(RenjuConfig.getPlaySpeed());
						 // 仅在本地开始回放
						localPlayChess(rc[0], rc[1]); 
						RenjuFrame.getBoardPanel().repaint();
					}
					setPlaying(false);
				} catch(Exception e) {
					gameStartPre(false);
					showErrorMessage("renju.game.info.option.fail");
				}
			}
		};
		play.setPriority(8);
		play.start();
	}
	
	/**
	 * 获得游戏第n次的步骤
	 * @paramn
	 * @return
	 */
	public static int[] getGameStep(int n) {
		String str = gameSteps.get(n);
		String[] temp = str.split(",");
		int[] rc = {Integer.parseInt(temp[0]), Integer.parseInt(temp[1])};
		return rc;
	}
	
	/**
	 * 设置窗口图标
	 * 
	 * @throws IOException 
	 */
	public static void setFrameIconImage(JFrame frame) throws IOException {
		BufferedImage im = ImageIO.read(RenjuConfig.getImage("renju.jpg"));
		frame.setIconImage(im);
	}
	
	/**
	 * 清空保存的游戏步骤
	 */
	public static void clearGameSteps() {
		 gameSteps.clear();
	}
	
	/**
	 * 切换下棋方
	 */
	public static void switchMySide() {
		GameOperate.mySide = !GameOperate.mySide ;
	}
	
	public static boolean isSaved() {
		return saved;
	}

	public static void setSaved(boolean saved) {
		if (saved) {
			RenjuFrame.getSaveButton().setEnabled(!saved);
			RenjuFrame.getOpenButton().setEnabled(!saved);
		} else {
			RenjuFrame.getSaveButton().setEnabled(isNotEmptyBoard());
			RenjuFrame.getOpenButton().setEnabled(
					new File(gameFilePath).exists());
		}
		GameOperate.saved = saved;
	}

	public int getLastWinCount() {
		return winCount;
	}

	public void setWinCount(int winCount) {
		GameOperate.winCount = winCount;
	}

	public static boolean isGameEnd() {
		return gameEnd;
	}

	public static void setGameEnd(boolean gameEnd) {
		if(gameEnd) {
			RenjuFrame.getBackButton().setEnabled(false);
			RenjuFrame.getSaveButton().setEnabled(false);
			RenjuFrame.getColorBox().setEnabled(true);
			RenjuFrame.getPlayButton().setEnabled(true);
			setMySide(false);
		}  else {
			RenjuFrame.getBackButton().setEnabled(isNotEmptyBoard());
			RenjuFrame.getColorBox().setEnabled(false);
			RenjuFrame.getPlayButton().setEnabled(false);
		}
		GameOperate.gameEnd = gameEnd;
	}

	public static boolean isMySide() {
		return mySide;
	}

	public static void setMySide(boolean mySide) {
		GameOperate.mySide = mySide;
	}
	
	public static List<String> getGameSteps() {
		return gameSteps;
	}
	
	public static int getUserColor()  {
		return userColor;
	}

	public static int[][] getBoard() {
		return board;
	}

	public static String getGameFilePath() {
		return gameFilePath;
	}

	public static Boolean isContinue() {
		return isContinue;
	}

	public static void setPlaying(Boolean playing) throws IOException {
		isPlaying  = playing;
		isContinue = playing;
		RenjuFrame.getPlayButton().setEnabled(!playing);
		
		if (playing) {
				RenjuFrame.getTerminalTitleKeys().add("renju.game.info.playing");
		} else {
			RenjuFrame.getTerminalTitleKeys().remove(RenjuFrame.getTerminalTitleKeys().size() - 1);
		}
		RenjuGame.getRenjuFrame().setFrameTitle();
	}
	
	public static Boolean isPlaying() {
		return isPlaying;
	}
}
