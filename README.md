## HiWeather
# 介绍：
这个项目是作为我的本科毕业设计而开发的。由于目前大多数网上能找到的 Kotlin Demo 都是Android项目，对于Web开发帮助有限，因此本人开源此项目作为一个参考，希望能够对各位有所帮助。 

Github地址：https://github.com/vitoling/HiWeather 
Git@OSC地址：http://git.oschina.net/vitoling/HiWeather 
目前主要维护Github仓库，Git@OSC仓库间歇性同步 

![后台登录](http://git.oschina.net/uploads/images/2016/0612/174557_b82d3bfa_459585.jpeg "后台登录页")
![天气预报](http://git.oschina.net/uploads/images/2016/0612/174757_f80614b2_459585.jpeg "网站天气预报页面")
# 技术：
Kotlin、SpringBoot、SpringMVC、Hibernate、Gradle、Thymeleaf 等。
# 声明：
本项目使用 GPLv3 作为开源许可证，请遵守相关协议。
# 使用说明：
访问后台管理路径：/admin
后台管理用户： 
	账号：admin 
	密码：admin
项目运行前，请检查src/resources/config/ 下的配置文件内的数据库配置，运行项目需要创建名为weather的数据库，并执行 document 文件夹下 weather.sql中的代码, 其中包含所有表的结构和区域信息的数据。数据库的编码必须为 utf-8。
### Intellij IDEA: 
导入gradle项目，等待刷新完成之后，右键AppStarter.kt文件点击Run ... 或者 Debug ... 即可开始运行。 

目前项目使用的gradle版本为3.3。