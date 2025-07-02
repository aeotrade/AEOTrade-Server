#慧贸贸项目说明

##项目背景

###需求来源

1、以让国际贸易更便利为使命，让我们成为行业受尊敬的企业和员工为目标，公司开始从项目研发型逐步向互联网服务方式转型

2、公司通过长时间的海关技术与业务的积累，需要从服务角度输出能力

###市场背景
市场现状

1、中美贸易战导致贸易业务形式不容乐观，中美贸易量下降贸易公司需要寻找新的业务增长

2、中国一带一路国策推进，符合慧贸贸的长期发展，且国内并无互联网服务角度的同类型龙头企业

3、公司承建北京单一窗口2.0获取行业流量入口先机，为市场推广做好准备工作

4、北京大兴国际机场空港通设计，使公司得到行业业务优先权，可以有效促进京津冀地区的业务发展

5、市场整体业务运作现状处理非常原始的状态，已经到达升级进化的阶段，但由于传统行业的关系网络利益以及地域特点导致新模式的服务推进缓慢

6、从长期角度看，贸易行业主要痛点存在于三方面，第一是如果扩展增量市场与守住存量市场，第二如何提高业务服务能力和议价权力，第三业务传递过程过于私人化无法沉淀为企业所用且协作成本大

竞争格局

国内目前没有涉及到深入用户的全场景化服务的竞品，国内主流的企业切入点分为3个方向，物流的撮合交易，关务服务，与外贸综合服务平台，当前行业内知名的企业运去哪为物流撮合+自营模式该企业发展形式也未跳出传统货代的业务范围就目前看已经发展受限了

而外综服平台本身因为不具有流量所以很难形成有效规模，国内市场目前没有知名平台，关务服务角度切入的企业多以金融服务数据贷为主营的业务内容，当前这三类企业主流的收入构成均为物流业务利润+金融差价构成

发展趋势

当前国外新兴物流处于科技转型阶段flxport为典型的科技型，老牌物流如德迅等他们主要以服务综合性和质量取胜，主要涵盖了全流程的供应链线上服务

国内的物流企业由于缺乏业务模式与监管，导致整体行业服务混乱野蛮成长期的利润已经接近尾声，近几年中型货代已经开始往标准化，服务化方向转型，对于服务体系，技术能力，融合性均有较高企业需求，同时单一窗口全面启用之后大企业的传统关务环节也急需升级

未来国际物流乃至国际贸易已经需要向新业态进化，以更好适应中国的国际开放深度逐渐增强国际地位

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
