# GammaFullbright（伽马全亮）

一个为 Minecraft 1.21.1 **NeoForge** 开发的纯客户端模组：按 **G** 键一键将游戏亮度提升至夜视药水级别（超亮），再按一次恢复原亮度。

> ⚠️ 注意：本模组仅支持 **NeoForge 加载器**（不是 Forge，也不是 Fabric）。

## 特性

- 一键切换常亮 / 恢复，超亮效果可达夜视药水级别
- 切换时无文字提示，干净利落
- 纯客户端模组，服务器无需安装
- 不修改存档、不影响联机

## 环境要求

| 项目 | 版本 |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.176 及以上 |
| Java | 21 |

## 使用

- 按 **G** 键：开启 / 关闭常亮（伽马全亮）
- 可在「选项 → 控制 → 伽马全亮」中更改按键

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
- 参考了 BetterBrightnessSlider 的核心思路，但去除了其导致游戏设置界面崩溃的部分

## 开源协议

[MIT](LICENSE)
