package com.gamma.fullbright;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 伽马全亮配置 —— 持久化"亮度等级"状态。
 *
 * 配置文件：config/gammafullbright.toml（NeoForge 标准 TOML 格式）
 * 记录 level 状态（OFF / MEDIUM / FULL），可通过 NeoForge 模组菜单
 * （Mods → Config）调节，游戏启动时读取并自动应用。
 */
public class GammaFullbrightConfig {
    /** 配置构建器 */
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /**
     * 亮度等级：
     * - OFF：不启用（等于没开）
     * - MEDIUM：中等增强（看清路，不全亮）
     * - FULL：全亮（夜视药水级超亮）
     */
    public static final ModConfigSpec.EnumValue<BrightnessLevel> LEVEL = BUILDER
            .comment("亮度等级：OFF=关闭，MEDIUM=中等增强，FULL=全亮（夜视级）")
            .defineEnum("level", BrightnessLevel.OFF);

    /** 完整的配置规格 */
    public static final ModConfigSpec SPEC = BUILDER.build();
}