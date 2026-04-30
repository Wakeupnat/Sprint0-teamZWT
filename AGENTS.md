# AGENTS.md 酒店AI客服系统
## 架构概述
本项目为酒店AI客服系统，基于SpringBoot开发，集成LangChain4j对接通义千问大模型，Oracle数据库存储，实现自然语言交互、房间预订与信息查询功能。

## 目录结构
controller：接收前端HTTP请求；service：核心业务逻辑；repository：JPA数据访问；entity：数据库映射实体；config：全局跨域与框架配置；tool：大模型调用工具类；resources：配置文件与前端静态页面。

## 核心模块职责
Controller提供统一聊天接口，转发请求至业务层；Service负责预订校验、大模型交互与意图识别；Repository基于JPA完成无硬编码SQL数据库操作；Entity通过JPA注解映射数据表；Config管理跨域与全局配置；Tool封装预订查询工具与会话ID管理。

## 编码规范
统一驼峰命名，数据库字段下划线映射；类与核心方法必须加注释；禁用System.out，统一业务返回提示；接口统一POST、JSON传参、/api/chat前缀；使用Spring构造器/注解依赖注入。

## 禁止操作清单
Controller禁止编写业务逻辑；禁止硬编码SQL与敏感密钥；配置类仅做框架配置不写业务；工具类只封装方法不实现复杂业务；禁止随意修改核心实体字段与会话ID传递逻辑。