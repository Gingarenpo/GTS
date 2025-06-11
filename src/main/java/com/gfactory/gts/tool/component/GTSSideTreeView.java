package com.gfactory.gts.tool.component;

import static com.gfactory.gts.tool.GTSPackMakerConstants.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.gfactory.gts.tool.GTSPackMaker;
import com.gfactory.gts.tool.component.tab.GTSTabTextureView;
import com.gfactory.gts.tool.helper.I18n;

/**
 * プロジェクトを開いた時に左側に表示される、プロジェクトのツリービュー
 */
public class GTSSideTreeView extends JScrollPane implements MouseListener {
	
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
		
		// 表示
		this.projectTree.setVisible(true);
		
	}
	
	private void initTreeModel() {
		// 何もない空のツリーモデルを構築する
		treeModel = null;
		projectTree = new JTree();
		projectTree.setCellRenderer(new GTSFileTreeRender());
		projectTree.setRowHeight(20);
		projectTree.addMouseListener(this);
		projectTree.setVisible(false);

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
					this.setIcon(ICON_DIRECTORY);
				}
				else if (file.getName().endsWith(".mqo")) {
					this.setIcon(ICON_MODEL);
				}
				else if (file.getName().endsWith(".png") || file.getName().endsWith(".jpg")) {
					this.setIcon(ICON_TEXTURE);
				}
				else if (file.getName().endsWith(".json")) {
					this.setIcon(ICON_FILE);
				}
				else if (file.getName().endsWith(".ogg")) {
					this.setIcon(ICON_SOUND);
				}
				else {
					this.setIcon(ICON_CLOSE);
				}
				
			}
			
			return c;
		}
		
		
	}



	/**
	 * ツリーのノードがクリックされたときに、対象ファイルの場合はそれを開く。
	 * 対象ファイルではない場合はダイアログを出す。
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		// ダブルクリックでない場合は無視
		if (e.getClickCount() < 2) return;
		
		
		// 選択状態のツリーパスを取得
		TreePath path = this.projectTree.getPathForLocation(e.getX(), e.getY());
		if (path == null) return; // 未選択の場合は無視
		
		// 選択状態のツリーパスからそのノードを取得
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		
		// そのノードの持つファイルを参照
		Object f = node.getUserObject();
		if (!(f instanceof File)) return; // ファイルじゃない場合はスルー
		File file = (File) f;
		
		// ディレクトリの場合は無視
		if (file.isDirectory()) return;
		
		// TODO: 定数化してまとめたい
		if (file.getName().endsWith(".mqo")) {
			// MQOモデルファイルだった場合
		}
		else if (file.getName().endsWith(".png") || file.getName().endsWith(".jpg")) {
			// テクスチャファイルだった場合、テクスチャタブを開く
			JPanel panel = new GTSTabTextureView(file);
			GTSPackMaker.mainView.addTab(file.getName(), ICON_TEXTURE, panel, "");
			
		}
		else if (file.getName().endsWith(".json")) {
			// コンフィグファイルだった場合
		}
		else {
			// なんでもないファイルの場合、開けないよダイアログを出す
			JOptionPane.showMessageDialog(GTSPackMaker.window, I18n.format("message.unsupportFile", file.getName()), I18n.format("message.title.unsupportFile"), JOptionPane.ERROR_MESSAGE);
		}
	}

	
	//////////////////////////////////////////////
	// ここから空実装
	//////////////////////////////////////////////
	@Override
	public void mousePressed(MouseEvent e) {
		// NOOP
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// NOOP
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// NOOP
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// NOOP
		
	}

}
