##HiWeather
#介绍：
这个项目是作为我的本科毕业设计而开发的。由于目前大多数我能找到的 Kotlin Demo 都是 Android 项目，对于 Java Web 开发帮助有限，因此本人开源此项目作为一个参考，希望能够对各位有所帮助。

Github地址：https://github.com/vitoling/HiWeather
Git@OSC地址：http://git.oschina.net/vitoling/HiWeather
目前主要维护Github仓库，Git@OSC仓库间歇性同步

![后台登录](http://git.oschina.net/uploads/images/2016/0612/174557_b82d3bfa_459585.jpeg "后台登录页")
![天气预报](http://git.oschina.net/uploads/images/2016/0612/174757_f80614b2_459585.jpeg "网站天气预报页面")
#技术：
Kotlin、SpringBoot、SpringMVC、Hibernate、Gradle、Thymeleaf 等。
#声明：
本项目使用 GPLv3 作为开源许可证，请遵守相关协议。
#项目结构：
本项目包含三个模块：domain、admin、web。其中，domain 是基础模块，包含业务逻辑以及数据操作相关的包。admin 模块是后台模块，包含数据更新和管理的功能。而 web 模块是前台网站。
数据存储在 mysql 数据库中。
#使用说明：
项目在 master 目录下，使用 setting.gradle 导入项目。
项目运行前，需要创建名为 weather 的数据库，并执行 document 文件夹下 weather.sql中的代码。数据库的编码必须为 utf-8。
可独立使用的模块为 admin 和 web。
admin 后台只有一个用户：admin，密码也是 admin