package com.gfactory.gts.tool.event;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

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
		GTSPackMaker.statusBar.setText(I18n.format("status.scanProject", projectFile.getAbsolutePath()));
		GTSPackMaker.sideView.createTreeModel(projectFile);
		GTSPackMaker.menuBar.revalidate();
	}
	
	public static void importProject() {
		// インポートするプロジェクトディレクトリを選択
		JFileChooser chooser = new JFileChooser();
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(new FileNameExtensionFilter(I18n.format("file.zip"), "zip"));
		int result = chooser.showDialog(GTSPackMaker.window, I18n.format("open"));
		
		if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION) {
			// エラー、あるいはキャンセルの場合は無視
			return;
		}
		
		// ファイルの中身をZipとしてみなす
		JOptionPane.showMessageDialog(GTSPackMaker.window, "開発中です");
	}
	
	public static void exportPack() {
		// エクスポートするプロジェクトディレクトリを選択
		JFileChooser chooser = new JFileChooser();
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(new FileNameExtensionFilter(I18n.format("file.zip"), "zip"));
		int result = chooser.showDialog(GTSPackMaker.window, I18n.format("save"));
		
		if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION) {
			// エラー、あるいはキャンセルの場合は無視
			return;
		}
		
		// ファイルを保存する
		File file = chooser.getSelectedFile();
		try (FileOutputStream fos = new FileOutputStream(file)) {
			try (ZipOutputStream zos = new ZipOutputStream(fos)) {
				writeZipEntries(GTSPackMaker.project.getProjectDirectory(), GTSPackMaker.project.getProjectDirectory(), zos);
			}
		} catch (IOException e) {
			// エラー発生。
			JOptionPane.showMessageDialog(GTSPackMaker.window, I18n.format("message.failed", "エクスポート", e.getLocalizedMessage()), I18n.format("message.title.failed"), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * ファイルを再帰的に検索して、中身をZipファイル圧縮する
	 * @param root ルートとなるパス（プロジェクトディレクトリ）
	 * @param file Zipにするファイル
	 * @param zos Zipのストリーム
	 */
	private static void writeZipEntries(File root, File file, ZipOutputStream zos) throws IOException {
		if (file.isDirectory()) {
			// ディレクトリの場合はサブディレクトリ・ファイルを取得して再帰処理
			File[] files = file.listFiles();
			if (files == null) return;
			for (File child: files) {
				writeZipEntries(root, child, zos);
			}
		}
		else {
			// ファイルの場合はZip圧縮
			// ルートディレクトリからの相対パスを取得
			String name = root.toURI().relativize(file.toURI()).getPath();
			
			// 指定したエントリー名でZipファイルに登録
			zos.putNextEntry(new ZipEntry(name));
			
			// 実際の内容を書き込んでいく
			try (FileInputStream fis = new FileInputStream(file)) {
				byte[] buffer = new byte[4096];
				int read;
				while ((read = fis.read(buffer)) != -1) {
					zos.write(buffer, 0, read);
				}
			}
			
			// エントリーを閉じる
			zos.closeEntry();
		}
	}

}
