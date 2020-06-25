# Apache Dubbo (孵化) 项目

[![Build Status](https://travis-ci.org/apache/incubator-dubbo.svg?branch=master)](https://travis-ci.org/apache/incubator-dubbo)
[![codecov](https://codecov.io/gh/apache/incubator-dubbo/branch/master/graph/badge.svg)](https://codecov.io/gh/apache/incubator-dubbo)
![maven](https://img.shields.io/maven-central/v/org.apache.dubbo/dubbo.svg)
![license](https://img.shields.io/github/license/alibaba/dubbo.svg)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/apache/incubator-dubbo.svg)](http://isitmaintained.com/project/apache/incubator-dubbo "Average time to resolve an issue")
[![Percentage of issues still open](http://isitmaintained.com/badge/open/apache/incubator-dubbo.svg)](http://isitmaintained.com/project/apache/incubator-dubbo "Percentage of issues still open")
[![Tweet](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?text=Apache%20Dubbo%20(incubating)%20is%20a%20high-performance%2C%20java%20based%2C%20open%20source%20RPC%20framework.&url=http://dubbo.incubator.apache.org/&via=ApacheDubbo&hashtags=rpc,java,dubbo,micro-service)
[![](https://img.shields.io/twitter/follow/ApacheDubbo.svg?label=Follow&style=social&logoWidth=0)](https://twitter.com/intent/follow?screen_name=ApacheDubbo)
[![Gitter](https://badges.gitter.im/alibaba/dubbo.svg)](https://gitter.im/alibaba/dubbo?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

Apache Dubbo是一个基于Java的高性能开源RPC框架。请访问官方网站以获取快速入门和文档，以及访问Wiki以获得新闻，常见问题解答和发行说明。


## 架构

![Architecture](http://dubbo.apache.org/img/architecture.png)

## 源码导读
相关代码中已添加中文注释：

*  Dubbo SPI机制：
[org.apache.dubbo.common.extension](https://github.com/houxudong01/dubbo/blob/feature/2.7.1/dubbo-common/src/main/java/org/apache/dubbo/common/extension/ExtensionLoader.java)  

* 服务暴露：
[org.apache.dubbo.config.spring.ServiceBean#onApplicationEvent()](https://github.com/houxudong01/dubbo/blob/feature/2.7.1/dubbo-config/dubbo-config-spring/src/main/java/org/apache/dubbo/config/spring/ServiceBean.java#L121)
* 服务引入: [org.apache.dubbo.config.spring.ServiceBean#afterPropertiesSet()](https://github.com/houxudong01/dubbo/blob/feature/2.7.1/dubbo-config/dubbo-config-spring/src/main/java/org/apache/dubbo/config/spring/ReferenceBean.java#L220)
*  服务调用过程:
[org.apache.dubbo.demo.consumer.Application](https://github.com/houxudong01/dubbo/blob/feature/2.7.1/dubbo-demo/dubbo-demo-annotation/dubbo-demo-annotation-consumer/src/main/java/org/apache/dubbo/demo/consumer/Application.java#L43)

## 官方文档
[文档](http://dubbo.apache.org/zh-cn/docs/user/quick-start.html)

## 特性

* 基于透明接口的RPC
* 智能负载均衡
* 自动服务注册和发现
* 高扩展性
* 运行时流量路由
* 可视化服务治理

## 入门

以下代码段来自 [Dubbo Samples](https://github.com/apache/incubator-dubbo-samples/tree/master/dubbo-samples-api). 您可以克隆示例项目，dubbo-samples-api然后在继续阅读之前进入子目录。

```bash
# git clone https://github.com/apache/incubator-dubbo-samples.git
# cd incubator-dubbo-samples/dubbo-samples-api
```
目录下有一个README文件 [README](https://github.com/apache/incubator-dubbo-samples/tree/master/dubbo-samples-api/README.md) dubbo-samples-api。阅读它，然后按照说明尝试该示例。

### Maven 依赖

```xml
<properties>
    <dubbo.version>2.7.1</dubbo.version>
</properties>
    
<dependencies>
    <dependency>
        <groupId>org.apache.dubbo</groupId>
        <artifactId>dubbo</artifactId>
        <version>${dubbo.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.dubbo</groupId>
        <artifactId>dubbo-dependencies-zookeeper</artifactId>
        <version>${dubbo.version}</version>
    </dependency>
</dependencies>
```

### 定义服务接口

```java
package org.apache.dubbo.samples.api;

public interface GreetingService {
    String sayHello(String name);
}
```

* 请参阅GitHub上的 [api/GreetingService.java](https://github.com/apache/incubator-dubbo-samples/blob/master/dubbo-samples-api/src/main/java/org/apache/dubbo/samples/api/GreetingsService.java)

### 为提供者实现服务接口

```java
package org.apache.dubbo.samples.provider;
 
import org.apache.dubbo.samples.api.GreetingService;
 
public class GreetingServiceImpl implements GreetingService {
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
```

* 请参阅GitHub上的 [provider/GreetingServiceImpl.java](https://github.com/apache/incubator-dubbo-samples/blob/master/dubbo-samples-api/src/main/java/org/apache/dubbo/samples/provider/GreetingsServiceImpl.java) 

### 启动服务提供者

```java
package org.apache.dubbo.demo.provider;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.samples.api.GreetingService;

import java.io.IOException;
 
public class Application {

    public static void main(String[] args) throws IOException {
        ServiceConfig<GreetingService> serviceConfig = new ServiceConfig<GreetingService>();
        serviceConfig.setApplication(new ApplicationConfig("first-dubbo-provider"));
        serviceConfig.setRegistry(new RegistryConfig("multicast://224.5.6.7:1234"));
        serviceConfig.setInterface(GreetingService.class);
        serviceConfig.setRef(new GreetingServiceImpl());
        serviceConfig.export();
        System.in.read();
    }
}
```

* 请参阅GitHub上的 [provider/Application.java](https://github.com/apache/incubator-dubbo-samples/blob/master/dubbo-samples-api/src/main/java/org/apache/dubbo/samples/provider/Application.java) *

### 构建并运行服务提供者

```bash
# mvn clean package
# mvn -Djava.net.preferIPv4Stack=true -Dexec.mainClass=org.apache.dubbo.demo.provider.Application exec:java
```

### 消费者调用远程服务

```java
package org.apache.dubbo.demo.consumer;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.samples.api.GreetingService;

public class Application {
    public static void main(String[] args) {
        ReferenceConfig<GreetingService> referenceConfig = new ReferenceConfig<GreetingService>();
        referenceConfig.setApplication(new ApplicationConfig("first-dubbo-consumer"));
        referenceConfig.setRegistry(new RegistryConfig("multicast://224.5.6.7:1234"));
        referenceConfig.setInterface(GreetingService.class);
        GreetingService greetingService = referenceConfig.get();
        System.out.println(greetingService.sayHello("world"));
    }
}
```

### 消费者构建和启动

```bash
# mvn clean package
# mvn -Djava.net.preferIPv4Stack=true -Dexec.mainClass=org.apache.dubbo.demo.consumer.Application exec:java
```

消费者将会在屏幕上打印出 `Hello world`

*See [consumer/Application.java](https://github.com/apache/incubator-dubbo-samples/blob/master/dubbo-samples-api/src/main/java/org/apache/dubbo/samples/consumer/Application.java) on GitHub.*

### 下一步

* [Your first Dubbo application](http://dubbo.apache.org/en-us/blog/dubbo-101.html) - A 101 tutorial to reveal more details, with the same code above.
* [Dubbo user manual](http://dubbo.apache.org/en-us/docs/user/preface/background.html) - How to use Dubbo and all its features.
* [Dubbo developer guide](http://dubbo.apache.org/en-us/docs/dev/build.html) - How to involve in Dubbo development.
* [Dubbo admin manual](http://dubbo.apache.org/en-us/docs/admin/install/provider-demo.html) - How to admin and manage Dubbo services.

## 联系

* Mailing list: 
  * dev list: for dev/user discussion. [subscribe](mailto:dev-subscribe@dubbo.incubator.apache.org), [unsubscribe](mailto:dev-unsubscribe@dubbo.incubator.apache.org), [archive](https://lists.apache.org/list.html?dev@dubbo.apache.org),  [guide](https://github.com/apache/incubator-dubbo/wiki/Mailing-list-subscription-guide)
  
* Bugs: [Issues](https://github.com/apache/incubator-dubbo/issues/new?template=dubbo-issue-report-template.md)
* Gitter: [Gitter channel](https://gitter.im/alibaba/dubbo) 
* Twitter: [@ApacheDubbo](https://twitter.com/ApacheDubbo)

## 贡献

See [CONTRIBUTING](https://github.com/apache/incubator-dubbo/blob/master/CONTRIBUTING.md) for details on submitting patches and the contribution workflow.

### 我可以怎样贡献?

* Take a look at issues with tag called [`Good first issue`](https://github.com/apache/incubator-dubbo/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22) or [`Help wanted`](https://github.com/apache/incubator-dubbo/issues?q=is%3Aopen+is%3Aissue+label%3A%22help+wanted%22).
* Join the discussion on mailing list, subscription [guide](https://github.com/apache/incubator-dubbo/wiki/Mailing-list-subscription-guide).
* Answer questions on [issues](https://github.com/apache/incubator-dubbo/issues).
* Fix bugs reported on [issues](https://github.com/apache/incubator-dubbo/issues), and send us pull request.
* Review the existing [pull request](https://github.com/apache/incubator-dubbo/pulls).
* Improve the [website](https://github.com/apache/incubator-dubbo-website), typically we need
  * blog post
  * translation on documentation
  * use cases about how Dubbo is being used in enterprise system.
* Improve the [dubbo-admin/dubbo-monitor](https://github.com/apache/incubator-dubbo-admin).
* Contribute to the projects listed in [ecosystem](https://github.com/dubbo).
* Any form of contribution that is not mentioned above.
* If you would like to contribute, please send an email to dev@dubbo.incubator.apache.org to let us know!

## 报告错误

Please follow the [template](https://github.com/apache/incubator-dubbo/issues/new?template=dubbo-issue-report-template.md) for reporting any issues.

## 报告安全漏洞

Please report security vulnerability to [us](mailto:security@dubbo.incubator.apache.org) privately.

## Dubbo 生态

* [Dubbo Ecosystem Entry](https://github.com/dubbo) - A GitHub group `dubbo` to gather all Dubbo relevant projects not appropriate in [apache](https://github.com/apache) group yet
* [Dubbo Website](https://github.com/apache/incubator-dubbo-website) - Apache Dubbo (incubating) official website
* [Dubbo Samples](https://github.com/apache/incubator-dubbo-samples) - samples for Apache Dubbo (incubating)
* [Dubbo Spring Boot](https://github.com/apache/incubator-dubbo-spring-boot-project) - Spring Boot Project for Dubbo
* [Dubbo Admin](https://github.com/apache/incubator-dubbo-admin) - The reference implementation for Dubbo admin

#### 语言

* [Node.js](https://github.com/dubbo/dubbo2.js)
* [Python](https://github.com/dubbo/dubbo-client-py)
* [PHP](https://github.com/dubbo/dubbo-php-framework)
* [Go](https://github.com/dubbo/dubbo-go)

## 执照

Apache Dubbo is under the Apache 2.0 license. See the [LICENSE](https://github.com/apache/incubator-dubbo/blob/master/LICENSE) file for details.
