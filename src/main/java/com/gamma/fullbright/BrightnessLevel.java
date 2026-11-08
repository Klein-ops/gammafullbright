package com.gamma.fullbright;

/**
 * 亮度等级（三档）。
 *
 * OFF    —— 最低级：不启用任何增强，等于没开。
 * MEDIUM —— 中等增强：gamma 明显提升，能看清路但不至于全亮。
 * FULL   —— 全亮：现有夜视药水级超亮效果。
 *
 * 通过 NeoForge 模组菜单（Mods → Config）调节，持久化到 config/gammafullbright.toml。
 */
public enum BrightnessLevel {
    OFF,
    MEDIUM,
    FULL;

    /** 循环到下一档：OFF → MEDIUM → FULL → OFF …… */
    public BrightnessLevel next() {
        BrightnessLevel[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}