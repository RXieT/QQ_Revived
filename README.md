# QQ Revived

QQ Revived 是一个基于 libxposed 的 Android 模块，目标宿主为 `com.tencent.qqlite`。项目主要接管 QQ Lite / Watch 风格宿主界面的首页和聊天页，以 Compose + Miuix 重绘核心交互，同时通过 Hook 层桥接宿主数据、点击事件、头像加载、消息发送和表情能力。

## 界面设计

![界面](./img/screen.jpg)

## 功能概览

- 首页 Compose 重绘：消息、联系人、动态、我的页面。
- 聊天页 Compose 重绘：消息列表、输入栏、表情面板、图片/视频/文件/语音预览。
- 宿主能力复用：复用宿主头像、消息点击、长按、发送文本、发送表情、加载历史消息等能力。
- 全局界面适配：Edge-to-edge、状态栏/导航栏透明、宿主 Watch UI 背景修正。
- 兼容 Xposed 注入场景：自建 `LifecycleOwner`、`SavedStateRegistryOwner`、`ViewModelStoreOwner` 支撑宿主 View 中注入 Compose。


## 宿主
[📱 下载 QQ手表版 9.0.5](/apk/QQ手表版_9.0.5.apk)


## 目标环境

- Android Gradle Plugin 9.2.1
- Kotlin 2.4.10 / Compose 1.12.0-rc01
- compileSdk 37 / targetSdk 37 / minSdk 33（Android 13+）
- Miuix UI / blur 0.9.4-rc01，与 Hyper-PiliPlus Android 原生层对齐
- libxposed API 102，模块 `minApiVersion=101`
- 目标宿主包名：`com.tencent.qqlite`

## Vector / Android 17 兼容性排查

本分支调整了 Hook 初始化流程，但尚未完成编译及 Vector / Android 17 真机验证：

- 系统 Hook 在包加载阶段安装；宿主 Hook 等到 `onPackageReady`，使用最终 ClassLoader。
- 单个 Hook 安装抛错时记录 `QQRevived` 日志并继续其他 Hook；失败项不自动重试，避免部分安装后的重复拦截。
- 禁用自动与手动热重载，并移除模块入口对 API 102 热重载参数类型的引用。原实现只卸载旧 Hook，而框架不会在热重载后重放包生命周期回调；已有 Compose View 和宿主回调也未安全清理。
- 更新模块后，请**强行停止 QQ 手表版再打开**（包括后台进程）；仅划掉最近任务不一定有效。

验证时请记录 Vector 版本及模块管理页显示的 API、Android 系统版本、QQ 版本。目标宿主仍为 **QQ 手表版 9.0.5 / `com.tencent.qqlite`**，不是普通 QQ。

建议验证顺序：

1. 构建前执行 `:app:compileDebugKotlin` 与 `:app:lintDebug`，再生成 APK。
2. 在 Vector 中启用模块并确认宿主作用域，冷启动 QQ，检查 `QQRevived` 加载日志及各项 Hook 的失败日志。
3. 测试首页、聊天、返回、键盘弹出/收起、旋转与前后台切换。
4. 覆盖更新模块后强行停止 QQ 再启动，确认界面和消息收发仍正常。
5. 若闪退，提供框架日志和复现时的完整异常堆栈；不要只截最后一行。分享前请遮盖账号、消息内容等隐私。

libxposed 的回调与热重载契约见 [API 102 源码](https://github.com/libxposed/api/blob/102.0.0/api/src/main/java/io/github/libxposed/api/XposedModuleInterface.java)。框架 API 版本与 Android SDK 版本是两回事，不能仅凭 Android 17 推断框架支持 API 102。

## 本分支的源码恢复与玻璃组件

上游 `cefec2e` / `5d41bbd` 引用了未提交的公共组件、主题、设置和窗口适配源码，干净检出无法编译。本分支从 `9570e8b` 恢复可追溯的头像、图片请求和状态组件，其余按现有调用契约重新实现；重建界面不保证与原版截图完全一致。

- 主题同时提供 Miuix / Material 配色，支持系统/浅色/深色、三种强调色及玻璃总开关。玻璃折射与磨砂模糊各有关闭/低/中/高四档，独立组合、即时生效并持久化；默认低折射 + 中磨砂。磨砂半径为 0/8/20/36dp，玻璃等级调整边缘折射与高光而不是整体透明度。总开关关闭时保留等级选择，两项均关闭时不采样背景。高等级的 GPU 开销与效果需实机评估。
- QQ 内“我的 → 模块设置”与模块管理页分别保存到各自应用的私有偏好，**不跨应用同步**；QQ 的设置入口不再依赖宿主动作列表非空。
- 首页浮动导航、侧栏和聊天顶栏使用 Miuix blur + 固定版本的 Apache-2.0 玻璃材质；关闭玻璃或未启用硬件加速时使用普通背景。背景采样不包含玻璃消费者，避免递归绘制。
- 依赖参考 [Hyper-PiliPlus](https://github.com/AritxOnly/Hyper-PiliPlus/tree/12f95eaf5d660a1c3ae5bb5e66c39984ddd73aac) 的原生 Android 层。其 Dart 组件和 GPL 自定义导航未复制。这里不是官方名为“HyperOS 4”的 Miuix 发布版，也不承诺完全复刻参考应用效果。
- 未发布的玻璃源码固定于 Miuix PR #423 提交 `ec4c96963abf206ab2f2b0253a5efca71ba28043`，来源、修改记录及许可证见 [`third_party/miuix-glass/README.md`](third_party/miuix-glass/README.md)。这套运行时着色器将最低系统版本提升至 **Android 13**。
- 新增自适应布局单元测试；宿主窗口指标直接读取 View / Insets，不覆盖宿主 Insets listener。所有新界面仍需真机验证返回、键盘、旋转和 GPU 效果。

## 交流群
[💬 加入 Telegram 交流群](https://t.me/niubimokuai)

## 构建

### GitHub Actions（无需本地 Android 编译环境）

`.github/workflows/android-debug.yml` 在推送 `master` / `fix/**` 分支、向 `master` 提交 PR 或手动触发时运行。工作流安装 JDK 21、Android SDK 37 和 Build Tools 37.0.0，依次执行 Kotlin 编译、单元测试、Lint 和 debug APK 构建；任一步失败都会阻止后续构建，不跳过检查。

在仓库 **Actions → Android debug APK** 中打开对应运行，成功后下载 `QQ-Revived-debug-运行编号` artifact，解压取得 APK。诊断报告以独立 artifact 上传，保留 14 天。CI 成功仅表示构建检查通过，不代表 Vector / Android 17 真机兼容已验证。

调试 APK 使用 CI 临时 debug 签名，可能与已安装版本及其他运行的签名不同。若安装提示签名冲突，先备份模块设置再卸载**模块自身**后安装；不要卸载 QQ 宿主或清除其数据。安装后强行停止 QQ 再打开。

### 本地构建

需要 JDK 21、Android SDK Platform 37.0（SDK 包名为 `platforms;android-37.0`）、Build Tools 37.0.0；Gradle 由仓库 Wrapper 下载。

```powershell
.\gradlew.bat :app:assembleDebug
```

常用快速验证：

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:lintDebug
```

构建产物位于：

```text
app/build/outputs/apk/
```

## 项目结构

```text
app/src/main/java/me/padi/qqlite/revived/
├─ ModuleMainKt.kt              # libxposed 模块入口
├─ App.kt                       # 模块自身 Application
├─ compose/                     # 注入宿主的 Compose 页面
├─ hooks/                       # Xposed Hook 与宿主适配
│  ├─ common/                   # 通用 Hook
│  ├─ home/                     # 首页 Hook 与数据桥接
│  └─ aio/                      # 聊天页 Hook 与数据桥接
├─ legacy/view/                 # 模块管理页 Activity
├─ shared/                      # 跨 UI 体系共享 model / ViewModel
└─ utils/                       # 反射、Context、ComposeView 注入工具
```

更多架构说明见 [PROJECT_ARCHITECTURE.md](PROJECT_ARCHITECTURE.md)。

## Xposed 配置

入口文件：

```text
app/src/main/resources/META-INF/xposed/java_init.list
```

作用域文件：

```text
app/src/main/resources/META-INF/xposed/scope.list
```

当前默认作用域：

```text
com.tencent.qqlite
```

## 开发注意

- Hook 层只处理宿主适配、反射查找和事件桥接。
- UI 状态由 `shared` 层 ViewModel 管理，不在 Hook 层长期维护 UI 状态真相。
- `compose` 和 `shared` 不应反向依赖 `hooks`。
- `utils/HostComposeView.kt` 是宿主 View 中注入 Compose 的关键基础设施，修改后必须实机验证生命周期、返回键和输入法行为。
- 反射字段和方法名与宿主版本强相关，升级宿主后优先检查 `hooks/home` 与 `hooks/aio`。

## 许可证

本项目使用仓库根目录的 [LICENSE](LICENSE)。
