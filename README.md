# FireBird-TechVision 技术成长轨迹

## 项目简介
FireBird-TechVision 是我在大学期间持续更新的技术成长轨迹项目，记录了我的学习历程和技术实践。项目采用前后端分离架构，涵盖多种技术栈：

**前端**：Vue3 框架

**后端**：Flask (Python) 和 Spring Boot (Java)

**数据库​**：MySQL

## 项目结构FireBird-TechVision
```
├── flask-parts/            # Flask 后端项目
├── spring-parts/           # Spring Boot 后端项目
├── vue3-parts/             # Vue3 前端项目
├── sql-parts/              # 数据库相关
├── .gitignore
└── README.md
```
## 技术展示

### 已实现功能模块
**用户系统**
- 登录/注册功能 (Flask + Vue3)
- 用户信息管理 (Spring Boot)

**游戏项目**
- 飞机大战游戏 (Vue3)
- 迷宫游戏 (Vue3)
- 植物大战僵尸简化版 (Spring Boot)

**个人网站**
- 响应式个人简历网站
- 技术博客系统

### 技术栈详情

#### 前端技术
- Vue3 组合式API
- Vue Router
- Pinia 状态管理
- Element Plus UI 组件库
- Axios HTTP 请求

#### 后端技术
**Flask**:
- Flask-SQLAlchemy
- Flask-Login
- Flask-RESTful

**Spring Boot**:
- Spring Security
- Spring Data JPA
- MyBatis

#### 数据库
- MySQL 关系型数据库
- 数据库设计规范化
- 索引优化

## 如何运行

### 前端项目 (Vue3)cd vue3-parts
```
npm install
npm run dev
```
### Flask 后端cd flask-parts
```
pip install -r requirements.txt
python app.py
```
### Spring Boot 后端cd spring-parts
```
mvn spring-boot:run
```
## 项目进展
- 用户登录注册系统 (Flask + Vue3)
- 飞机大战游戏 (Vue3)
- 迷宫游戏 (Vue3)
- 植物大战僵尸完整版 (Spring Boot)
- 个人博客系统 (全栈)

## 学习收获
通过本项目，我深入理解了：

- 前后端分离开发模式
- RESTful API 设计
- 数据库设计与优化
- 多种编程语言的协同开发
- 版本控制与团队协作

## 未来计划
- 添加更多实战项目
- 实现微服务架构
- 引入Docker容器化
- 增加自动化测试
- 完善CI/CD流程

## 贡献
欢迎提出建议或贡献代码！请通过 Issues 或 Pull Requests 参与。
