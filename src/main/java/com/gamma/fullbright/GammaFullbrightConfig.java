package com.gamma.fullbright;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 伽马全亮配置 —— 持久化"开启/关闭"开关状态。
 *
 * 配置文件：config/gammafullbright.toml（NeoForge 标准 TOML 格式）
 * 记录 enabled 状态：开启常亮时为 true，关闭时为 false。
 * 游戏启动时读取该配置，若为 true 则自动恢复常亮，实现跨存档/跨局持久化。
 */
public class GammaFullbrightConfig {
    /** 配置构建器 */
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** 开关状态：true = 开启常亮（gamma 超亮），false = 关闭 */
    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("是否开启伽马全亮（常亮）模式。true = 开启，false = 关闭。")
            .define("enabled", false);

    /** 完整的配置规格 */
    public static final ModConfigSpec SPEC = BUILDER.build();
}
