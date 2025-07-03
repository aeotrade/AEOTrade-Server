#慧贸OS项目说明

## 平台技术架构

![biz_platform](https://github.com/user-attachments/assets/e8935be9-45a7-4383-8e48-a9e7d689ec91)


## 系统应用架构

![image](https://github.com/user-attachments/assets/e00b704a-bfc2-4b3d-86fc-a2b063d20da0)



##平台目录结构

```
 aeotrade-common-starter
	aeotrade-common-starter 公共包starter
	aeotrade-adapay-starter 支付接口（有redmi文件）
	aeotrade-common-api 公共api
	aeotrade-commom-base-starter 公共包
	aeotrade-common-chainmaker上链 sdk 公共包
	aeotrade-common-datasource-starter 数据源公共包
	aeotrade-common-doc 接口文档公共starter
	aeotrade-common-logging-starter日志处理
	aeotrade-redis-starter缓存公共starter
	aeotrade-security-starter 权限安全相关
	aeotrade-stream-rabbit-starter 队列公共starter
	aeotrade-websocket-starter 长链接starter

 aeotrade-server 服务提供
	aeotrade-boot-admin 服务监控
	aeotrade-server-chain 区块链组织添加证书相关接口
	aeotrade-server-eol
	aeotrade-server-gateway 网关
	aeotrade-server-generator 代码生成
	aeotrade-server-message 消息系统
	aeotrade-server-pay 支付模块
	aeotrade-server-sidecar-atcl 异构模块对接中秋
	aeotrade-server-statistics 统计数据下发模块
	aeotrade-server-log 日志模块
 aeotrade-datacenter 保管箱
    aeotrade-datacenter-admin 保管箱监控
    aeotrade-datacenter-dec 数据接收
    aeotrade-datacenter-dispose 上链失败数据处理
    aeotrade-datacenter-kafka kafka发送相关工具
    aeotrade-datacenter-streaming 数据解析
    aeotrade-exchange-chainmaker 交换数据上链模块
 aeotrade-msmq-rabbit 队列转换工程

 aeotrade-provider
 	aeotrade-provider-admin 权限控制
 	aeotrade-provider-atcl  保管箱接口提供
 	aeotrade-provider-cloud 慧贸云
 	aeotrade-provider-file 文件工程
 	aeotrade-provider-mamber 会员模块
 	aeotrade-provider-oauth 认证授权
 	aeotrade-provider-pms 栏目商城
 	aeotrade-provider-service 客服系统
 	aeotrade-provider-uac 用户信息企业模块
 	aeotrade-provider-weixin 咨询模块

aeotrade-python-transit python队列转换
aeotrade-python-transit-msmq python队列转换
aeotrade-sidecar-flask-server pythonflask异构服务
aeotrade-sidecar-server 异构服务
    



```
集成python

https://github.com/CarpenterGISer/springcloud-python




**消息模块**



**一.消息发送(生产者)**

1.在Mqconfig中指定通道

![image](https://github.com/user-attachments/assets/6782608a-9c8c-4969-892d-b1e89ae83a2e)


2.调用MqSend的send方法,头部传入解析类名,消息.

![image](https://github.com/user-attachments/assets/7566f071-846f-4fb7-80ee-a2c10e274ea8)


3.在需要发送的工程中引入依赖

````
<dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-stream</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
        </dependency>
````
4.yml添加如下配置

````
  cloud:
    stream:
      binders:
        defaultRabbit:
          type: rabbit
          environment: #配置rabbimq连接环境
            spring:
              rabbitmq:
                host: localhost
                username: aeotrade
                password: hmtx20191001
                virtual-host: /
      bindings:
        sendOutput:
          destination: queue.messages  #exchange名称，交换模式默认是topic
          content-type: application/json #消息格式
          group: outputgroup #分组
          consumer:
            max-attempts: 2 #失败重试次数
````
5.启动类添加 @EnableBinding(value ={MqConfig.class}) 注解

二.消息消费者,消息系统aeotrade-provider-message

**2.1消息接收系统类图(消费者)**
![image](https://github.com/user-attachments/assets/d1504106-80a1-443e-8a16-aa2ada414095)


添加新的消息消费时 , 定义类名,继承AbstractComponent抽象类重写 抽象方法

2.2 WebSocket

NOTICE：

This software is licensed under the GNU Lesser General Public License (LGPL) version 3.0 or later. However, it is not permitted to use this software for commercial purposes without explicit permission from the copyright holder.
If the above restrictions are violated, all commercial profits generated during unauthorized commercial use shall belong to the copyright holder. 
The copyright holder reserves the right to pursue legal liability against infringers through legal means, including but not limited to demanding the cessation of infringement and compensation for losses suffered as a result of infringement.
本软件根据GNU较宽松通用公共许可证（LGPL）3.0或更高版本获得许可。但是，未经版权所有者明确许可，不得将本软件用于商业目的。
若违反上述限制，在未经授权的商业化使用过程中所产生的一切商业收益，均归版权所有者。
版权所有者保留通过法律途径追究侵权者法律责任的权利，包括但不限于要求停止侵权行为、赔偿因侵权行为所遭受的损失等。
