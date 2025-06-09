package com.gfactory.gts.tool.component;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

/**
 * プロジェクトを開いた時に左側に表示される、プロジェクトのツリービュー
 */
public class GTSSideTreeView extends JScrollPane {
	
	/**
	 * プロジェクトツリーを表示するモデル
	 */
	private JTree projectTree;
	
	/**
	 * プロジェクトツリーの実データ
	 */
	private DefaultTreeModel treeModel;
	
	public GTSSideTreeView() {
		init();
	}
	
	public void init() {
		// 自分自身の設定
		this.setPreferredSize(new Dimension(200, 0));
		
		// 初期セットアップ
		initTreeModel();
		this.setViewportView(projectTree);
	}
	
	/**
	 * 指定したファイル（ディレクトリ）をもとにしてツリーモデルを構築し、
	 * このツリーを更新する。
	 * 
	 * 指定したファイルがディレクトリではない場合は例外を投げる。
	 * @param file 基準となるプロジェクトディレクトリ
	 */
	public void createTreeModel(File file) throws IllegalArgumentException {
		// ルートディレクトリをまず追加する
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(file);
		treeModel = new DefaultTreeModel(rootNode);
		
		// 小ディレクトリを追加する
		this.searchDirectory(rootNode, file);
		
		// ツリーモデルを更新する
		this.treeModel.reload();
		
		// ツリーモデルを適用する
		this.projectTree.setModel(treeModel);
		
	}
	
	private void initTreeModel() {
		// 何もない空のツリーモデルを構築する
		treeModel = null;
		projectTree = new JTree();
		projectTree.setCellRenderer(new GTSFileTreeRender());
		projectTree.setRowHeight(32);
	}
	
	private void searchDirectory(DefaultMutableTreeNode node, File file) {
		// 再帰検索を行う
		if (file.isDirectory()) {
			// ファイルがディレクトリの場合は、その中身の全ファイルを確かめる
			File[] files = file.listFiles();
			if (files != null) {
				// ファイルの中身がある場合は、その中身の数だけ繰り返す
				for (File f: files) {
					// 子ノードを作成する
					DefaultMutableTreeNode child = new DefaultMutableTreeNode(f);
					node.add(child); // 子ノードを親ノードに追加する
					if (f.isDirectory()) {
						// 子要素がディレクトリの場合はそこも再帰的に検索する
						searchDirectory(child, f);
					}
				}
			}
		}
	}
	
	/**
	 * アイコンなどを合わせて描画するための特殊レンダラー
	 */
	private static class GTSFileTreeRender extends DefaultTreeCellRenderer {
		
		private Icon lightIcon = new ImageIcon(getClass().getResource("/tools/icons/light.png"));
		private Icon controllerIcon = new ImageIcon(getClass().getResource("/tools/icons/controller.png"));
		private Icon poleIcon = new ImageIcon(getClass().getResource("/tools/icons/pole.png"));

		/**
		 * 描画すべきcomponentを返す。引数はよくわからない
		 */
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			
			// 中身はツリーであることが保障されているのでキャスト
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Object userObject = node.getUserObject(); // この中に指定したファイルが入っている（はず）
			if (userObject instanceof File) {
				// きちんとファイルインスタンスだった場合
				File file = (File) userObject;
				this.setText(file.getName());
				
				// ファイルの拡張子によって分岐
				if (file.isDirectory()) {
					this.setIcon(controllerIcon);
				}
				else {
					this.setIcon(lightIcon);
				}
				
			}
			
			return c;
		}
		
		
	}

}
