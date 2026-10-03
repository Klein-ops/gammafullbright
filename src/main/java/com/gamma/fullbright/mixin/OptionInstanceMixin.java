package com.gamma.fullbright.mixin;

import com.gamma.fullbright.BetterSlider;
import com.mojang.serialization.Codec;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 把 gamma 的 OptionInstance 值域从 0~1 换成 -1~12（BetterSlider），
 * 这样 set(12) 才能通过 validateValue 校验，渲染端才能产生超亮效果。
 *
 * 注意：只替换 values 字段，绝不碰 onValueUpdate / toString ——
 * BBS 的崩溃正是因为它把 onValueUpdate 换成了递归调用 set() 的方法导致栈溢出。
 */
@Mixin(OptionInstance.class)
public class OptionInstanceMixin {
    @Shadow
    @Final
    Component caption;

    @Shadow
    @Final
    @Mutable
    private OptionInstance.ValueSet<Double> values;

    @Shadow
    @Final
    @Mutable
    private Codec<Double> codec;

    @Inject(at = @At("RETURN"), method = "<init>*", remap = false)
    private void gammaFullbright$replaceGammaValueSet(CallbackInfo info) {
        if (this.caption.getContents() instanceof TranslatableContents translatableContents
                && translatableContents.getKey().equals("options.gamma")) {
            this.values = BetterSlider.INSTANCE;
            this.codec = this.values.codec();
        }
    }
}