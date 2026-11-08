package com.gamma.fullbright;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import org.lwjgl.glfw.GLFW;

/**
 * 伽马全亮 —— 纯客户端模组，亮度三档可调。
 *
 * 按 G 键在三个亮度等级间循环：
 *   关闭（OFF）—— 不干预 gamma，等于没开；
 *   中等（MEDIUM）—— gamma 明显提升，能看清路但不至于全亮；
 *   全亮（FULL）—— gamma 拉到 12，夜视药水级超亮。
 *
 * 亮度等级持久化到 config/gammafullbright.toml，可通过 NeoForge 模组菜单
 * （Mods → Config → 伽马全亮）调节，改动会实时生效，下次启动自动恢复。
 */
@Mod(value = GammaFullbrightMod.MODID, dist = Dist.CLIENT)
public class GammaFullbrightMod {
    public static final String MODID = "gammafullbright";

    /** 中等档 gamma 值：明显提亮（看清路），但不会钳到全白 */
    private static final double MEDIUM_GAMMA = 3.0D;
    /** 全亮档 gamma 值（BetterSlider 允许的最大值，渲染端产生夜视级亮度） */
    private static final double FULL_GAMMA = BetterSlider.MAX;

    /** 切换按键：G */
    private static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.gammafullbright.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.gammafullbright"
    );

    /** 当前生效的亮度等级（初始从配置读取） */
    private BrightnessLevel currentLevel = BrightnessLevel.OFF;
    /** 记录应用亮度前的原始 gamma 值（退出/关闭时恢复） */
    private double originalGamma = 0.5D;
    /** 是否已进入世界（进入后配置变更实时生效，未进入则等待进入时应用） */
    private boolean inGame = false;

    public GammaFullbrightMod(IEventBus modEventBus, ModContainer modContainer) {
        // 注册配置（TOML 文件，位于 config/gammafullbright.toml）
        modContainer.registerConfig(ModConfig.Type.CLIENT, GammaFullbrightConfig.SPEC);

        modEventBus.addListener(this::registerKeyMappings);
        // 配置加载/重载事件（mod 总线）：模组菜单改动后实时应用
        modEventBus.addListener(this::onConfigReload);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggingIn);
        NeoForge.EVENT_BUS.addListener(this::onGameShuttingDown);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_KEY);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return;
        }
        while (TOGGLE_KEY.consumeClick()) {
            cycleLevel(mc);
        }
    }

    /** 玩家进入世界后，按当前配置等级应用亮度（跨局持久化） */
    private void onPlayerLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return;
        }
        inGame = true;
        currentLevel = GammaFullbrightConfig.LEVEL.get();
        applyLevel(mc);
    }

    /** 配置重载（含模组菜单改动）时实时应用新等级 */
    private void onConfigReload(ModConfigEvent event) {
        if (event.getConfig().getSpec() != GammaFullbrightConfig.SPEC) {
            return;
        }
        currentLevel = GammaFullbrightConfig.LEVEL.get();
        if (inGame) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.player != null) {
                applyLevel(mc);
            }
        }
    }

    /** 按 G 循环：OFF → MEDIUM → FULL → OFF …… */
    private void cycleLevel(Minecraft mc) {
        currentLevel = currentLevel.next();
        GammaFullbrightConfig.LEVEL.set(currentLevel);
        GammaFullbrightConfig.LEVEL.save();
        applyLevel(mc);
        mc.player.displayClientMessage(Component.literal("亮度：" + levelText(currentLevel)), true);
    }

    /** 应用当前等级对应的 gamma 值 */
    private void applyLevel(Minecraft mc) {
        Options options = mc.options;
        double current = options.gamma().get();
        if (currentLevel == BrightnessLevel.OFF) {
            // 关闭：仅当 gamma 不在原版值域（说明被本模组改过）时才恢复玩家原值，
            // 若本来就是原版值则完全不干预，避免覆盖玩家自己设置的亮度
            if (current > 1.0D) {
                options.gamma().set(originalGamma);
                options.save();
            }
        } else {
            // 仅当当前 gamma 还是玩家原值（原版值域 0~1）时才记录，
            // 避免从 MEDIUM 切到 FULL 时把中间值误记为"原始亮度"
            if (current <= 1.0D) {
                originalGamma = current;
            }
            double target = currentLevel == BrightnessLevel.MEDIUM ? MEDIUM_GAMMA : FULL_GAMMA;
            options.gamma().set(target);
        }
    }

    /** 等级对应的简洁提示文字 */
    private static String levelText(BrightnessLevel level) {
        return switch (level) {
            case OFF -> "关";
            case MEDIUM -> "中";
            case FULL -> "满";
        };
    }

    private void onGameShuttingDown(GameShuttingDownEvent event) {
        // 退出时恢复 gamma 原值并保存 options.txt（不污染原版配置），
        // 但配置文件的 level 保持不变，下次启动自动恢复对应等级。
        if (currentLevel != BrightnessLevel.OFF) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                mc.options.gamma().set(originalGamma);
                mc.options.save();
            }
        }
        inGame = false;
    }
}