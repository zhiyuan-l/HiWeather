##HiWeather
#Introduction
This project provides a complete example of developing web project in Kotlin. The admin module updates the weather data automatically and the web module displays all the weather datum. 
![SIGNIN](http://git.oschina.net/uploads/images/2016/0612/174557_b82d3bfa_459585.jpeg "SIGN IN ADMIN MODULE")
![FORECAST](http://git.oschina.net/uploads/images/2016/0612/174757_f80614b2_459585.jpeg "FORECAST PAGE")
#Keywords
Kotlin SpringBoot SpringMVC Hibernate Gradle Thymeleaf
#Usage
Importing the project from setting.gradle
Running this project needs an active mysql server, and the database should be named weather, the charset of the DB must be utf-8, you can configure this project by editing the application.properties file. Before deploying the website, you should execute doc/weather.sql, the init script.
There is only one user, both the username and password are admin.
