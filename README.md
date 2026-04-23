# 🚀 Sprint0-teamZWT - 启航报告

## 📢 团队口号
**"ZWT三人行，代码必先行！"**  
*—— 协作共赢，代码无敌*

---

## 👥 团队成员及角色分配

| 姓名 | 学号 | Scrum角色 | 职责 |
|:----:|:----:|:----------|:-----|
| **赵文琦** | 9109223138 | **产品负责人 (Product Owner)** | 负责梳理需求、定义产品功能、优先级排序 |
| **王鸿艳** | 9109223131 | **Scrum Master** | 负责团队流程、消除障碍、确保Scrum实践 |
| **汤 骞** | 9109223177 | **开发人员 (Developer)** | 负责代码实现、测试和技术落地 |

---


## 📂 项目信息

- **项目名称**：Sprint0-teamZWT
- **仓库地址**：https://github.com/Wakeupnat/Sprint0-teamZWT
- **可见性**：公开
- **创建时间**：2026年3月

---

## 项目信息
- 项目名称：Sprint0-teamZWT
- 仓库地址：[https://github.com/Wakeupnat/Sprint0-teamZWT](https://github.com/Wakeupnat/Sprint0-teamZWT)
- 可见性：公开
- 创建时间：2026年3月
- 项目简介：本项目是**基于 Spring Boot 的酒店预订管理系统**，面向C端用户与B端酒店管理员，实现酒店查询、房型预订、在线支付、订单全流程管理等完整业务闭环。

## 📑 目录
1. [系统架构与模块关系](#系统架构与模块关系)
2. [本地开发环境搭建](#本地开发环境搭建)
3. [核心业务模块职责](#核心业务模块职责)
4. [CI/CD 流水线说明](#cicd-流水线说明)
5. [单元测试说明](#单元测试说明)

---

## 系统架构与模块关系
### 1. 整体分层架构图
```
┌────────────────────────┐
│   前端层              │
│ (Vue/React/移动端H5)   │
└───────────┬────────────┘
            │ RESTful API
┌───────────▼────────────┐
│         后端层         │
│ ┌──────┐  ┌──────┐     │
│ │Controller│→│Service │     │
│ └──────┘  └──────┘     │
│ ┌──────┐  ┌──────┐     │
│ │Mapper  │  │Common  │     │
│ └──────┘  └──────┘     │
└───────────┬────────────┘
            │
┌───────────▼────────────┐   ┌──────────────────────┐
│      数据层 MySQL/Redis │   │ 第三方服务(支付/短信/地图) │
└────────────────────────┘   └──────────────────────┘
```

### 2. 模块协作说明
| 层级 | 核心职责 |
|------|----------|
| **前端层** | 负责用户页面交互，通过标准 RESTful API 与后端完成数据通信 |
| **控制层 Controller** | 统一接收前端请求、完成参数合法性校验、路由分发、统一封装返回响应体 |
| **服务层 Service** | 系统核心，封装全部业务逻辑；调用持久层完成数据读写、联动通用模块与第三方外部服务 |
| **持久层 Mapper** | 基于 MyBatis/MyBatis-Plus 封装数据库CRUD操作，统一数据访问逻辑 |
| **通用模块 Common** | 全局工具类、统一全局异常处理器、常量枚举定义、统一返回值包装、公共工具能力 |
| **数据层** | MySQL 作为业务主库持久化核心数据；Redis 承担缓存、分布式锁、热点数据加速能力 |
| **第三方服务** | 对接外部能力：微信/支付宝支付、短信通知、地图定位、距离计算等 |

---

## 本地开发环境搭建
### 🔧 前置依赖要求
| 依赖名称 | 推荐版本 | 环境验证命令 |
|----------|----------|--------------|
| JDK | 1.8 / 11+ | `java -version` |
| Maven | 3.6+ | `mvn -v` |
| MySQL | 5.7 / 8.0 | `mysql -V` |
| Redis | 5.0+ | `redis-cli --version` |

### 📦 搭建详细步骤
#### 步骤1：安装并配置基础依赖
1.  JDK：下载对应版本并安装，配置系统 `JAVA_HOME` 全局环境变量
2.  Maven：官网下载安装，配置 `MAVEN_HOME`，替换为国内阿里镜像加速依赖下载
3.  MySQL：安装完成后，执行以下SQL初始化项目数据库
```sql
CREATE DATABASE IF NOT EXISTS hotel_booking DEFAULT CHARSET utf8mb4;
USE hotel_booking;
SOURCE /你的项目路径/sql/init.sql;
```
4.  Redis：安装后默认后台启动，如需访问密码，后续修改项目配置文件即可

#### 步骤2：拉取远程项目代码
```bash
# 克隆远程仓库到本地
git clone https://github.com/Wakeupnat/Sprint0-teamZWT.git

# 进入项目根目录
cd Sprint0-teamZWT
```

#### 步骤3：修改开发环境配置
打开 `src/main/resources/application-dev.yml`，修改本地数据库与Redis连接信息
```yaml
spring:
  # MySQL 数据库配置
  datasource:
    url: jdbc:mysql://localhost:3306/hotel_booking?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root         # 替换为你的MySQL账号
    password: 123456       # 替换为你的MySQL密码
    driver-class-name: com.mysql.cj.jdbc.Driver

  # Redis 缓存配置
  redis:
    host: localhost
    port: 6379
    password: ""    # 无密码则留空
    database: 0
```

#### 步骤4：启动项目
**方式1：IDEA可视化启动（推荐）**
1.  IDEA打开项目，等待Maven全部依赖加载完毕
2.  找到项目入口启动类 `HotelBookingApplication.java`
3.  右键文件 → 点击 `Run 'HotelBookingApplication'`

**方式2：Maven命令行启动**
```bash
mvn clean compile spring-boot:run
```

#### 步骤5：验证项目启动成功
1.  终端健康检查
```bash
curl http://localhost:8080/health
```
2.  浏览器直接访问
```
http://localhost:8080/swagger-ui.html
```
✅ 验证标准：接口返回 `{"status":"UP"}` 或成功打开Swagger接口文档页面，代表本地启动完全正常

---

## 核心业务模块职责
### 1. 用户模块（user-module）
- 核心职责：用户账号全生命周期、注册登录、信息维护、权限与角色体系管控
- 对外提供能力：
  - 用户账号/手机号注册、账号密码+验证码多方式登录
  - 用户个人信息查询、修改、账号注销能力
  - 角色权限分配、接口访问鉴权（对接Spring Security安全框架）
  - 登录Token签发、刷新、过期失效管理

### 2. 酒店模块（hotel-module）
- 核心职责：酒店基础档案、房型管理、价格策略、库存房态维护
- 对外提供能力：
  - 酒店列表、详情查询，支持多条件筛选、排序、分页加载
  - 房型、房价、入住规则的新增、编辑、上下线管理
  - 实时房型剩余库存查询、房态扣减与恢复

### 3. 订单模块（order-module）
- 核心职责：预订订单全生命周期、状态流转、支付与退款闭环
- 对外提供能力：
  - 预订订单创建、提交、主动取消能力
  - 支付结果回调接收、订单状态自动同步更新
  - 多维度订单列表、详情查询（用户/时间/订单状态筛选）
  - 退款申请提交、人工审核、退款执行全流程

### 4. 支付模块（pay-module）
- 核心职责：对接第三方支付渠道，统一封装支付与退款能力
- 对外提供能力：
  - 预支付订单创建、拉起收银台支付
  - 支付结果异步回调验签、安全校验、通知订单模块
  - 支付流水记录查询、原路退款能力

### 5. 通知模块（notify-module）
- 核心职责：全场景消息触达，保障用户信息同步
- 对外提供能力：
  - 短信验证码发送、校验能力
  - 订单预订成功、支付、取消、退款等状态变更主动通知
  - 站内信、邮件、自定义批量消息推送

---
