# GenAllSetter
[![Version](https://img.shields.io/jetbrains/plugin/v/13688-genallsetter.svg)](https://plugins.jetbrains.com/plugin/13688-genallsetter)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/13688-genallsetter.svg)](https://plugins.jetbrains.com/plugin/13688-genallsetter)

This is a plugin for quickly invoking all Set methods of a Java object. It allows you to bid farewell to the pain of manually calling each assignment statement, preventing omissions, and reducing workload. Note that this plugin is only applicable to Android Studio and IntelliJ IDEA.

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "GenAllSetter"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/TonyPhoneix/genallsetter2kt/releases/latest) and install it manually using
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>
  
## Usage

### Shortcuts

You only need to remember the shortcut for Generate, which is `⌘ + N` on macOS and `Alt + Insert` on Windows.

### Demonstration

#### Generate All Setter 

If you want to call all Set methods of a Java object, you can do so by invoking the Generate menu on the new object statement and selecting `Generate All Setter`.

![set](https://tva1.sinaimg.cn/large/007S8ZIlgy1gide98s6wng30go0km0yp.gif)

#### Generate All Setter With Default Value

`Generate All Setter` won't fill in content for you. If you want to automatically fill in default values, you can choose `Generate All Setter With Default Value`.

![set 默认值](https://tva1.sinaimg.cn/large/007S8ZIlgy1gideb8a62zg30go0kmjzi.gif)

#### Generate All Setter And Getter

The plugin provides the ability to search for parameters or local variables within the current method that can generate Get methods. When checked, the plugin will search for member variables with the same name and type in parameters or local variables and automatically fill in the get method, achieving a similar effect to BeanUtils. This is useful for object conversion.
![set and get](https://tva1.sinaimg.cn/large/007S8ZIlgy1gidefncktlg30go0kmnpd.gif)

#### Generate All Builder

In addition to set methods, the plugin also adds support for the common Lombok @Builder chained assignment. You only need to invoke the Generate menu on `Object.builder()` and select the functionality you want.

![ezgif.com-video-to-gif](https://tva1.sinaimg.cn/large/007S8ZIlgy1gidejc9wr1g30go0km4l3.gif)

#### change log
Starting from version 2.0.5, the plugin now supports:
- Generating getter methods with variable declarations (e.g., `String name = test.getName();`)
- Generating getters from method parameters as well as local variables



### If you find this plugin helpful:

- Please give it a star.
- Share it with others to let more people know about this plugin.
