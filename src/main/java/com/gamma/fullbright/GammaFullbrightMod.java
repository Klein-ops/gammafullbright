package com.gamma.fullbright;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import org.lwjgl.glfw.GLFW;

/**
 * 伽马全亮（超亮版）—— 纯客户端模组。
 *
 * 按 G 键：将亮度（gamma）设为 12（远超原版上限 1），配合 LightTextureMixin
 * 让光照插值因子爆表、暗处被钳到全白，达到夜视药水级别的超亮效果。
 * 再按一次 G 恢复原亮度。
 *
 * 开关状态持久化：通过 GammaFullbrightConfig 写入 config/gammafullbright.toml，
 * 游戏启动时自动恢复上次的开启/关闭状态（上把开启，下把仍是开启）。
 *
 * 实现参考 BetterBrightnessSlider 的核心（替换 gamma 值域 + 拦截渲染插值），
 * 但去掉了它的界面部分（onValueUpdate/toString/Sodium滑条 mixin）—— 那正是它崩溃的原因。
 */
@Mod(value = GammaFullbrightMod.MODID, dist = Dist.CLIENT)
public class GammaFullbrightMod {
    public static final String MODID = "gammafullbright";

    /** 超亮时的 gamma 值（BetterSlider 允许的最大值，渲染端产生夜视级亮度） */
    private static final double FULLBRIGHT_GAMMA = BetterSlider.MAX;
    /** 切换按键：G */
    private static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.gammafullbright.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.gammafullbright"
    );

    /** 当前是否处于超亮模式 */
    private boolean active = false;
    /** 记录开启超亮前的原始 gamma 值 */
    private double originalGamma = 0.5D;

    public GammaFullbrightMod(IEventBus modEventBus, ModContainer modContainer) {
        // 注册配置（TOML 文件，位于 config/gammafullbright.toml）
        modContainer.registerConfig(ModConfig.Type.CLIENT, GammaFullbrightConfig.SPEC);

        modEventBus.addListener(this::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggingIn);
        NeoForge.EVENT_BUS.addListener(this::onGameShuttingDown);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_KEY);
    }

    /** 玩家进入世界后，若配置为开启则自动恢复常亮（跨局持久化） */
    private void onPlayerLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return;
        }
        // 若配置为开启且当前未激活，则自动恢复超亮
        if (!active && GammaFullbrightConfig.ENABLED.get()) {
            originalGamma = mc.options.gamma().get();
            mc.options.gamma().set(FULLBRIGHT_GAMMA);
            active = true;
        }
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return;
        }
        while (TOGGLE_KEY.consumeClick()) {
            toggle(mc);
        }
    }

    private void toggle(Minecraft mc) {
        Options options = mc.options;
        if (!active) {
            originalGamma = options.gamma().get();
            options.gamma().set(FULLBRIGHT_GAMMA);
            active = true;
            GammaFullbrightConfig.ENABLED.set(true);
        } else {
            options.gamma().set(originalGamma);
            active = false;
            GammaFullbrightConfig.ENABLED.set(false);
            // 关闭时保存恢复后的 gamma 值到 options.txt
            options.save();
        }
        // 配置变更立即写入磁盘，保证下次启动能读到最新状态
        GammaFullbrightConfig.ENABLED.save();
    }

    private void onGameShuttingDown(GameShuttingDownEvent event) {
        // 退出时恢复 gamma 原值并保存 options.txt（不污染原版配置），
        // 但配置文件的 enabled 保持 true，下次启动自动恢复常亮。
        if (active) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                mc.options.gamma().set(originalGamma);
                mc.options.save();
                active = false;
            }
        }
    }
}