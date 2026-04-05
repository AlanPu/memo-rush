# MemoRush - 记忆力训练应用

MemoRush 是一款专为提高记忆力和认知能力设计的 Android 应用，通过多种有趣的游戏方式帮助用户锻炼大脑。

## 🎯 功能特点

- **多种记忆力训练游戏**：包含5种不同类型的记忆力训练游戏
- **现代化界面**：使用 Jetpack Compose 构建的流畅、美观的用户界面
- **渐变与动画效果**：采用现代设计风格，具有平滑的动画和渐变效果
- **响应式布局**：适配不同屏幕尺寸的设备
- **游戏难度适中**：适合各年龄段用户使用

## 🎮 游戏类型

### 1. 网格翻牌 (Grid Memory)
- **玩法**：记住卡片位置，找出所有配对
- **训练目标**：提高空间记忆力和专注力

### 2. 西蒙说 (Simon Says)
- **玩法**：记住并重复颜色序列
- **训练目标**：提高短期记忆力和注意力

### 3. N-Back任务 (N-Back Task)
- **玩法**：判断当前项目是否与N步前相同
- **训练目标**：提高工作记忆和认知灵活性

### 4. 消失的物品 (Missing Items)
- **玩法**：观察场景变化，找出消失的物品
- **训练目标**：提高视觉记忆力和观察力

### 5. 斯特鲁普任务 (Stroop Task)
- **玩法**：说出文字颜色而非文字内容
- **训练目标**：提高注意力控制和认知抑制能力

## 🛠 技术栈

- **开发语言**：Kotlin
- **UI框架**：Jetpack Compose
- **架构**：MVVM (Model-View-ViewModel)
- **构建工具**：Gradle
- **测试框架**：JUnit

## 📁 项目结构

```
memo-rush/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/top/alan/memorush/
│   │   │   │   ├── common/          # 通用组件
│   │   │   │   ├── game/            # 游戏实现
│   │   │   │   ├── model/           # 数据模型
│   │   │   │   ├── ui/              # UI 相关
│   │   │   │   └── MainActivity.kt  # 主活动
│   │   │   └── res/                 # 资源文件
│   │   └── test/                    # 单元测试
│   └── build.gradle.kts             # 应用构建配置
├── gradle/                          # Gradle 配置
├── .gitignore                       # Git 忽略文件
├── build.gradle.kts                 # 项目构建配置
└── settings.gradle.kts              # 项目设置
```

## 🚀 安装与运行

### 前置条件
- Android Studio Arctic Fox (2020.3.1) 或更高版本
- JDK 11 或更高版本
- Android SDK API 级别 31 或更高

### 安装步骤
1. 克隆项目到本地：
   ```bash
   git clone https://github.com/AlanPu/memo-rush.git
   ```

2. 在 Android Studio 中打开项目

3. 同步 Gradle 依赖

4. 运行应用到模拟器或真机设备

## 🎨 设计风格

- **配色方案**：深色背景搭配霓虹色调，营造现代科技感
- **动画效果**：流畅的过渡动画和微交互
- **布局**：卡片式设计，层次分明
- **字体**：清晰易读，突出重要信息

## 🧪 测试

项目包含单元测试，覆盖了各个游戏的 ViewModel 逻辑：

- `GridMemoryGameViewModelTest.kt`
- `SimonSaysGameViewModelTest.kt`
- `NBackGameViewModelTest.kt`
- `MissingItemsGameViewModelTest.kt`
- `StroopTaskGameViewModelTest.kt`

运行测试命令：
```bash
./gradlew test
```

## 🌟 亮点

- **模块化设计**：代码结构清晰，易于维护和扩展
- **响应式 UI**：使用 Jetpack Compose 构建现代化界面
- **丰富的动画**：提升用户体验的流畅动画效果
- **全面的测试**：确保游戏逻辑的正确性
- **多种游戏类型**：满足不同的记忆力训练需求

---

**开始你的记忆力训练之旅吧！** 🧠💪