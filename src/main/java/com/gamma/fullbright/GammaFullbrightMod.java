package com.gamma.fullbright;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
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

    public GammaFullbrightMod(IEventBus modEventBus) {
        modEventBus.addListener(this::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
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
            toggle(mc);
        }
    }

    private void toggle(Minecraft mc) {
        Options options = mc.options;
        if (!active) {
            originalGamma = options.gamma().get();
            options.gamma().set(FULLBRIGHT_GAMMA);
            active = true;
        } else {
            options.gamma().set(originalGamma);
            active = false;
        }
        // 关闭时保存恢复后的值；开启时故意不保存，由退出事件兜底恢复
        if (!active) {
            options.save();
        }
    }

    private void onGameShuttingDown(GameShuttingDownEvent event) {
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