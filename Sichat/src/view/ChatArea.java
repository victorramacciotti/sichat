package view;

import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.Image;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class ChatArea extends JPanel{
	private JTextField messageField;
	public ChatArea() {
		setBackground(new Color(16, 25, 31));
		
		JPanel chatPanel = new JPanel();
		chatPanel.setBackground(new Color(32, 44, 51));
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(chatPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap(581, Short.MAX_VALUE)
					.addComponent(chatPanel, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE))
		);
		
		
		/*MESSAGE TEXTAREA*/
		
		messageField = new JTextField();
		messageField.setForeground(new Color(255, 255, 255));
		messageField.setBackground(new Color(42, 57, 66));
		messageField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		messageField.setColumns(10);
		Color borderColor = new Color(42, 57, 66);
	    messageField.setBorder(BorderFactory.createLineBorder(borderColor, 5));
		
	    
	    /*FILE BUTTON*/
	    
		ImageIcon fileBtnIcon = new ImageIcon(getClass().getResource("/icons/attach_file.png"));
		Image fileBtnIconResizer = fileBtnIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
		ImageIcon fileBtnIconSmooth = new ImageIcon(fileBtnIconResizer);
		JButton fileBtn = new JButton(fileBtnIconSmooth);
		fileBtn.setBackground(new Color(32, 44, 51));
		fileBtn.setOpaque(true); 
		fileBtn.setBorderPainted(false); 
		fileBtn.revalidate();
		fileBtn.repaint();
		fileBtn.setForeground(new Color(255, 255, 255));
		fileBtn.setFont(new Font("Verdana", Font.BOLD, 15));
		fileBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		

		/*SUBMIT/SEND BUTTON*/
		
		ImageIcon submitIcon = new ImageIcon(getClass().getResource("/icons/send_icon.png"));
		Image submitIconResizer = submitIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
		ImageIcon submitIconResizerSmooth = new ImageIcon(submitIconResizer);
		JButton submitBtn = new JButton(submitIconResizerSmooth);
		submitBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		submitBtn.setForeground(new Color(255, 255, 255));
		submitBtn.setFont(new Font("Verdana", Font.BOLD, 15));
		submitBtn.setBackground(new Color(32, 44, 51));
		submitBtn.setOpaque(true);
		submitBtn.setBorderPainted(false);
		submitBtn.revalidate();
		submitBtn.repaint();
		
		/*LAYOUT*/
		GroupLayout gl_chatPanel = new GroupLayout(chatPanel);
		gl_chatPanel.setHorizontalGroup(
			gl_chatPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_chatPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(fileBtn, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(messageField, GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
					.addGap(10)
					.addComponent(submitBtn, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
					.addGap(14))
		);
		gl_chatPanel.setVerticalGroup(
			gl_chatPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_chatPanel.createSequentialGroup()
					.addGroup(gl_chatPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_chatPanel.createSequentialGroup()
							.addGap(16)
							.addGroup(gl_chatPanel.createParallelGroup(Alignment.LEADING, false)
								.addComponent(submitBtn, GroupLayout.PREFERRED_SIZE, 32, Short.MAX_VALUE)
								.addComponent(fileBtn, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addGroup(gl_chatPanel.createSequentialGroup()
							.addGap(13)
							.addComponent(messageField, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(12, Short.MAX_VALUE))
		);
		chatPanel.setLayout(gl_chatPanel);
		setLayout(groupLayout);
	}
}
