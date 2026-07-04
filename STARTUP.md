# 快伟通 - 项目启动指南

本文档详细说明如何启动快伟通企业级即时通讯平台。

---

## 📋 环境要求

在启动项目之前，请确保已安装以下软件：

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17+ | Java开发环境 |
| Maven | 3.8+ | 项目构建工具 |
| Node.js | 18+ | 前端运行环境 |
| npm | 9+ | 包管理工具 |
| Docker | 20+ | 容器化平台 |
| Docker Compose | 2.0+ | 容器编排工具 |
| Git | 2.30+ | 版本控制工具 |

**验证环境：**

```bash
# 检查JDK版本
java -version

# 检查Maven版本
mvn -v

# 检查Node.js版本
node -v

# 检查npm版本
npm -v

# 检查Docker版本
docker -v

# 检查Docker Compose版本
docker-compose -v
```

---

## 🚀 启动步骤

### 第一步：克隆项目

```bash
# 克隆项目到本地
git clone https://github.com/your-username/kwchat.git

# 进入项目目录
cd kwchat
```

### 第二步：启动基础设施

使用Docker Compose一键启动所有基础服务：

```bash
# 进入docker目录
cd docker

# 启动所有服务
docker-compose up -d
```

**启动的服务包括：**

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3306 | 主数据库 |
| Redis | 6379 | 缓存服务 |
| MinIO | 9000/9001 | 对象存储（控制台） |
| RabbitMQ | 5672/15672 | 消息队列（管理界面） |
| Elasticsearch | 9200 | 搜索引擎 |
| Kibana | 5601 | ES可视化 |
| Prometheus | 9090 | 监控 |
| Grafana | 3000 | 监控可视化 |

**验证服务启动：**

```bash
# 查看容器状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

**访问各服务控制台：**

- MinIO控制台: http://localhost:9001
  - 用户名: `minioadmin`
  - 密码: `minioadmin123`

- RabbitMQ管理界面: http://localhost:15672
  - 用户名: `kwchat`
  - 密码: `kwchat123456`

- Grafana监控: http://localhost:3000
  - 用户名: `admin`
  - 密码: `admin123456`

### 第三步：初始化数据库

```bash
# 返回项目根目录
cd ..

# 执行数据库初始化脚本
mysql -h 127.0.0.1 -u root -p < sql/init.sql

# 执行RBAC权限控制脚本
mysql -h 127.0.0.1 -u root -p < sql/rbac.sql
```

**或者使用Docker MySQL：**

```bash
# 使用Docker MySQL执行脚本
docker exec -i kwchat-mysql mysql -u root -proot123456 < sql/init.sql
docker exec -i kwchat-mysql mysql -u root -proot123456 < sql/rbac.sql
```

**验证数据库初始化：**

```bash
# 连接MySQL
mysql -h 127.0.0.1 -u kwchat -pkuaitong123456 kwchat

# 查看表
SHOW TABLES;

# 查看角色数据
SELECT * FROM sys_role;

# 查看权限数据
SELECT * FROM sys_permission;

# 查看部门数据
SELECT * FROM sys_department;
```

### 第四步：启动后端服务

```bash
# 进入后端应用目录
cd kwchat-app

# 清理并编译项目
mvn clean install

# 启动Spring Boot应用
mvn spring-boot:run
```

**或者使用IDE启动：**

1. 使用IntelliJ IDEA打开项目根目录
2. 找到 `kwchat-app/src/main/java/com/kwp/chat/KuaiTongApplication.java`
3. 右键选择 `Run 'KuaiTongApplication'`

**启动成功标志：**

```
========================================
   快伟通启动成功！
   API文档: http://localhost:8080/api/doc.html
========================================
```

**验证后端服务：**

- API文档: http://localhost:8080/api/doc.html
- 健康检查: http://localhost:8080/api/actuator/health

### 第五步：启动聊天前端

**打开新的终端窗口：**

```bash
# 进入前端项目目录
cd kwchat-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

**启动成功标志：**

```
  VITE v5.x.x  ready in xxx ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: http://192.168.x.x:3000/
```

### 第六步：启动管理后台

**打开新的终端窗口：**

```bash
# 进入管理后台目录
cd kwchat-admin

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

**启动成功标志：**

```
  VITE v5.x.x  ready in xxx ms

  ➜  Local:   http://localhost:3001/
  ➜  Network: http://192.168.x.x:3001/
```

---

## 🔐 访问应用

### 聊天应用

- 地址: http://localhost:3000
- 默认账号: `admin` / `admin123`

### 管理后台

- 地址: http://localhost:3001
- 默认账号: `admin` / `admin123`

### API文档

- 地址: http://localhost:8080/api/doc.html

---

## 📊 服务端口汇总

| 服务 | 端口 | 地址 |
|------|------|------|
| 后端API | 8080 | http://localhost:8080 |
| 聊天前端 | 3000 | http://localhost:3000 |
| 管理后台 | 3001 | http://localhost:3001 |
| WebSocket | 9090 | ws://localhost:9090/ws |
| MySQL | 3306 | localhost:3306 |
| Redis | 6379 | localhost:6379 |
| MinIO | 9000 | http://localhost:9000 |
| MinIO控制台 | 9001 | http://localhost:9001 |
| RabbitMQ | 5672 | localhost:5672 |
| RabbitMQ管理 | 15672 | http://localhost:15672 |
| Elasticsearch | 9200 | http://localhost:9200 |
| Kibana | 5601 | http://localhost:5601 |
| Prometheus | 9090 | http://localhost:9090 |
| Grafana | 3000 | http://localhost:3000 |

---

## 🛑 停止服务

### 停止前端和后台

在运行前端和后台的终端中按 `Ctrl + C`

### 停止后端服务

在运行后端的终端中按 `Ctrl + C`

### 停止Docker服务

```bash
cd docker
docker-compose down
```

### 停止并清理数据

```bash
cd docker
docker-compose down -v  # 删除数据卷
```

---

## 📝 一键启动脚本

### Windows (start.bat)

```batch
@echo off
echo ========================================
echo 快伟通 - 企业级即时通讯平台
echo ========================================

echo.
echo [1/4] 启动基础设施...
cd docker
docker-compose up -d
cd ..

echo.
echo [2/4] 等待服务启动...
timeout /t 30

echo.
echo [3/4] 初始化数据库...
docker exec -i kwchat-mysql mysql -u root -proot123456 < sql/init.sql
docker exec -i kwchat-mysql mysql -u root -proot123456 < sql/rbac.sql

echo.
echo [4/4] 启动后端服务...
start cmd /k "cd kwchat-app && mvn spring-boot:run"

echo.
echo 等待后端服务启动...
timeout /t 60

echo.
echo 启动前端服务...
start cmd /k "cd kwchat-frontend && npm install && npm run dev"

echo.
echo 启动管理后台...
start cmd /k "cd kwchat-admin && npm install && npm run dev"

echo.
echo ========================================
echo 所有服务已启动！
echo.
echo 聊天应用: http://localhost:3000
echo 管理后台: http://localhost:3001
echo API文档: http://localhost:8080/api/doc.html
echo.
echo 默认账号: admin / admin123
echo ========================================
pause
```

### Linux/Mac (start.sh)

```bash
#!/bin/bash

echo "========================================"
echo "快伟通 - 企业级即时通讯平台"
echo "========================================"

echo ""
echo "[1/4] 启动基础设施..."
cd docker
docker-compose up -d
cd ..

echo ""
echo "[2/4] 等待服务启动..."
sleep 30

echo ""
echo "[3/4] 初始化数据库..."
docker exec -i kwchat-mysql mysql -u root -proot123456 < sql/init.sql
docker exec -i kwchat-mysql mysql -u root -proot123456 < sql/rbac.sql

echo ""
echo "[4/4] 启动后端服务..."
cd kwchat-app
mvn spring-boot:run &
cd ..
sleep 60

echo ""
echo "启动前端服务..."
cd kwchat-frontend
npm install
npm run dev &
cd ..

echo ""
echo "启动管理后台..."
cd kwchat-admin
npm install
npm run dev &
cd ..

echo ""
echo "========================================"
echo "所有服务已启动！"
echo ""
echo "聊天应用: http://localhost:3000"
echo "管理后台: http://localhost:3001"
echo "API文档: http://localhost:8080/api/doc.html"
echo ""
echo "默认账号: admin / admin123"
echo "========================================"
```

**赋予执行权限：**

```bash
chmod +x start.sh
```

---

## ❓ 常见问题

### 1. Docker启动失败

```bash
# 检查Docker是否运行
docker info

# 检查端口是否被占用
netstat -ano | findstr :3306  # Windows
lsof -i :3306                 # Linux/Mac

# 重新启动Docker
docker-compose down
docker-compose up -d
```

### 2. 数据库连接失败

```bash
# 检查MySQL是否启动
docker ps | grep mysql

# 检查数据库配置
cat kwchat-app/src/main/resources/application.yml

# 测试数据库连接
mysql -h 127.0.0.1 -u kwchat -pkuaitong123456 kwchat
```

### 3. 前端启动失败

```bash
# 清除npm缓存
npm cache clean --force

# 删除node_modules重新安装
rm -rf node_modules
npm install

# 检查Node.js版本
node -v
```

### 4. 后端编译失败

```bash
# 清理并重新编译
mvn clean install -DskipTests

# 检查JDK版本
java -version

# 检查Maven版本
mvn -version
```

### 5. WebSocket连接失败

```bash
# 检查WebSocket端口
netstat -ano | findstr :9090

# 检查防火墙设置
# 确保9090端口未被阻止
```

---

## 🔧 配置文件位置

| 配置 | 文件路径 |
|------|----------|
| 后端配置 | `kwchat-app/src/main/resources/application.yml` |
| 聊天前端配置 | `kwchat-frontend/.env.development` |
| 管理后台配置 | `kwchat-admin/.env.development` |
| Docker配置 | `docker/docker-compose.yml` |
| Nginx配置 | `docker/nginx/nginx.conf` |

---

如有其他问题，请查看项目README.md或提交Issue。