# GammaFullbright（伽马全亮）

一个为 Minecraft 1.21.1 **NeoForge** 开发的纯客户端模组：亮度**三档可调**，从基础增强到夜视药水级超亮，按 **G** 键循环切换。

> ⚠️ 注意：本模组仅支持 **NeoForge 加载器**（不是 Forge，也不是 Fabric）。

## 特性

- **三档亮度等级**，按 G 键循环切换：
  - **关闭（OFF）**：不干预 gamma，等于没开
  - **中等（MEDIUM）**：gamma 明显提升，能看清路但不至于全亮
  - **全亮（FULL）**：夜视药水级超亮
- 亮度等级可通过 **NeoForge 模组菜单**（Mods → Config → 伽马全亮）直接调节，改动实时生效
- 亮度等级**持久化**：上把调好的等级，下把进游戏自动恢复
- 切换时显示简洁提示（actionbar）
- 纯客户端模组，服务器无需安装
- 不修改存档、不影响联机

## 环境要求

| 项目 | 版本 |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.176 及以上 |
| Java | 21 |

## 使用

- 按 **G** 键：循环切换亮度等级（关 → 中 → 满 → 关）
- 可在「选项 → 控制 → 伽马全亮」中更改按键
- 在 **Mods → Config → 伽马全亮** 中可手动选择亮度等级

## 构建

需要 JDK 21 与 Gradle 8.x：

```bash
gradle build --no-daemon
```

构建产物位于 `build/libs/gammafullbright-<version>.jar`。

## 安装

1. 将 `gammafullbright-<version>.jar` 放入 `mods/` 目录
2. 启动游戏（确保已安装对应版本的 NeoForge）

## 技术实现

- 通过 `OptionInstanceMixin` 将原版 gamma 值域（0 ~ 1）替换为 `-1 ~ 12`（见 `BetterSlider`）
- 通过 `LightTextureMixin` 拦截光照纹理更新中的插值计算，使高 gamma 值在暗处被钳到全白，实现夜视级亮度
- 中等档使用适中的 gamma 值（3.0）提供温和增强，全亮档使用最大值（12）
- 配置使用 NeoForge `ModConfigSpec`（TOML），等级状态持久化到 `config/gammafullbright.toml`
- 参考了 BetterBrightnessSlider 的核心思路，但去除了其导致游戏设置界面崩溃的部分

## 开源协议

[MIT](LICENSE)
