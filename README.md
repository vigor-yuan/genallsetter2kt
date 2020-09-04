# GenAllSetter

![Build](https://github.com/TonyPhoneix/genallsetter2kt/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/13688-genallsetter.svg)](https://plugins.jetbrains.com/plugin/13688-genallsetter)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/13688-genallsetter.svg)](https://plugins.jetbrains.com/plugin/13688-genallsetter)
<!-- Plugin description -->
这是一个快速调用Java对象的所有Set方法的插件，它可以让你告别手动挨个调用赋值语句的痛苦，避免遗落并可以减少工作量。注意，该插件仅适用于Android Studio 和 Intellij IDEA。
## Installation

- Using IDE built-in plugin system:
  
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "GenAllSetter"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/TonyPhoneix/genallsetter2kt/releases/latest) and install it manually using
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>
## Usage

### 热键

你只需要记住Generate的热键即可，在mac上为`⌘ + N`， 在Windows上为`Alt + Insert`

### 演示

#### Generate All Setter 

如果只是想调用Java对象的所有Set方法，你可以这样做，在new对象的语句上唤出Generate菜单，选择 `Generate All Setter`。

![set](https://tva1.sinaimg.cn/large/007S8ZIlgy1gide98s6wng30go0km0yp.gif)

#### Generate All Setter With Default Value

`Generate All Setter` 不会帮你填写内容，如果你想自动填写默认值，你可以选择`Generate All Setter With Default Value`.

![set 默认值](https://tva1.sinaimg.cn/large/007S8ZIlgy1gideb8a62zg30go0kmjzi.gif)

#### Generate All Setter And Getter

插件还提供了从当前方法内部搜寻可以生成Get方法的参数或者是本地变量。勾选后，插件会在参数或本地变量中寻找同名同类型的成员变量并自动填充get方法，达到类似于BeanUtils的效果。这对于对象转换来说很有用

![set and get](https://tva1.sinaimg.cn/large/007S8ZIlgy1gidefncktlg30go0kmnpd.gif)



#### Generate All Builder

除了set方法以外，插件还添加了对常见的Lombok @Builder 链式赋值的支持，你仅仅需要在`Object.builder()`上唤起Generate菜单，选择你想要的功能即可。

![ezgif.com-video-to-gif](https://tva1.sinaimg.cn/large/007S8ZIlgy1gidejc9wr1g30go0km4l3.gif)

### 如果你觉得这个插件不错的话？

- 请给我个小星星
- 分享给别人，让更多人知道这个插件。
<!-- Plugin description end -->