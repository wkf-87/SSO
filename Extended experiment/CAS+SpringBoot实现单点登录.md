# CAS+SpringBoot实现单点登录

## 一、实验原理

### 1.单点登录实现方式

![image-20210916022832661](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210916022832661.png)



### 2.CAS-SringBoot实现SSO单点登录

![image-20210916022904077](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210916022904077.png)

![image-20210916023212928](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210916023212928.png)

![image-20210916023300135](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210916023300135.png)



## 二、实验步骤

### 1.搭建Tomcat-Http支持

#### （1）生成秘钥库

```
keytool -genkey -v -alias cqu2019cassso.com -keyalg RSA -keystore E:\Project_Java\CAS_SpringBoot_SSO\cas\keystore\cqu2019cassso.com.keystore
```

我们采用JDK自带的keytool工具生成秘钥库，别名一定要与之后的域名一致，为防止出现权限问题，所有回答均回答别名

存储在项目\cas\keystore文件下

秘钥库密码填写123456

执行完会生成秘钥库文件

![image-20210915184741693](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915184741693.png)

#### （2）导出证书

```
keytool -export -trustcacerts  -alias cqu2019cassso.com -file E:\Project_Java\CAS_SpringBoot_SSO\cas\keystore\cqu2019cassso.com.cer  -keystore E:\Project_Java\CAS_SpringBoot_SSO\cas\keystore\cqu2019cassso.com.keystore
```

密码为前一步输入的123456

![image-20210915185328702](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915185328702.png)

![image-20210915185348863](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915185348863.png)

#### （3）将证书导入到JDK证书库

```
keytool -import -trustcacerts -alias cqu2019cassso.com -file E:\Project_Java\CAS_SpringBoot_SSO\cas\keystore\cqu2019cassso.com.cer -keystore "E:\Environment\JDK_Java_SE_8\jre\lib\security\cacerts"
```

最后一个地址在jdk环境目录下，注意这里的密钥库口令统一为changeit

![image-20210915185752240](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915185752240.png)

#### （4）tomcat配置http支持

tomcat版本为9.0.38

找到tomcat->conf->server.xml 打开文件，加下如下配置即可，设置端口为8443

```
	<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"

               maxThreads="150" SSLEnabled="true" scheme="https" secure="true"

               clientAuth="false" sslProtocol="TLS" 

                keystoreFile="E:\Project_Java\CAS_SpringBoot_SSO\cas\keystore\cqu2019cassso.com.keystore"

                keystorePass="123456" />
```

来到tomcat目录的conf子目录中，找到一个名为 "logging.properties" 的文件，打开这个文本文件，找到如下配置项：

```
java.util.logging.ConsoleHandler.encoding = UTF-8
```

将其改为如下，将中文乱码转化为正常显示

```
java.util.logging.ConsoleHandler.encoding = GBK
```

保存后，在tomcat启动bin下的startup.bat

![image-20210915190804706](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915190804706.png)

#### （5）测试

访问 https://localhost:8443

![image-20210915191811441](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915191811441.png)

显示如上页面测试成功



### 2.CAS Server服务端搭建

#### （1）CAS Server war下载

https://repo1.maven.org/maven2/org/apereo/cas/cas-server-webapp-tomcat/5.3.14/

下载：cas-server-webapp-tomcat-5.3.14

#### （2）CAS Server发布到tomcat

把war包放tomcat\webapps下，启动tomcat会自动解压，我们把名称改成cas，方便访问

![image-20210915192613894](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915192613894.png)

在cas\WEB-INF\classes\log4j2 修改日志存放地址，修改至大cas目录下，与keystore、tomcat同目录

![image-20210915192934237](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915192934237.png)

![image-20210915193034401](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915193034401.png)

启动tomcat，访问https://localhost:8443/cas 测试，显示如下页面成功：

![image-20210915193342323](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915193342323.png)

在主机系统文件下找到目录 C:\Windows\System32\drivers\etc，配置hosts文件，添加如下建立域名访问：

```
127.0.0.1   cqu2019cassso.com
```

![image-20210915193711639](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915193711639.png)

通过域名访问 https://cqu2019cassso.com:8443/cas进行测试，显示同上表示成功

![image-20210915193857104](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915193857104.png)

此时的用户名密码为设置文档中默认参数，casuer/Mellon

![image-20210915194101811](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915194101811.png)

#### （3）配置数据库，数据库用户认证

数据库使用MySQL5.7版本，8版本改动较大以下过程大多不适用！同时建议使用SQLyog可视化工具

新建数据库和表，代码如下：

```
CREATE DATABASE /*!32312 IF NOT EXISTS*/`db_sso` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `db_sso`;

/*Table structure for table `t_cas` */

DROP TABLE IF EXISTS `t_cas`;

CREATE TABLE `t_cas` (

  `id` int(11) NOT NULL AUTO_INCREMENT,

  `username` varchar(30) DEFAULT NULL,

  `password` varchar(100) DEFAULT NULL,

  PRIMARY KEY (`id`)

) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

/*Data for the table `t_cas` */

insert  into `t_cas`(`id`,`username`,`password`) values (1,'wzt','20194249');
```

打开t_cas表单，显示如下：

![image-20210915194756226](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915194756226.png)

修改application.properties配置文件，文件位置cas\WEB-INF\classes下，注释掉写死的用户数据并加上jdbc数据源配置

```
cas.authn.jdbc.query[0].dialect=org.hibernate.dialect.MySQL5Dialect

cas.authn.jdbc.query[0].url=jdbc:mysql://localhost:3306/db_sso?serverTimezone=GMT

cas.authn.jdbc.query[0].user=root

cas.authn.jdbc.query[0].password=123456 # 连接数据库的用户名密码

cas.authn.jdbc.query[0].sql=select * from t_cas where username=? # 遍历查找username

cas.authn.jdbc.query[0].fieldPassword=password

cas.authn.jdbc.query[0].driverClass=com.mysql.jdbc.Driver # 数据库驱动
```

![image-20210915195454015](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915195454015.png)

下载并加载如下jdbc驱动包以及jar包，加载至cas\apache-tomcat-9.0.38\webapps\cas\WEB-INF\lib下：

![image-20210915195643594](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915195643594.png)

启动tomcat测试：

![image-20210915195954141](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915195954141.png)

![image-20210915200015864](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915200015864.png)

显示如上测试成功

#### （4）密码加密校验

避免密码明文显示，进行md5加密，数据库下生成对应的md5密码

```
SELECT MD5('20194249');
```

![image-20210915200412544](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915200412544.png)

将加密后的密码更新到数据库

![image-20210915200505867](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915200505867.png)

再次修改application.properties配置文件，继续添加如下：

```
cas.authn.jdbc.query[0].passwordEncoder.type=DEFAULT

cas.authn.jdbc.query[0].passwordEncoder.characterEncoding=UTF-8

#MD5加密策略

cas.authn.jdbc.query[0].passwordEncoder.encodingAlgorithm=MD5
```

启动tomcat再次测试，用户名密码同时，测试成功显示如上



### 3.CAS Client+SpringBoot客户端整合搭建

Git版本控制的操作不在下面的过程中赘述

#### （1）创建项目并配置

在E:\Project_Java\CAS_SpringBoot_SSO\sso-sys创建Maven项目sso-sys，不使用模板

![image-20210915204014638](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915204014638.png)

配置项目pom文件，控制各组件版本，代码如下：

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.cqu2019sso</groupId>
    <artifactId>sso-sys</artifactId>
    <packaging>pom</packaging>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <cas.version>2.3.0-GA</cas.version>
        <spring-boot.version>2.3.4.RELEASE</spring-boot.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>net.unicon.cas</groupId>
                <artifactId>cas-client-autoconfig-support</artifactId>
                <version>${cas.version}</version>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

        </dependencies>
    </dependencyManagement>

</project>
```

#### （2）创建模块system1

一个公司有多个系统，用一个module模拟一个系统

![image-20210915202444889](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915202444889.png)

![image-20210915202612582](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915202612582.png)

配置system1的pom文件，添加依赖，代码如下：

```
<dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </dependency>

        <dependency>
            <groupId>net.unicon.cas</groupId>
            <artifactId>cas-client-autoconfig-support</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-cas</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-taglibs</artifactId>
        </dependency>

    </dependencies>
```

system1\src\main\resources下添加templates、application.yml 配置文件

![image-20210915205425585](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915205425585.png)

templates下的index.htmi文件代码如下，是一个简单的html文件，包含读取用户名、退出的交互：

```
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
<meta charset="UTF-8"/>
<title>system1</title>
</head>
<body>
欢迎：&nbsp;&nbsp;<font th:text="${session._const_cas_assertion_.principal.name}"></font>&nbsp;&nbsp;进入system1；
<br/><br/>
&nbsp;&nbsp;<a href="/logout">安全退出</a>
</body>
</html>
```

application.yml配置文件代码如下：

```
server:
  port: 7777 # 指定system1的端口7777


# 建立cas连接
cas:
  server-url-prefix: https://cqu2019cassso.com:8443/cas
  server-login-url: https://cqu2019cassso.com:8443/cas/login # 未获得登录权限时的登录网址
  client-host-url: http://cqu2019cassso.com:7777 # system1的登录网址
  validation-type: cas3
```

在java\com.cqu2019cassso下建立IndexController以及System1Application文件，即控制端

IndexController.java代码如下：

```
package com.cqu2019cassso.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    /**
     * 网站根目录请求
     * @return
     */
    @RequestMapping("/system1")
    public ModelAndView root(){
        ModelAndView mav=new ModelAndView();
        mav.setViewName("index");
        return mav;
    }

    /**
     * 注销
     * @return
     */
    // HttpSession session

    @RequestMapping("/logout")
    public String logout(){
        return "redirect:https://cqu2019cassso.com:8443/cas/logout";
    }
}
```

System1Application.java代码如下：

```
package com.cqu2019cassso;
import net.unicon.cas.client.configuration.EnableCasClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableCasClient
public class System1Application {

    public static void main(String[] args) {
        SpringApplication.run(System1Application.class, args);
    }
}
```

#### （3）更改配置支持http请求

在CAS_SpringBoot_SSO\cas\apache-tomcat-9.0.38\webapps\cas\WEB-INF\classes\services下修改HTTPSandIMAPS-10000001文件，添加http即可

![image-20210915211736642](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915211736642.png)

再次修改application.properties配置文件，继续添加如下：

```
cas.tgc.secure=false

cas.serviceRegistry.initFromJson=true
```

#### （4）启动测试

启动tomcat，启动SQLyog，运行System1Application.java

访问 http://cqu2019cassso.com:7777 ，由于没有登录权限（票根），无法登陆会首先进入登录网址 https://cqu2019cassso.com:8443/cas/login

![image-20210915220408662](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915220408662.png)

完成登录后自动跳转到 http://cqu2019cassso.com:7777

![image-20210915220435159](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915220435159.png)

点击安全退出即可注销，注意关闭浏览器清除缓存

![image-20210915220533568](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210915220533568.png)

#### （5）system2模块

完全参照模块system1进行设置即可，端口设置为8888

![image-20210916010944319](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210916010944319.png)

![image-20210916011024656](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210916011024656.png)

其余文件同理



### 4.更改页面

此前的界面均为CAS服务的默认界面，现在对其进行修改，删去无效信息保持界面简洁

更改CAS Sever前端即可，后端更改更复杂

各种配置均在CAS_SpringBoot_SSO\cas\apache-tomcat-9.0.38\webapps\cas\WEB-INF\classes下

#### （1）更改网页图标

更改CAS_SpringBoot_SSO\cas\apache-tomcat-9.0.38\webapps\cas\WEB-INF\classes\static\favicon.ico文件即可

![](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210916012239046.png)

#### （2）更改布局

修改CAS_SpringBoot_SSO\cas\apache-tomcat-9.0.38\webapps\cas\WEB-INF\classes\templates\layout.html文件

在IDEA中打开CAS_SpringBoot_SSO\cas\apache-tomcat-9.0.38\webapps\cas项目修改更加快捷

![image-20210916013143498](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210916013143498.png)

注释掉header、footer即可

![image-20210916013411147](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210916013411147.png)

#### （3）更改其它

对照html文件查看对应代码修改即可，以下均为举例，实际操作中并未全部应用

去掉login界面右侧信息

![image-20210916013658923](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210916013658923.png)

换成一张PNG图片

![image-20210916015212508](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210916015212508.png)

![image-20210916015231692](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210916015231692.png)

### 5.建立引导总站

为方便跳转，查看实验成果，建立一个简单的html文件作为引导总站，代码如下：

===最新的账户、密码已经更新到数据库===

```
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Cache-Control" content="no-cache">
    <meta http-equiv="Expires" content="0">
    <title>CQU2019级JAVA实验1单点登录系统</title>
</head>
<body>
CQU2019级JAVA实验1&nbsp&nbsp&nbsp&nbspSSO单点登录系统<br/>
<br/>
Info<br/>
王正霆 20194249 账户：wzt 密码：20194249<br/>
杨寒壹 20194244 账户：yhy 密码：20194244<br/>
王开发 20194235 账户：wkf 密码：20194235<br/> #最新的账户、密码已经更新到数据库
<br/>
<a href="http://cqu2019cassso.com:7777" target="_blank">system1</a><br/>
<a href="http://cqu2019cassso.com:8888" target="_blank">system2</a><br/>

</body>
```

显示如下：

![image-20210916021418141](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210916021418141.png)



## 三、实验成果

### 1.准备工作

启动tomcat、连接数据库、运行System1Application、System2Application



### 2.初态

![image-20210916021851836](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210916021851836.png)



### 3.登录System1，刷新System2

![image-20210916022042494](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210916022042494.png)

![image-20210916022103827](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210916022103827.png)

System2界面刷新后直接进入System2内界面，无需login，实验成功！



### 4.登出System1，刷新System2

![image-20210916022248613](https://wangzt670-img.oss-cn-beijing.aliyuncs.com/img/image-20210916022248613.png)

System1显示注销成功，System2刷新后需要重新login，实验成功！



### 5.小结

程序正常运行，各环节测试结果符合预期，简单的SSO单点登录系统完成！



## 四、实验总结

### 1.CAS实现SSO单点登录的优势

#### （1）CAS Sever

首先，CAS Sever的实现方式为SpringMVC+webflow

其次，CAS本身也是开源的验证登录服务，方便我们借鉴、引用、复写，节省了大量细节开发时间

#### （2）CAS的安全性

对于一个 CAS 用户来说，最重要是要保护它的 TGC ，如果 TGC 不慎被 CAS Server 以外的实体获得， Hacker 能够找到该 TGC ，然后冒充 CAS 用户访问 所有 授权资源，PGT 的角色跟 TGC 是一样的

从基础模式可以看出， TGC 是 CAS Server 通过 SSL 方式发送给终端用户，因此，要截取 TGC 难度非常大，从而确保 CAS 的安全性

TGT 的存活周期默认为 120 分钟



### 2.实验不足与反思

#### （1）MySQL版本问题

MySQL5.7与8两个版本差别显著，调用JDBC方式、数据库驱动等等都有明显区别

本实验开始采用MySQL8版本，开发过程极为艰辛，8版本更新，规范的调用方式等等可借鉴参考的资料较少

后改为5.7版本，5.7版本更为成熟，各种报错信息可参考文档更多，开发过程明显更加顺利

#### （2）CAS实现单点登录的不足

CAS确实及大地方便了SSO单点登录系统的开发过程，但是也导致了最终系统的不足部分：

CAS不能在不关闭浏览器的情况向实现完全注销，浏览器会留下缓存数据

在实验完成后也查询过文档希望找寻CAS在不关闭浏览器的情况向实现完全注销的方法，尚未找到正确的方法文档，有待后续改进
