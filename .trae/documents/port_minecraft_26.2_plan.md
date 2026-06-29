# 移植到 Minecraft 26.2 计划

## 项目概述
- **项目名称**: Happy Ghast Shoot (happy-ghast-shoot)
- **模组类型**: Fabric 模组
- **当前版本**: Minecraft 26.1
- **目标版本**: Minecraft 26.2
- **构建工具**: Gradle + Fabric Loom

## 代码库调研结论

### 当前版本配置
- Minecraft: 26.1
- Fabric Loader: 0.18.5
- Fabric API: 0.144.3+26.1
- Fabric Loom: 1.15-SNAPSHOT
- Java: 25

### 模组功能
- 玩家骑乘 Happy Ghast（快乐恶魂）时可以发射火球
- 使用按键 G 触发射击
- 包含充能时间、冷却时间机制
- 使用 CustomPacketPayload 进行网络通信

### 源代码结构
- 主入口: `HappyGhastShoot.java` - 服务端逻辑
- 客户端入口: `HappyGhastShootClient.java` - 按键绑定和客户端逻辑
- 网络包: `ShootHappyGhastPayload.java` - 射击网络包
- Mixin: 示例 Mixin（`ExampleMixin.java`, `ExampleClientMixin.java`），未实际使用

## 需要修改的文件和模块

### 1. 配置文件（必改）
| 文件 | 修改内容 |
|------|----------|
| `gradle.properties` | 更新 `minecraft_version`, `loader_version`, `fabric_api_version`, `loom_version` |
| `src/main/resources/fabric.mod.json` | 更新 `minecraft` 依赖版本范围，更新 `fabricloader` 最低版本 |

### 2. 构建文件（可能需要）
| 文件 | 修改内容 |
|------|----------|
| `build.gradle` | 检查 Java 版本是否需要更新，检查 Loom API 变化 |

### 3. 源代码（可能需要）
| 文件 | 潜在修改点 |
|------|------------|
| `HappyGhastShoot.java` | 检查 `HappyGhast` 类 API 变化，`LargeFireball` 构造函数变化，网络 API 变化 |
| `HappyGhastShootClient.java` | 检查 `KeyMappingHelper` API 变化，`ClientPlayNetworking` 变化 |
| `ShootHappyGhastPayload.java` | 检查 `CustomPacketPayload` API 变化 |
| `ExampleMixin.java` | 检查 `MinecraftServer.loadLevel` 方法签名变化 |
| `ExampleClientMixin.java` | 检查 `Minecraft.run` 方法签名变化 |

## 移植步骤

### 第一步：更新版本配置
1. 从 Fabric 官方开发页面（https://fabricmc.net/develop）获取 Minecraft 26.2 的最新版本信息：
   - Fabric Loader 最新版本
   - Fabric API 对应 26.2 的最新版本
   - Fabric Loom 最新版本
2. 更新 `gradle.properties` 中的版本号

### 第二步：更新模组元数据
1. 更新 `fabric.mod.json` 中的 `minecraft` 依赖版本（`~26.1` → `~26.2`）
2. 更新 `fabricloader` 最低版本（如有必要）
3. 更新 `java` 版本要求（如 26.2 需要更高 Java 版本）

### 第三步：更新构建配置
1. 检查 `build.gradle` 中的 Java 版本设置（`sourceCompatibility`, `targetCompatibility`, `options.release`）
2. 如 Java 版本有变化，同步更新

### 第四步：编译并修复错误
1. 运行 `./gradlew build` 尝试编译
2. 根据编译错误修复源代码中的 API 不兼容问题
3. 可能的 API 变化点：
   - `HappyGhast` 实体类的包路径或方法变化
   - `LargeFireball` 构造函数参数变化
   - Fabric API 的网络包注册方式变化
   - 按键绑定 API 变化
   - Mixin 目标方法签名变化

### 第五步：测试验证
1. 运行游戏客户端测试模组功能
2. 验证骑乘 Happy Ghast 射击功能正常
3. 验证无运行时错误或崩溃

## 潜在依赖和考虑事项

### 版本对应关系
- Minecraft 26.2 可能需要更新版本的 Fabric Loader 和 Fabric API
- Java 版本可能需要从 25 升级到更高版本（如 26+）
- Fabric Loom 可能需要更新到支持 26.2 的版本

### API 破坏性变更风险
1. **实体类重构**: Minecraft 26.2 可能对 `HappyGhast` 或 `LargeFireball` 类进行重构
2. **网络 API 变化**: Fabric API 的网络模块可能有调整
3. **事件系统变化**: `ServerTickEvents` 或 `ClientTickEvents` 可能有 API 调整
4. **Mixin 目标变化**: Minecraft 内部方法签名可能变化
5. **按键绑定 API**: `KeyMappingHelper` 可能有变化

### Happy Ghast 实体风险
- 本模组核心依赖 `net.minecraft.world.entity.animal.happyghast.HappyGhast`
- 如果 26.2 版本中该实体被移除、重命名或重构，需要做较大改动
- 类似地，`LargeFireball` 的路径 `net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball` 也可能变化

## 风险处理

### 高风险：编译失败
- **应对**: 逐步修复，先解决版本配置问题，再逐个修复 API 不兼容
- **备选**: 如果某些 API 完全移除，查找替代方案或使用 Access Widener / Mixin 访问内部 API

### 中风险：运行时崩溃
- **应对**: 使用 Mixin 注入点调整，或使用 Fabric API 的兼容性层
- **工具**: 查看崩溃日志，定位具体问题类和方法

### 中风险：Happy Ghast 实体变更
- **应对**: 检查 26.2 的实体类结构，调整引用路径和方法调用
- **备选**: 如果实体被移除，可能需要用其他可骑乘实体替代或自行注册实体

### 低风险：功能行为变化
- **应对**: 调整参数或逻辑以匹配新版本行为
- **验证**: 游戏内测试确认功能正常

## 验证标准
- `./gradlew build` 编译成功，无错误
- 游戏客户端可以正常加载模组
- 骑乘 Happy Ghast 时按 G 键可以发射火球
- 无控制台错误或警告
- 服务端和客户端均正常工作
