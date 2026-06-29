# task-mall

# task-mall - 后端服务
### 技术栈：  
java 21  
主框架（Spring boot 3.5.14）  
ORM框架（mybatis-flex）  
数据库（mysql8.0+）  
数据库表版本管理（liquibase，暂未使用）  
缓存（redis，lettuce客户端操作redis数据，redisson客户端用作分布式锁）  
权限框架（sa-token）  
依赖管理（maven 3.8+）  
计划引入消息队列 RocketMQ

### 启动：都是先执行对应子系统的sql脚本，然后启动各个子系统
mall-admin - 管理后台服务  
mall-mission - 任务子系统  
mall-promotion - 营销活动子系统  
mall-user - 用户（会员）子系统  
mall-wallet - 钱包子系统  
mall-invest - 项目投资子系统（暂时不做，将功能融合到任务子系统）
------------------------------------------------------------------------


# mall-admin-ui - 管理后台页面
### 技术栈：
主要框架（基于vue3）：https://arco.design/  
vue3 + node（版本：v20.19.6）+ pnpm（版本：10.34.4）
### 启动：
cd mall-admin-ui  
pnpm install  
pnpm run dev
------------------------------------------------------------------------

# mall-ui - 用户（会员）H5 页面
### 技术栈：
主要框架（基于vue3）：https://vant-ui.github.io/vant/#/zh-CN  
vue + node（版本：v20.19.6）+ pnpm（版本：10.34.4）  
### 启动：
cd mall-ui  
pnpm install  
pnpm run dev