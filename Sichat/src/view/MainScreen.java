package view;

import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Font;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class MainScreen extends JPanel{
	public MainScreen() {
		setBackground(new Color(32, 44, 51));
		setPreferredSize(new Dimension(870, 670));
		
		ChatArea chatArea = new ChatArea();
		
		JPanel chatHeader = new JPanel();
		chatHeader.setBackground(new Color(32, 44, 51));
		
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(chatArea, GroupLayout.DEFAULT_SIZE, 976, Short.MAX_VALUE)
				.addComponent(chatHeader, GroupLayout.DEFAULT_SIZE, 976, Short.MAX_VALUE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(chatHeader, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(chatArea, GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE))
		);
		chatHeader.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("RECEPTOR");
		lblNewLabel.setBounds(10, 18, 110, 20);
		lblNewLabel.setForeground(new Color(255, 255, 255));
		lblNewLabel.setBackground(new Color(240, 240, 240));
		lblNewLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
		chatHeader.add(lblNewLabel);
		setLayout(groupLayout);
	}
}
