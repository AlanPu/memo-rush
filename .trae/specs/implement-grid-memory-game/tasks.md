# Tasks

## Phase 1: 项目架构搭建
- [x] Task 1: 创建项目包结构
  - [x] SubTask 1.1: 创建 game 包用于存放所有游戏
  - [x] SubTask 1.2: 创建 common 包用于存放公共组件
  - [x] SubTask 1.3: 创建 model 包用于存放数据模型

- [x] Task 2: 创建游戏选择主界面
  - [x] SubTask 2.1: 创建 GameType 枚举类，定义所有游戏类型
  - [x] SubTask 2.2: 创建 GameSelectionScreen 可组合函数
  - [x] SubTask 2.3: 实现下拉菜单组件，显示游戏列表
  - [x] SubTask 2.4: 在 MainActivity 中设置导航逻辑

## Phase 2: 网格翻牌游戏核心实现
- [x] Task 3: 创建游戏数据模型
  - [x] SubTask 3.1: 创建 GridCell 数据类（位置、状态）
  - [x] SubTask 3.2: 创建 GameState 数据类（当前关卡、闪烁序列、用户选择等）
  - [x] SubTask 3.3: 创建 GameViewModel 管理游戏状态

- [x] Task 4: 实现网格UI组件
  - [x] SubTask 4.1: 创建 GridCell 可组合函数，显示单个格子
  - [x] SubTask 4.2: 创建 GameGrid 可组合函数，显示完整网格
  - [x] SubTask 4.3: 实现格子闪烁动画效果
  - [x] SubTask 4.4: 实现格子点击交互效果

- [x] Task 5: 实现游戏逻辑
  - [x] SubTask 5.1: 实现随机选择闪烁格子的逻辑
  - [x] SubTask 5.2: 实现闪烁序列播放逻辑
  - [x] SubTask 5.3: 实现用户输入验证逻辑
  - [x] SubTask 5.4: 实现关卡递进逻辑（闪烁数量递增）

- [x] Task 6: 实现游戏界面
  - [x] SubTask 6.1: 创建 GridMemoryGameScreen 可组合函数
  - [x] SubTask 6.2: 显示当前关卡和得分信息
  - [x] SubTask 6.3: 显示游戏状态提示（观察、记忆、选择）
  - [x] SubTask 6.4: 实现游戏结束界面

## Phase 3: 游戏设置和优化
- [x] Task 7: 实现游戏设置功能
  - [x] SubTask 7.1: 创建设置界面组件
  - [x] SubTask 7.2: 实现网格大小选择（3×3、4×4、5×5）
  - [x] SubTask 7.3: 实现游戏重置功能

- [x] Task 8: 实现进阶模式
  - [x] SubTask 8.1: 添加干扰项开关
  - [x] SubTask 8.2: 实现干扰项显示逻辑

## Phase 4: 测试和优化
- [x] Task 9: 编写单元测试
  - [x] SubTask 9.1: 测试游戏状态管理逻辑
  - [x] SubTask 9.2: 测试闪烁序列生成逻辑
  - [x] SubTask 9.3: 测试用户输入验证逻辑

- [x] Task 10: UI测试和优化
  - [x] SubTask 10.1: 测试不同屏幕尺寸的适配
  - [x] SubTask 10.2: 优化动画流畅度
  - [x] SubTask 10.3: 优化用户体验（提示、反馈）

# Task Dependencies
- Task 2 依赖 Task 1（需要包结构）
- Task 3-6 可以并行开发（核心游戏功能）
- Task 7-8 依赖 Task 6（需要基础游戏界面）
- Task 9-10 依赖 Task 8（需要完整功能）
