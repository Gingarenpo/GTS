package com.gfactory.gts.tool.project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import com.gfactory.gts.tool.GTSPackMaker;
import com.gfactory.gts.tool.helper.I18n;

/**
 * プロジェクトとしてディレクトリを読み込むためのもの。
 * ディレクトリを指定するとその中身を読み込んで、プロジェクト読み込みなどを行う。
 * 
 * 将来的に何かしらのメタ情報を保存するようにしたいが、今はただのJSONエディターなので…
 */
public class GTSPackProject {
	
	/**
	 * このプロジェクトのディレクトリの場所。必ず存在するはずだが、
	 * 作成後に削除されている場合もあるので存在チェックは必ず行う。
	 */
	private File projectDirectory;
	
	private ArrayList<File> projectFiles = new ArrayList<File>();
	
	/**
	 * ファイルスキャン中かどうか
	 */
	private boolean scanning = false;
	
	public GTSPackProject(File directory) {
		this.projectDirectory = directory;
		scanFiles();
	}
	
	public File getProjectDirectory() {
		return this.projectDirectory;
	}
	
	public void setProjectDirectory(File directory) {
		this.projectDirectory = directory;
		scanFiles();
	}
	
	public ArrayList<File> getProjectFiles() {
		return this.projectFiles;
	}
	
	/**
	 * このプロジェクトが物理的に存在しているかどうかを表す。
	 * 将来的にメモリに蓄えたプロジェクトを想定しているので用意しているが、
	 * 現状は単にパスにディレクトリが存在しているかのチェックにとどまる。
	 * @return
	 */
	public boolean isProjectExists() {
		return this.projectDirectory.exists();
	}
	
	private void scanFiles() {
		// ※スキャンはGUIスレッドでは行わない
		ScanTask task = new ScanTask(this);
		if (!this.scanning) task.execute();
		
	}

	private static class ScanTask extends SwingWorker<Void, Integer> {
		
		private GTSPackProject project;
		
		public ScanTask(GTSPackProject project) {
			this.project = project;
			this.project.projectFiles.clear();
			this.addPropertyChangeListener((e) -> {
				if ("progress".equals(e.getPropertyName())) {
					GTSPackMaker.progressBar.setValue((int) e.getNewValue());
				}
			});
		}

		@Override
		protected Void doInBackground() throws Exception {
			this.project.scanning = true;
			// スキャンを行う
			this.scanFiles(this.project.projectDirectory);
			
			return null;
		}
		
		private void scanFiles(File file) {
			if (file.isDirectory()) {
				// 内部の探索
				File[] files = file.listFiles();
				if (files == null) return;
				publish(files.length);
				for (File f: files) {
					scanFiles(f);
				}
			}
			else {
				// ファイルの場合はそれを登録する
				publish(0);
				this.project.projectFiles.add(file);
			}
		}

		@Override
		protected void process(List<Integer> chunks) {
			GTSPackMaker.progressBar.setValue(this.project.projectFiles.size());
			int max = 0;
			for (Integer chunk: chunks) {
				max += chunk;
			}
			System.out.println(max);
		}

		@Override
		protected void done() {
			super.done();
			this.project.scanning = false;
			GTSPackMaker.progressBar.setValue(100);
			GTSPackMaker.statusBar.setText(I18n.format("status.openProject", this.project.projectDirectory.getAbsolutePath()));
		}
		
	}
}
