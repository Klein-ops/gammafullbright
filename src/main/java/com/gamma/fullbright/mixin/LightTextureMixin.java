package com.gamma.fullbright.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 渲染核心：拦截 LightTexture.updateLightTexture 中决定亮度的插值因子
 * Math.max(0.0F, gamma - f3)（该调用在方法内是第 3 个 Math.max(FF)，ordinal=2）。
 *
 * 当 gamma 被 set 成 12（BetterSlider 允许）时，Math.max(0, 12-f3) 巨大，
 * lerp 后颜色被 clampColor 钳到全白 = 夜视效果；gamma<0 时返回 0 保持黑暗。
 * 原版 gamma=1 时 Math.max(0, 1-f3) 在暗处（f3≈1）只有 0，这就是原版不够亮的原因。
 */
@Mixin(LightTexture.class)
public class LightTextureMixin {
    @Redirect(
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 2),
            method = "updateLightTexture"
    )
    private float gammaFullbright$redirectGammaMax(float a, float b) {
        double gamma = Minecraft.getInstance().options.gamma().get();
        return (float) Math.max(gamma < 0 ? gamma : 0, b);
    }
}