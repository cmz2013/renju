package core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 启动游戏终端
 * @author chongming
 *
 */
public class RenjuGame extends JFrame implements Serializable  {
	private static final long serialVersionUID = -5249883296855427331L;
	private JLabel terminalTypeTitle;
	private JComboBox terminalTypeBox;
	private JLabel terminalPortTitle;
	private JTextField terminalPortText;
	private JLabel serviceIpTitle;
	private JTextField serviceIpText;
	private JButton okButton;
	private JButton cancelButton;
	private static RenjuFrame renjuFrame;
	
	public RenjuGame() throws IOException {
		super();
		final Properties renjuConfig = RenjuConfig.getRenjuConfig(); 
		terminalTypeTitle = new JLabel(renjuConfig.getProperty("renju.terminal.type"));
		terminalPortTitle = new JLabel(renjuConfig.getProperty("renju.terminal.port"));
		serviceIpTitle = new JLabel(renjuConfig.getProperty("renju.game.client.info.login.ip"));
		okButton = new JButton(renjuConfig.getProperty("renju.ui.panel.button.ok"));
		cancelButton = new JButton(renjuConfig.getProperty("renju.ui.panel.button.cancel"));
		terminalTypeBox = new JComboBox();
		terminalTypeBox.setFocusable(false);
		terminalTypeBox.addItem(renjuConfig.getProperty("renju.ui.panel.title.server"));
		terminalTypeBox.addItem(renjuConfig.getProperty("renju.ui.panel.title.client"));
		terminalPortText = new JTextField();
		terminalPortText.setText(RenjuConfig.getServicePort());
		terminalPortText.selectAll();
		serviceIpText = new JTextField();
		serviceIpText.setText("localhost");
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(null);
		okButton.setBounds(61,5,73, 25);
		cancelButton.setBounds(141, 5, 73, 25);
		terminalTypeTitle.setBounds(10, 5, 80, 30);
		terminalPortTitle.setBounds(10, 85, 80, 30);
		serviceIpTitle.setBounds(10, 45, 80, 30);
		terminalTypeBox.setBounds(90, 10, 172, 25);
		terminalPortText.setBounds(90, 90, 172, 25);
		serviceIpText.setBounds(90, 50, 172, 25);
		serviceIpText.setEnabled(false);
		btnPanel.setBounds(5, 120, 260, 40);
		this.setLayout(null);
		this.add(terminalTypeTitle);
		this.add(terminalPortTitle);
		this.add(terminalTypeBox);
		this.add(terminalPortText);
		this.add(serviceIpText);
		this.add(serviceIpTitle);
		btnPanel.add(okButton);
		btnPanel.add(cancelButton);
		this.add(btnPanel);
		terminalTypeBox.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (renjuConfig.getProperty("renju.ui.panel.title.server").equals(
						terminalTypeBox.getSelectedItem())) {
					serviceIpText.setEnabled(false);
					serviceIpText.setText("localhost");
				} else {
					serviceIpText.setEnabled(true);
					serviceIpText.setText(RenjuConfig.getServerIp());
				}
			}
		});
		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (renjuConfig.getProperty("renju.ui.panel.title.server").equals(
						terminalTypeBox.getSelectedItem())) {
					try {
						int port = getServicePort();
						if (port < 0) {
							return;
						}
						setVisible(false);
						renjuFrame = new RenjuServer(port);
						renjuFrame.setVisible(true);
						RenjuConfig.saveServicePort(port);
					} catch (Exception e1) {
						GameOperate.showErrorMessage("renju.game.server.info.error");
						System.exit(-1);
					}
				} else {
					try {
						String ip = serviceIpText.getText().trim();
						if (null == ip || "".equals(ip)) {
							GameOperate.showErrorMessage("renju.game.client.info.login.ip.error");
							return;
						}
						int port = getServicePort();
						if (port < 0) {
							return;
						}
						setVisible(false);
						renjuFrame = new RenjuClient(ip, port);
						renjuFrame.setVisible(true);
						RenjuConfig.saveServicePort(port);
						RenjuConfig.saveServerIP(ip);
					} catch (Exception e1) {
						GameOperate.showErrorMessage("renju.game.client.info.login.fail");
						System.exit(-1);
					} 
				}
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				System.exit(0);
			}
		});
		
		this.addWindowListener(new WindowListener() {
			
			public void windowDeactivated(WindowEvent e) { }
			public void windowIconified(WindowEvent e) { }
			public void windowOpened(WindowEvent e) { }			
			public void windowDeiconified(WindowEvent e) { }
			public void windowClosing(WindowEvent e) { 
				System.exit(0);
			}
			public void windowClosed(WindowEvent e) { }
			public void windowActivated(WindowEvent e) { }
		});
		
		this.setTitle(renjuConfig.getProperty("renju.ui.panel.title.tip"));
		this.setLocation(550, 290);
		this.setSize(280,190);
		this.setResizable(false);
		GameOperate.setFrameIconImage(this);
	}
	
	private int getServicePort() {
		String portText = terminalPortText.getText().trim();
		int port = 0;
		try {
			if (null == portText || "".equals(portText)) {
				GameOperate.showErrorMessage("renju.terminal.port.blank");
				return -1;
			}
			port = Integer.parseInt(portText);
		} catch (Exception e) {
			GameOperate.showErrorMessage("renju.terminal.port.error");
			return -1;
		}
		return port;
	}
	
	public static RenjuFrame getRenjuFrame() {
		return renjuFrame;
	}
	
	public static void main(String[] args) throws Exception {
		new RenjuGame().setVisible(true);
	}
}
