package com.gamma.fullbright;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.client.OptionInstance;

import java.util.Optional;

/**
 * 大范围亮度值域（-1 ~ 12），替代原版 UnitDouble（0 ~ 1）。
 * 这是实现超亮的关键：让 Options.gamma 可以 set 超过 1 的值。
 */
public enum BetterSlider implements OptionInstance.SliderableValueSet<Double> {
    INSTANCE;

    /** 最小值：-1（原版 gamma 语义中负数=完全黑暗） */
    public static final double MIN = -1.0D;
    /** 最大值：12（BBS 用的值，插值因子足够让暗处被钳到全白 = 夜视效果） */
    public static final double MAX = 12.0D;

    @Override
    public double toSliderValue(Double value) {
        return (value - MIN) / (MAX - MIN);
    }

    @Override
    public Double fromSliderValue(double slider) {
        return slider * (MAX - MIN) + MIN;
    }

    @Override
    public Optional<Double> validateValue(Double value) {
        return value >= MIN && value <= MAX ? Optional.of(value) : Optional.empty();
    }

    @Override
    public Codec<Double> codec() {
        return Codec.either(Codec.doubleRange(MIN, MAX), Codec.BOOL)
                .xmap(either -> either.map(v -> v, b -> b ? 1.0D : 0.0D), Either::left);
    }
}