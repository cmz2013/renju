package core;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * 
 * @author chongming
 * 
 * 游戏窗体
 */
public class RenjuFrame extends JFrame implements Serializable {
	private static final long serialVersionUID = 4080967965225244183L;
	protected static JButton startButton,backButton,saveButton,
								 playButton, exitButton, openButton;
	protected static JComboBox versionBox, colorBox;

	// 棋盘面板
	protected static JPanel boardPanel; 
	// 棋盘、棋子图片
	protected static BufferedImage imgBoard;
	protected static BufferedImage imgBlack;
	protected static BufferedImage imgWhite;
	
	protected boolean completed;
	protected static List<String> terminalTitleKeys;
	
	static {
		versionBox = new JComboBox();
		colorBox = new JComboBox();
		boardPanel = new BoardPanel();
		openButton = new JButton();
		saveButton = new JButton();
		playButton = new JButton();
		exitButton = new JButton();
		startButton = new JButton();
		backButton = new JButton();
		try {
			versionBox.setFocusable(false);
			colorBox.setFocusable(false);
			imgBoard = ImageIO.read(RenjuConfig.getImage("chessboard.jpg"));
			imgBlack = ImageIO.read(RenjuConfig.getImage("b.gif"));
			imgWhite = ImageIO.read(RenjuConfig.getImage("w.gif"));
		} catch (Exception e) {
			System.exit(-1);
		}
	}
	
	/**
	 * 设置界面信息
	 * 
	 * @throws IOException
	 */
	public void setUiText() throws IOException {
		completed = false;
		Properties renjuConfig = RenjuConfig.getRenjuConfig();
		setFrameTitle();
		
		startButton.setText(renjuConfig.getProperty("renju.ui.panel.button.new"));
		backButton.setText(renjuConfig.getProperty("renju.ui.panel.button.back"));
		saveButton.setText(renjuConfig.getProperty("renju.ui.panel.button.save"));
		playButton.setText(renjuConfig.getProperty("renju.ui.panel.button.play"));
		exitButton.setText(renjuConfig.getProperty("renju.ui.panel.button.exit"));
		openButton.setText(renjuConfig.getProperty("renju.ui.panel.button.open"));
		
		if (colorBox.getItemCount() > 0) {
			colorBox.removeAllItems();
		}
		colorBox.addItem(renjuConfig.getProperty("renju.game.color.white"));
		colorBox.addItem(renjuConfig.getProperty("renju.game.color.black"));
		if (GameOperate.getUserColor() == GameOperate.RENJU_WHITE) {
			colorBox.setSelectedItem(renjuConfig.getProperty("renju.game.color.white"));
		} else {
			colorBox.setSelectedItem(renjuConfig.getProperty("renju.game.color.black"));
		}
		
		if (versionBox.getItemCount() > 0) {
			versionBox.removeAllItems();
		}
		versionBox.addItem(renjuConfig.getProperty("renju.ui.version.chinese"));
		versionBox.addItem(renjuConfig.getProperty("renju.ui.version.english"));
		
		if (RenjuConfig.RENJU_VERSION_CN.equals(RenjuConfig.getVersion())) {
			versionBox.setSelectedItem(renjuConfig.getProperty("renju.ui.version.chinese"));
		} else {
			versionBox.setSelectedItem(renjuConfig.getProperty("renju.ui.version.english"));
		}
		completed = true;
	}

	/**
	 * 设置窗口标题
	 * 
	 * @throws IOException
	 */
	public void setFrameTitle() throws IOException {
		Properties renjuConfig = RenjuConfig.getRenjuConfig();
		String title = renjuConfig.getProperty(terminalTitleKeys.get(0)) + " -";
		for (int i = 1; i < terminalTitleKeys.size(); i++) {
			title += " " + renjuConfig.getProperty(terminalTitleKeys.get(i)) + "  ";
		}
		this.setTitle(title);
	}

	public RenjuFrame() throws Exception {
		terminalTitleKeys = new ArrayList<String>();
		terminalTitleKeys.add("renju.ui.panel.title");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (!GameOperate.isContinue()) {
						if (!GameOperate.isGameEnd()) {
							Properties renjuConfig = RenjuConfig.getRenjuConfig();
							int option = JOptionPane.showConfirmDialog(null, 
									renjuConfig.getProperty("renju.game.info.giveup"),
									renjuConfig.getProperty("renju.ui.panel.title.tip"), 
									JOptionPane.OK_CANCEL_OPTION);
							
							if(JOptionPane.CANCEL_OPTION == option) {
								return;
							}
						} 
						GameOperate.setStatus();
						//发送开局的请
						NetService.send(NetService.CMD_START_COLOR + GameOperate.getUserColor()); 
						colorBox.setEnabled(false);
					}
				} catch (Exception e1) {
					GameOperate.gameStartPre(false);
					GameOperate.showErrorMessage("renju.game.info.option.fail");
				}
			}
		});
		
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (!GameOperate.isContinue()) {
						GameOperate.setStatus();
						NetService.send(NetService.CMD_BACK);
					}
				} catch (Exception e1) {
					GameOperate.showErrorMessage("renju.game.info.option.fail");
				}
				
			}
		});
		
		// 保存棋局
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (!GameOperate.isContinue()) {
						GameOperate.saveGame();
					}
				} catch (Exception e1) {
					GameOperate.showErrorMessage("renju.game.info.save.fail");
				}
			} 
		});
		// 回放棋局监听器
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (!GameOperate.isContinue()) {
						GameOperate.showSteps();
					}
				} catch(Exception ex) {
					System.exit(-1);
				}
			} 
		});
		
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (!GameOperate.isContinue()) {
						Properties renjuConfig = RenjuConfig.getRenjuConfig();
						if (!GameOperate.isGameEnd()) {
							int option = JOptionPane.showConfirmDialog(null, 
									renjuConfig.getProperty("renju.game.info.giveup"),
									renjuConfig.getProperty("renju.ui.panel.title.tip"), 
									JOptionPane.OK_CANCEL_OPTION);
							
							if(JOptionPane.CANCEL_OPTION == option) {
								return;
							}
						}
						GameOperate.setStatus();
						NetService.send(NetService.CMD_OPEN);
					}
				} catch (Exception e2) {
					System.exit(-1);
				}
			}
		});
		
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					GameOperate.exitGame();
				} catch (Exception e1) {
					System.exit(-1);
				}
			}
		});
		
		colorBox.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				try {
					if (completed) {
						GameOperate.setUserColor();
					}
				} catch (IOException e1) {
					System.exit(-1);
				}
			}
		});
		
		versionBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (completed) { 
						GameOperate.switchVersion();
						setUiText(); 
					}
				} catch (IOException e1) {
					System.exit(-1);
				}
			}
		});
		JPanel btnPanel = new JPanel();
		btnPanel.add(startButton);
		btnPanel.add(openButton);
		btnPanel.add(colorBox);
		btnPanel.add(versionBox);
		btnPanel.add(saveButton);
		btnPanel.add(backButton);
		btnPanel.add(playButton);
		btnPanel.add(exitButton);
		
		btnPanel.setBounds(0, 0, 536, 30);
		boardPanel.setBounds(0, 30, 536, 570);
		
		btnPanel.setLayout(null);
		startButton.setBounds(0, 0, 67, 30);
		openButton.setBounds(67, 0, 67, 30);
		saveButton.setBounds(132, 0, 67, 30);
		backButton.setBounds(197, 0, 67, 30);
		playButton.setBounds(262, 0, 67, 30);
		exitButton.setBounds(327, 0, 67, 30);
		colorBox.setBounds(392, 0, 69, 30);
		versionBox.setBounds(461, 0, 69, 30);
		
		this.addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent arg0) {
			}
			public void windowClosed(WindowEvent arg0) {
			}

			public void windowClosing(WindowEvent arg0) {
				try {
					GameOperate.exitGame();
				} catch (Exception e) {
					System.exit(-1);
				}
			}

			public void windowDeactivated(WindowEvent arg0) {
			}

			public void windowOpened(WindowEvent arg0) {
			}
			
			public void windowDeiconified(WindowEvent arg0) {
			}

			public void windowIconified(WindowEvent arg0) {
			}

		});
		
		this.setLayout(null);
		this.add(btnPanel);
		this.add(boardPanel);
		this.setLocation(200, 100);
		this.setSize(536,588);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GameOperate.setFrameIconImage(this);
	}
	
	/**
	 * 棋盘panel
	 * 
	 */
	static class BoardPanel extends JPanel  implements Serializable  {
		private static final long serialVersionUID = -2674068184363032374L;

		public BoardPanel() {
			this.addMouseListener(
					new MouseAdapter() {
						public void mouseClicked(MouseEvent e) {
							try {
								GameOperate.playChess(e.getY()/35, e.getX()/35);
							} catch (Exception e1) {
								System.exit(-1);
							}
						}
					});
		}
		
		/**
		 * 重写绘制panel的方法，防止棋盘最小化再还原后，棋局丢失
		 */
		@Override
		protected void paintComponent(Graphics g) {
			// 执行父类中的代码将组建绘制完
			super.paintComponent(g); 
			//组件绘制完成后绘制棋盘
			g.drawImage(imgBoard,0,0,null);		
			//画走过的棋子
			for(int row = 0; row < GameOperate.getBoard().length; row++) {
				for(int col = 0; col < GameOperate.getBoard()[0].length;col++) {
					if(GameOperate.getBoard()[row][col] != 0) {
						BufferedImage img = imgBlack;
						if(GameOperate.getBoard()[row][col] == GameOperate.RENJU_WHITE) {
							img = imgWhite;
						}
						g.drawImage(img,col*35+4,row*35+4,null);
					}
				}
			}
		}
	}

	public static JComboBox getVersionBox() {
		return versionBox;
	}

	public static JComboBox getColorBox() {
		return colorBox;
	}

	public static JPanel getBoardPanel() {
		return boardPanel;
	}

	public static JButton getStartButton() {
		return startButton;
	}

	public static JButton getBackButton() {
		return backButton;
	}

	public static JButton getSaveButton() {
		return saveButton;
	}

	public static JButton getPlayButton() {
		return playButton;
	}

	public static JButton getExitButton() {
		return exitButton;
	}

	public static JButton getOpenButton() {
		return openButton;
	}

	public static List<String> getTerminalTitleKeys() {
		return terminalTitleKeys;
	}
   
}



