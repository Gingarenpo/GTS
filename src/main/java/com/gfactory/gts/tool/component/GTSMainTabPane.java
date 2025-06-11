package com.gfactory.gts.tool.component;

import static com.gfactory.gts.tool.GTSPackMakerConstants.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
	
	@Override
	public void addTab(String title, Icon icon, Component c, String tip) {
		super.addTab(title, icon, c, tip);
		
		// 追加したタブのインデックスを確認
		int index = this.indexOfComponent(c);
		
		// タブのレイアウトを決めるJPanelを作成
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		p.setOpaque(false);
		
		// タブのラベルを示すJLabelを作成
		JLabel l = new JLabel(title);
		
		// タブを閉じるボタンを示すJButtonを作成
		JButton b = new JButton(ICON_CLOSE);
		b.setPreferredSize(new Dimension(20, 20));
		b.setOpaque(true);
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setRolloverIcon(ICON_CLOSE_OVER);
		b.setMargin(new Insets(0, 5, 0, 0));
		
		// 閉じるボタンを押すと閉じる
		b.addActionListener((e) -> {
			int i = this.indexOfComponent(c);
			if (i != -1) this.remove(i);
		});
		
		// JPanelに追加
		p.add(l);
		p.add(b);
		
		// この要素に置き換え
		this.setTabComponentAt(index, p);
		
		// この要素をフォーカス
		this.setSelectedIndex(index);
	}
	
	@Override
	public void addTab(String title, Component c) {
		this.addTab(title, null, c, "");
	}
	
	@Override
	public void addTab(String title, Icon icon, Component c) {
		this.addTab(title, icon, c, "");
	}

}
