# Simon Says（西蒙说）游戏 Spec

## Why
Simon Says 是一个经典的心理学实验游戏化版本，非常适合训练听觉/视觉序列记忆和注意力广度。通过不断增加序列长度，可以有效训练工作记忆的存储和更新能力。

## What Changes
- 实现第二个游戏：Simon Says（西蒙说）
  - 4-9个不同颜色的色块
  - 系统给出颜色序列，用户需要按相同顺序点击
  - 序列长度逐关递增
  - 支持倒序模式（高难度）

## Impact
- Affected specs: 新增 Simon Says 游戏功能
- Affected code: 
  - 新增 Simon Says 游戏相关类（ViewModel、Screen、Model）
  - 更新 MainActivity 导航逻辑
  - 复用现有的游戏选择界面

## ADDED Requirements

### Requirement: Simon Says 游戏基础功能
系统应提供 Simon Says 游戏，训练用户的序列记忆能力。

#### Scenario: 游戏初始化
- **WHEN** 用户开始游戏
- **THEN** 显示 4 个不同颜色的色块（默认）
- **AND** 从长度为 2 的序列开始

#### Scenario: 显示颜色序列
- **WHEN** 关卡开始
- **THEN** 系统按顺序高亮显示颜色序列
- **AND** 每个颜色高亮持续约 600ms
- **AND** 颜色之间有短暂间隔

#### Scenario: 用户点击颜色
- **WHEN** 序列显示结束
- **THEN** 用户可以点击颜色块
- **WHEN** 用户点击正确的颜色（按顺序）
- **THEN** 颜色块高亮反馈
- **WHEN** 用户点击错误的颜色
- **THEN** 显示错误提示，游戏结束

#### Scenario: 关卡完成判定
- **WHEN** 用户按顺序点击完所有颜色
- **THEN** 系统判断是否全部正确
- **IF** 全部正确
  - **THEN** 进入下一关，序列长度 +1
- **IF** 有错误
  - **THEN** 显示失败提示，可选择重试

### Requirement: 游戏设置
系统应提供游戏设置选项。

#### Scenario: 调整颜色块数量
- **WHEN** 用户选择颜色块数量
- **THEN** 可以选择 4、6、9 个颜色块

#### Scenario: 倒序模式
- **WHEN** 用户开启倒序模式
- **THEN** 用户需要按相反的顺序点击颜色
- **AND** 这是高难度模式

#### Scenario: 重置游戏
- **WHEN** 用户点击重置按钮
- **THEN** 游戏回到第 1 关，序列长度重置为 2

### Requirement: 游戏界面
系统应提供清晰的游戏界面。

#### Scenario: 显示游戏信息
- **WHEN** 游戏进行中
- **THEN** 显示当前关卡、序列长度、当前进度

#### Scenario: 显示游戏状态
- **WHEN** 游戏状态变化
- **THEN** 显示对应的状态提示（观察、记忆、选择）

#### Scenario: 游戏结束界面
- **WHEN** 游戏结束
- **THEN** 显示最终成绩和重试选项

## MODIFIED Requirements
无

## REMOVED Requirements
无
