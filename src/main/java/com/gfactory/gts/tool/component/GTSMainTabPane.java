package com.gfactory.gts.tool.component;

import javax.swing.JTabbedPane;

import com.gfactory.gts.tool.helper.I18n;

/**
 * エディタ画面として用意しておく、中央に表示するタブの部分
 */
public class GTSMainTabPane extends JTabbedPane {
	
	public GTSMainTabPane() {
		// 画面幅最大で取る
		this.setPreferredSize(getMaximumSize());
		
		// ようこそ画面を表示
		this.addTab(I18n.format("welcome"), new GTSWelcomeTab());
		
	}

}
