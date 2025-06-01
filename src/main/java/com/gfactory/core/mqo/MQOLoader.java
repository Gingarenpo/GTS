package com.gfactory.core.mqo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MQOファイルを読み込むためのローダークラス。
 * 以前はMQOクラスのコンストラクタで管理していたが、
 * 今回はローダークラスをStaticとして分離することにした。
 */
public final class MQOLoader {
		
	/**
	 * ローダーはstaticのヘルパーメソッドとして使用されることを想定しているため、
	 * インスタンスの作成を禁止する。
	 */
	private MQOLoader() {}
	
	/**
	 * MQOファイルをInputStreamとして渡し、その中身を読み込む。
	 * MQOファイルとして不正なファイルと判断された場合は、読み込みを中止し例外を投げる。
	 * createThumbnailをtrueにする場合、サムネイルチャンクの内容が読み込まれる。通常は無視。
	 * 
	 * @param is MQO
	 * @return 読み込んだMQO。
	 * @throws IOException 読み込めなかったとき
	 * @throws MQOException 不正なファイルを指定したとき
	 */
	public static MQO load(InputStream is, boolean createThumbnail) throws IOException, MQOException {
		try (Scanner s = new Scanner(is)) {
			return MQOLoader.parse(s, createThumbnail); // パース結果をもとにMQOファイルを返す
		}
	}
	
	/**
	 * MQOファイルをInputStreamとして渡し、その中身を読み込む。
	 * MQOファイルとして不正なファイルと判断された場合は、読み込みを中止し例外を投げる。
	 * 通常はこのメソッドを使用して作成すること
	 * 
	 * @param is MQO
	 * @return 読み込んだMQO
	 * @throws IOException 読み込めなかったとき
	 * @throws MQOException 不正なファイルを指定したとき
	 */
	public static MQO load(InputStream is) throws IOException, MQOException {
		return MQOLoader.load(is, false);
	}
	
	/**
	 * MQOファイルをByte配列として渡し、その中身を読み込む。
	 * ZipEntryの読み込みの場合、中身をそのまま読み取ることが難しいので、その代用。
	 * 結局ByteArrayInputStreamでラップしているため、可能であればInputstream直接読み込む。
	 * 
	 * @param data MQO
	 * @return 読み込んだMQO
	 * @throws IOException 読み込めなかったとき
	 * @throws MQOException 不正なファイルを指定したとき
	 */
	public static MQO load(byte[] data) throws IOException, MQOException {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
			return MQOLoader.load(bais);
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	
	
	private static MQO parse(Scanner s, boolean createThumbnail) throws MQOException, IOException {
		// チャンク名の読み取りに使用するパターン
		Pattern chunkPattern = Pattern.compile("^(.+?) (.+ ?)*\\{$");

		// 面の読み取りに使用するパターン
		Pattern facePattern = Pattern.compile("^(\\d+) V\\(([0123456789 ]+)\\) M\\(([0123456789 ]+)\\) UV\\(([0123456789.e+\\- ]+)\\)$");
		
		// 現在見ているチャンクを扱う
		Deque<Chunk> chunkQueue = new ArrayDeque<>();
		
		// 二次元チャンクになっているので保持のためObjectを扱う
		MQOObject object = null;

		// サムネイルデータ（W*H）を保持するためのデータ
		int[][] rawThumbnail = new int[0][];

		// サムネイル一時格納データ
		StringBuilder rawThumbnailString = new StringBuilder();
		
		// 返却するMQO
		MQO mqo = new MQO();
		
		while (s.hasNextLine()) {
			// 1行取得
			String line = s.nextLine().trim().replace("\t", "");
			
			if (line.startsWith("}")) {
				// チャンク終了なので、見ているチャンクをもとに戻す
				// 存在しない場合を考慮して例外は出さないようにする
				Chunk c = chunkQueue.pollLast();
				if (Chunk.Object.equals(c)) {
					// オブジェクトを追加して閉じる
					mqo.getObjects().add(object);
					object = null;
				}
			}
			else if (line.endsWith("{")) {
				// チャンクの始まりを記すため、このチャンクの情報を取得する
				Matcher m = chunkPattern.matcher(line.trim());
				if (!m.matches()) continue; // マッチしない場合はおかしいので無視
				
				Chunk c = Chunk.toChunk(m.group(1));
				if (c == null || (c.equals(Chunk.Thumbnail) && !createThumbnail)) continue; // 無視するチャンク
				
				// チャンクの始まりをキューに通達
				chunkQueue.offer(c);

				if (c.equals(Chunk.Object)) {
					object = new MQOObject(m.group(2).replace("\"", "").trim());
				}
				else if (c.equals(Chunk.Thumbnail)) {
					// サムネイルからメタ情報を取得
					// w h bit rgb raw
					String[] meta = m.group(2).split(" ");
					// 流石にここはオプションなので整数であることを前提とする
					rawThumbnail = new int[Integer.parseInt(meta[0])][Integer.parseInt(meta[1])];
				}
			}
			else if (chunkQueue.peekLast() != null) {
				// チャンク内部であるため、各チャンクによって内容を分岐
				// vertexとfaceはobjectに内包されているのでこいつを前提とするのが厄介
				switch (chunkQueue.peekLast()) {
				case Object:
					// facet=スムージング角度なのでそれを代入する
					if (line.startsWith("facet")) {
						String smoothing = line.split(" ")[1];
						try {
							object.setSmoothing(Math.toRadians(Double.parseDouble(smoothing)));
						} catch (NumberFormatException e) {
							System.out.println("Warning: could not read smoothing");
						}
					}
					break;
				case Vertex:
					// 頂点情報（半角スペース3つ区切り）
					String[] v = line.trim().split(" "); // 半角スペースで区切る
					if (v.length != 3) throw new MQOException("Vertex has over 4 position.");
					MQOVertex vertex = new MQOVertex(Double.parseDouble(v[0]), Double.parseDouble(v[1]), Double.parseDouble(v[2]));
					if (object == null) throw new MQOException("Vertex is not included in Object Chunk.");
					object.getVertexs().add(vertex);
					break;
				case Face:
					// 面情報を取得
					if (object == null) throw new MQOException("Face is not included in Object Chunk.");
					Matcher fm = facePattern.matcher(line);
					if (!fm.matches()) break; // なぜか空文字が引っかかることがあるので
					// 頂点情報、マテリアル情報、UV情報を取り出す
					String[] vs = fm.group(2).split(" ");
					int[] vs2 = new int[vs.length];
					for (int i = 0; i < vs.length; i++) {
						vs2[i] = Integer.parseInt(vs[i]);
					}
					String[] uvs = fm.group(4).split(" ");
					double[][] uvs2 = new double[uvs.length][2];
					for (int i = 0; i < vs.length; i++) {
						uvs2[i] = new double[] {Double.parseDouble(uvs[i*2]), Double.parseDouble(uvs[i*2+1])};
					}
					int material = Integer.parseInt(fm.group(3));
					int vLength = Integer.parseInt(fm.group(1)); // 頂点数
					if (vs.length != vLength || uvs.length != vLength * 2) throw new MQOException("Face has wrong vertexs or uvs");
					// 面情報を追加する
					MQOFace face = new MQOFace(material, uvs2, vs2, vs.length);
					object.getFaces().add(face);
					break;
				case Material:
					// 現在未対応
					break;
				case Thumbnail:
					rawThumbnailString.append(line);
					break;
				default:
				}
			}
			
		}

		// サムネイル情報のパースを行う
		if (createThumbnail) {
            int w = 0;
			for (int i = 0; i < rawThumbnailString.length(); i+=6) {
				// 6文字ずつ読み込んでRGBの値とする
				String rgb = rawThumbnailString.substring(i, i+6);
				rawThumbnail[Math.min(i / 6 % rawThumbnail[0].length, rawThumbnail[0].length-1)][w / 128] = Integer.parseInt(rgb, 16);
				w++;
			}
			mqo.createThumbnailImage(rawThumbnail);
		}
		
		// 不要なメモリは解放する
		chunkQueue.clear();
		
		return mqo;
	}
	
	
	/**
	 * 各チャンクの開始終了を表すためのENUM。
	 * 文字列だと色々めんどくさいのでこうする。
	 */
	private enum Chunk {
		Object("Object"),
		Vertex("vertex"),
		Face("face"),
		Material("Material"),
		Thumbnail("Thumbnail");
		
		final String line;

		Chunk(String line) {
			this.line = line;
		}
		
		public String getName() {
			return line;
		}
		
		/**
		 * valueOfはないと例外出すので、その代わり
		 * @param name
		 * @return
		 */
		public static Chunk toChunk(String name) {
			for (Chunk c: Chunk.values()) {
				if (c.line.equals(name)) return c;
			}
			return null;
		}
		
	}

}
