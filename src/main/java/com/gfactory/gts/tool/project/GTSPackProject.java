package com.gfactory.gts.tool.project;

import java.io.File;

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
	
	public GTSPackProject(File directory) {
		this.projectDirectory = directory;
	}
	
	public File getProjectDirectory() {
		return this.projectDirectory;
	}
	
	public void setProjectDirectory(File directory) {
		this.projectDirectory = directory;
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

}
