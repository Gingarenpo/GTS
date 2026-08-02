package com.gfactory.gts.common.sign;

import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;

/**
 * 114-A・Bの地名板に対応した地名板の情報
 * 色とか枠線とか角丸とかその辺を入れている。
 */
public class GTS114Sign extends GTSSignBase {

    /**
     * 地名板は角丸か？（trueにすると山梨みたいな感じになる）
     */
    public boolean rounded = false;

    /**
     * 地名板は枠線があるか？
     */
    public boolean border = true;

    /**
     * この地名板は幅固定か？
     */
    public boolean widthFix = true;


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GTS114Sign)) return false;
        GTS114Sign that = (GTS114Sign) o;
        return rounded == that.rounded && border == that.border && widthFix == that.widthFix && Double.compare(aspect, that.aspect) == 0 && Objects.equals(japanese, that.japanese) && Objects.equals(english, that.english) && Objects.equals(color, that.color) && Objects.equals(textColor, that.textColor) && Objects.equals(japaneseFont, that.japaneseFont) && Objects.equals(englishFont, that.englishFont);
    }

    @Override
    public int hashCode() {
        return Objects.hash(japanese, english, color, textColor, japaneseFont, englishFont, rounded, border, widthFix, aspect);
    }

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.widthFix = compound.getBoolean("width_fix");
        this.rounded = compound.getBoolean("rounded");
        this.border = compound.getBoolean("border");
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound compound = super.writeToNBT();
        compound.setBoolean("width_fix", widthFix);
        compound.setBoolean("rounded", rounded);
        compound.setBoolean("border", border);

        return compound;
    }
}
