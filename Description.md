<!-- Plugin description -->
# GenAllSetter
[GitHub](https://github.com/TonyPhoneix/genallsetter2kt) | [Issues](https://github.com/TonyPhoneix/genallsetter2kt/issues)

A powerful plugin for quickly generating and invoking all Set methods of Java objects. Say goodbye to manually writing repetitive assignment statements, prevent omissions, and reduce workload. This plugin is specifically designed for Android Studio and IntelliJ IDEA.

## Usage

### Shortcuts

You only need to remember the Generate shortcut: `⌘ + N` on macOS or `Alt + Insert` on Windows.

### Features

#### Generate All Setter 

To call all Set methods of a Java object, simply invoke the Generate menu on a new object statement and select `Generate All Setter`.

![set](https://tva1.sinaimg.cn/large/007S8ZIlgy1gide98s6wng30go0km0yp.gif)

#### Generate All Setter With Default Value

While `Generate All Setter` creates empty setter calls, you can choose `Generate All Setter With Default Value` to automatically fill in default values.

![set with default value](https://tva1.sinaimg.cn/large/007S8ZIlgy1gideb8a62zg30go0kmjzi.gif)

#### Generate All Setter And Getter

The plugin can search for parameters or local variables within the current method that can be used with getter methods. When enabled, it will look for member variables with matching names and types in parameters or local variables, automatically filling in the getter methods - similar to BeanUtils functionality. This is particularly useful for object conversion.

Starting from version 2.0.5, the plugin now supports:
- Generating getter methods with variable declarations (e.g., `String name = test.getName();`)
- Generating getters from both method parameters and local variables

![set and get](https://tva1.sinaimg.cn/large/007S8ZIlgy1gidefncktlg30go0kmnpd.gif)

#### Generate All Builder

In addition to setter methods, the plugin supports Lombok's @Builder pattern for chained assignments. Simply invoke the Generate menu on `Object.builder()` and select your desired functionality.

![builder demo](https://tva1.sinaimg.cn/large/007S8ZIlgy1gidejc9wr1g30go0km4l3.gif)

### Support the Project

If you find this plugin helpful:
- Please give it a star on [GitHub](https://github.com/TonyPhoneix/genallsetter2kt)
- Share it with others to help more developers discover this plugin
<!-- Plugin description end -->