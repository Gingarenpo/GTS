package com.gfactory.gts.tool.component;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.gfactory.gts.tool.helper.I18n;

/**
 * Pack Makerを最初に起動したときに表示されるようこそ画面
 * 一応これは独立したタブなのでパネルとして保管
 */
public class GTSWelcomeTab extends JPanel {
	
	public GTSWelcomeTab() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(new JLabel(I18n.format("welcome.des")));
	}

}
