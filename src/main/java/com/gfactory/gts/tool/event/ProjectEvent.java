package com.gfactory.gts.tool.event;

import java.io.File;

import javax.swing.JFileChooser;

import com.gfactory.gts.tool.GTSPackMaker;
import com.gfactory.gts.tool.helper.I18n;
import com.gfactory.gts.tool.project.GTSPackProject;

/**
 * プロジェクトオープン、作成、セーブ、その他もろもろの操作をメソッドとして格納したもの
 */
public class ProjectEvent {
	
	public static void createProject() {
		// プロジェクトディレクトリをどこにするかウィンドウ出す
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int result = chooser.showDialog(GTSPackMaker.window, I18n.format("open"));
		
		if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION) {
			// エラー、あるいはキャンセルの場合は無視
			return;
		}
		
		// ファイルを指定し、中身を確認
		File projectFile = chooser.getSelectedFile();
		
		// その場所をプロジェクトとして設置
		GTSPackMaker.project = new GTSPackProject(projectFile);
		GTSPackMaker.statusBar.setText(I18n.format("status.openProject", projectFile.getAbsolutePath()));
	}

}
