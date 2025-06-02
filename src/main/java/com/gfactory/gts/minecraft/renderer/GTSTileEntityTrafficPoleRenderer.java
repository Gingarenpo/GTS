package com.gfactory.gts.minecraft.renderer;

import com.gfactory.core.mqo.MQO;
import com.gfactory.core.mqo.MQOObject;
import com.gfactory.gts.minecraft.GTS;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficArm;
import com.gfactory.gts.minecraft.tileentity.GTSTileEntityTrafficPole;
import com.gfactory.gts.pack.GTSPack;
import com.gfactory.gts.pack.config.GTSTrafficArmConfig;
import com.gfactory.gts.pack.config.GTSTrafficPoleConfig;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class GTSTileEntityTrafficPoleRenderer extends GTSTileEntityRenderer<GTSTileEntityTrafficPole, GTSTrafficPoleConfig>{
    @Override
    public void renderModel(GTSTileEntityTrafficPole te, GTSPack pack, GTSTrafficPoleConfig config, Tessellator t, MQO model, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        // テクスチャの指定を行う
        ResourceLocation textureBase = pack.getOrCreateBindTexture(config.getTextures().getBase());

        if (textureBase == null) {
            // テクスチャが見つからないという緊急事態だが落とすのはまずいのでエラー出しまくる
            GTS.LOGGER.warn(I18n.format("gts.warning.texture_cannot_load", "Some Texture"));
            return; // 描画をしない
        }

        this.bindTexture(textureBase);

        // 接続部分を取得
        ArrayList<String> obj = config.getNormalObject();
        if (te.isUpJoint() && !te.isBottomJoint()) {
            // 上だけ
            ArrayList<String> obj2 = config.getUpJointObject();
            if (obj2 != null) obj = obj2;
        }
        else if (!te.isUpJoint() && te.isBottomJoint()) {
            // 下だけ
            ArrayList<String> obj2 = config.getBottomJointObject();
            if (obj2 != null) obj = obj2;
        }
        else if (te.isUpJoint() && te.isBottomJoint()) {
            // 下だけ
            ArrayList<String> obj2 = config.getFullJointObject();
            if (obj2 != null) obj = obj2;
        }

        for (MQOObject o: model.getObjects()) {
            if (obj.contains(o.getName())) o.draw();
        }

        GlStateManager.popMatrix();
        GlStateManager.pushMatrix(); // 回転をリセットする
        GL11.glTranslated(
                x + 0.5 + te.getPosX() * Math.cos(te.getAngle()) + te.getPosZ() * Math.sin(te.getAngle()),
                y + 0.5 + te.getPosY(),
                z + 0.5 + te.getPosZ() * Math.cos(te.getAngle()) + te.getPosX() * Math.sin(te.getAngle())
        ); // ブロックの原点を描画対象の座標に移動させる（ただしMQOの性質上原点を中心に移動させる）

        // アームがある場合、アームの数だけ描画を繰り返す
        for (GTSTileEntityTrafficArm arm: te.getJointArms()) {
            // アームのテクスチャを取得する
            GTSPack armPack = arm.getPack();
            if (armPack == null) continue;
            GTSTrafficArmConfig armConfig = (GTSTrafficArmConfig) arm.getConfig();
            if (armConfig == null || armConfig.getTextures() == null) continue;
            ResourceLocation armTex = armPack.getOrCreateBindTexture(armConfig.getTextures().getBase());
            if (armTex == null) continue;
            this.bindTexture(armTex);
            MQO armModel = armPack.getResizingModels(armConfig.getModel(), armConfig.getSize());
            if (armModel == null) continue;

            Vec3d startPos = new Vec3d(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
            Vec3d endPos = new Vec3d(arm.getPos().getX(), arm.getPos().getY(), arm.getPos().getZ());

            // 次のアームのために今の位置を保存
            GlStateManager.pushMatrix();

            // 回転方向を決めるため、方向ベクトルを算出
            GlStateManager.rotate(-90, 0, 1, 0); // Z基準にするため一度回す
            Vec3d delta = endPos.subtract(startPos).normalize();
            double length = Math.sqrt(Math.pow(startPos.x - endPos.x,2) + Math.pow(startPos.y - endPos.y, 2) + Math.pow(startPos.z - endPos.z, 2));

            // size * 2をlengthから引き、ベースが取るべき長さを算出する
            double baseLength = length - armConfig.getSize() * 2;

            // Y軸回転量（yaw）、X軸回転量（pitch）を算出
            double yaw = Math.toDegrees(Math.atan2(delta.x, delta.z));
            double pitch = Math.toDegrees(-Math.asin(delta.y));

            // Y軸回転し、次にX軸回転
            GlStateManager.rotate((float)yaw, 0, 1, 0);
            GlStateManager.rotate((float)pitch, 0, 0, 0);

            // 回した方向からX軸に対して+sizeの半分平行移動（こうしないとポールの中心にアームが突き刺さる）
            GlStateManager.translate(armConfig.getSize() / 2.0, 0, 0);

            // 先頭オブジェクトを描画（相当に長いか、短いけどスタートは必ず書く場合
            if (baseLength >= armConfig.getSize() || armConfig.isDrawStartPrimary()) {
                for (MQOObject o: armModel.getObjects()) {
                    if (armConfig.getEdgeObjects().contains(o.getName())) o.draw();
                }
            }

            // 次に、ベースオブジェクトを描画
            // 先っぽまで移動するのでサイズの半分移動し、その後さらにベースの半分移動して原点にする
            GlStateManager.translate(baseLength / 2f + armConfig.getSize() / 2f, 0, 0);
            // ベースの長さ/サイズ文拡大する
            GlStateManager.scale(baseLength / armConfig.getSize() * 1.2, 1, 1);
            // 描画
            if (baseLength >= 0) {
                for (MQOObject o: armModel.getObjects()) {
                    if (armConfig.getBaseObjects().contains(o.getName())) o.draw();
                }
            }

            // 最後を描画する
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-90, 0, 1, 0);
            GlStateManager.rotate((float)yaw, 0, 1, 0);
            GlStateManager.rotate((float)pitch, 0, 0, 0);

            // 先っぽまで移動するのでlength - size / 2する
            GlStateManager.translate(length - armConfig.getSize() / 2f, 0, 0);

            for (MQOObject o: armModel.getObjects()) {
                if (armConfig.getEndObjects().contains(o.getName())) o.draw();
            }

            // 取り出して元に戻す
            GlStateManager.popMatrix();
        }
    }





}
