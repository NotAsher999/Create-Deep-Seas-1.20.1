# Create Deep Seas — Forge 1.20.1 移植

本分支把上游 Create Deep Seas 2.2.3 移植到 Minecraft 1.20.1、Forge
47.4.0 和 Java 17，并保留原来的单 JAR、三个 Mod ID：
`create_submarine`、`create_abyss`、`create_high_seas`。

行为基线为上游提交
`37b876cea7e06e6a3ea56fce998aff9c4a0d3984`。目前没有对应 2.2.3 的上游
成品 JAR，因此本工程以锁定的源码和历史为准，不声称与某个原包逐字节对应。

## 运行依赖

- Minecraft 1.20.1
- Forge 47.4.0
- Java 17
- Create 6.0.8
- Flywheel 1.0.5
- Ponder 1.0.91
- Veil 1.0.0.296
- [Sable 2.0.5-port.1](https://github.com/NotAsher999/sable-1.20.1)
- [Simulated/Aeronautics 1.3.1-port.1](https://github.com/NotAsher999/Simulated-Project-1.20.1)

Copycats+ 3.0.4、Embeddium 0.3.31、Oculus 1.8.0、Lithostitched 和 Fusion
是可选兼容项。精确版本和 SHA-256 见
[`docs/DEPENDENCY_MAP.md`](docs/DEPENDENCY_MAP.md)。

游戏实例只能安装 production JAR。带 `-dev`、`-sources` 等后缀的文件只供
编译或 IDE 使用，不能放进 `mods`。

## 当前状态

最终 Forge 工作区可独立构建。生产候选已在 PJ 多模组实例中完成进入世界、
Sable 子世界建立、F3+T 资源重载、暂停恢复、保存和正常退出；基础、Copycats、
Embeddium、Copycats+Embeddium 客户端组合以及基础/Copycats 专服启动也已验证。

证据边界和上游仍属 WIP 的内容见
[`docs/PORT_STATUS.md`](docs/PORT_STATUS.md) 与
[`docs/VALIDATION.md`](docs/VALIDATION.md)。

## 构建与恢复

开发构建：

```powershell
.\gradlew.bat clean build --no-daemon
```

提交并建立 annotated tag `v2.2.3-port.3` 后，运行 `build-release.bat` 可生成
经过校验的本地工程 checkpoint。普通 source ZIP 不含被忽略的第三方本地 JAR；
minimal-workspace ZIP 含精确依赖、Git bundle、恢复说明和全文件哈希。

## 许可边界

上游采用 All Rights Reserved。README 对贡献行为作了明确邀请，因此本工程以
贡献移植形式维护；公开发布编译包、再分发或上传平台仍需权利人单独授权。本地
构建和源码 checkpoint 不代表获得公开分发许可。
