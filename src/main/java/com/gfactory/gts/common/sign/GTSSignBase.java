package com.gfactory.gts.common.sign;

import com.gfactory.gts.common.IGTSNBTSerializable;
import net.minecraft.nbt.NBTTagCompound;

import java.awt.*;

/**
 * GTS内で作成することのできる地名板や標示板に関する情報を保存するインスタンス。
 * 元々GTS114Signのみがこの担当だったが、色々な標示板に対応する関係上ベースを作ることにした。
 * その結果、中身が結構変わってしまっているが気にしたら負けだと思う。
 */
public class GTSSignBase implements IGTSNBTSerializable {

    /**
     * 標示板の背景色（バックグラウンドカラー）
     */
    public Color color = new Color(255, 255, 255);

    /**
     * 標示板の文字色（プライマリーカラー）
     */
    public Color textColor = new Color(0, 0, 255);

    /**
     * 標示板のアスペクト比
     */
    public double aspect = 2.0;

    /**
     * 標示板の日本語部分のフォント
     */
    public String japaneseFont = "A-SK Nar Min2 E";

    /**
     * 標示板の英語部分のフォント（使わない場合もある）
     */
    public String englishFont = "Helvetica Neue";

    /**
     * 日本語部分
     */
    public String japanese = "生成中";

    /**
     * 英字部分
     */
    public String english = "Generating";

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.color = new Color(compound.getInteger("color"));
        this.textColor = new Color(compound.getInteger("text_color"));
        this.japanese = compound.getString("japanese");
        this.english = compound.getString("english");
        this.japaneseFont = compound.getString("japanese_font");
        this.englishFont = compound.getString("english_font");
        this.aspect = compound.getDouble("aspect");
    }

    @Override
    public NBTTagCompound writeToNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("color", color.getRGB());
        compound.setInteger("text_color", textColor.getRGB());
        compound.setString("japanese", japanese);
        compound.setString("english", english);
        compound.setString("japanese_font", japaneseFont);
        compound.setString("english_font", englishFont);
        compound.setDouble("aspect", aspect);
        return compound;
    }
}
