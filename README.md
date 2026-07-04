# 快伟通 - 企业级即时通讯平台

快伟通是快伟派科技有限公司推出的企业级即时通讯平台，支持多端部署，提供安全可靠的沟通协作体验。

## ✨ 功能特性

### 💬 即时通讯
- 单聊和群聊支持
- 多模态消息：文本、图片、文件、视频、语音
- 消息已读/未读状态
- 消息撤回（2分钟内）
- 消息引用和转发
- 历史消息搜索

### 👥 好友系统
- 好友申请/同意/拒绝
- 好友分组管理
- 好友备注
- 黑名单功能

### 🏢 群组管理
- 创建群聊
- 邀请/移除成员
- 群公告
- 群管理员设置
- @功能
- 群消息免打扰

### 🤖 AI助手（开发中）
- 智能摘要：一键总结聊天记录
- 消息翻译：多语言实时翻译
- 知识库问答：基于企业文档的RAG问答

### 🔒 安全特性
- JWT Token认证
- 端到端加密（规划中）
- 敏感信息脱敏
- 操作审计日志

### 📱 多端支持
- Web端（Vue3 + Element Plus）
- PC桌面端（Electron）
- 移动端（uni-app，支持iOS/Android）

## 🛠️ 技术栈

### 后端技术
| 技术 | 说明 | 版本 |
|------|------|------|
| Spring Boot | 基础框架 | 3.2.5 |
| MyBatis-Plus | ORM框架 | 3.5.5 |
| MySQL | 主数据库 | 8.0+ |
| Redis | 缓存 | 7.0+ |
| RabbitMQ | 消息队列 | 3.12+ |
| MinIO | 对象存储 | 最新版 |
| Elasticsearch | 搜索引擎 | 8.12+ |
| Netty | WebSocket服务 | 4.1.100 |
| JWT | 认证 | 0.12.5 |
| Knife4j | API文档 | 4.4.0 |

### 前端技术
| 技术 | 说明 | 版本 |
|------|------|------|
| Vue 3 | 前端框架 | 3.4+ |
| Vite | 构建工具 | 5.1+ |
| Element Plus | UI组件库 | 2.5+ |
| Pinia | 状态管理 | 2.1+ |
| Vue Router | 路由 | 4.3+ |
| Axios | HTTP客户端 | 1.6+ |
| Electron | 桌面端框架 | 28+ |
| uni-app | 移动端框架 | Vue3 |

### 基础设施
| 技术 | 说明 |
|------|------|
| Docker | 容器化 |
| Docker Compose | 容器编排 |
| Nginx | 反向代理 |
| Prometheus | 监控 |
| Grafana | 可视化 |
| ELK | 日志系统 |

## 📁 项目结构

```
kwchat/
├── kwchat-common/            # 公共模块
├── kwchat-model/             # 实体模块
├── kwchat-dao/               # 数据访问层
├── kwchat-service/           # 业务逻辑层
├── kwchat-api/               # API接口层
├── kwchat-websocket/         # WebSocket服务
├── kwchat-app/               # 应用启动模块
├── kwchat-frontend/          # 前端项目
│   ├── src/
│   │   ├── api/               # API接口
│   │   ├── assets/            # 静态资源
│   │   ├── components/        # 公共组件
│   │   ├── layouts/           # 布局组件
│   │   ├── router/            # 路由配置
│   │   ├── store/             # 状态管理
│   │   ├── styles/            # 样式文件
│   │   ├── utils/             # 工具类
│   │   └── views/             # 页面组件
│   ├── public/                # 公共资源
│   └── package.json           # 依赖配置
├── docker/                    # Docker配置
│   ├── docker-compose.yml     # 容器编排
│   └── prometheus/            # Prometheus配置
├── sql/                       # 数据库脚本
│   └── init.sql               # 初始化脚本
└── README.md                  # 项目说明
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- Docker & Docker Compose
- MySQL 8.0+
- Redis 7.0+

### 1. 克隆项目

```bash
git clone https://github.com/your-username/kwchat.git
cd kwchat
```

### 2. 启动基础设施

```bash
cd docker
docker-compose up -d
```

这将启动以下服务：
- MySQL (端口: 3306)
- Redis (端口: 6379)
- MinIO (端口: 9000, 控制台: 9001)
- RabbitMQ (端口: 5672, 管理界面: 15672)
- Elasticsearch (端口: 9200)
- Kibana (端口: 5601)
- Prometheus (端口: 9090)
- Grafana (端口: 3000)

### 3. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

### 4. 启动后端服务

```bash
cd kwchat-parent
mvn clean install
cd kwchat-app
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

API文档地址: http://localhost:8080/api/doc.html

### 5. 启动前端服务

```bash
cd kwchat-frontend
npm install
npm run dev
```

前端服务将在 http://localhost:3000 启动

### 6. 访问应用

打开浏览器访问 http://localhost:3000

默认管理员账号：
- 用户名: admin
- 密码: admin123

## 📦 部署

### Docker部署

```bash
# 构建后端镜像
cd kwchat-parent
mvn clean package -DskipTests
docker build -t kwchat-backend .

# 构建前端镜像
cd kwchat-frontend
npm run build
docker build -t kwchat-frontend .

# 启动所有服务
cd docker
docker-compose -f docker-compose.prod.yml up -d
```

### 传统部署

#### 后端部署

```bash
# 打包
cd kwchat-parent
mvn clean package -DskipTests

# 运行
java -jar kwchat-app/target/kwchat-app-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

#### 前端部署

```bash
# 构建
cd kwchat-frontend
npm run build

# 部署到Nginx
cp -r dist/* /usr/share/nginx/html/
```

#### Nginx配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # WebSocket代理
    location /ws/ {
        proxy_pass http://localhost:8080/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
}
```

## 🔧 配置说明

### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/kwchat?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

### Redis配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your_password
```

### MinIO配置

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: your_access_key
  secret-key: your_secret_key
  bucket-name: kwchat
```

### JWT配置

```yaml
jwt:
  secret: your_jwt_secret_key
  expiration: 86400000  # 24小时
  refresh-expiration: 604800000  # 7天
```

## 📊 监控

### Prometheus

访问 http://localhost:9090 查看监控指标

### Grafana

访问 http://localhost:3000 查看可视化面板

默认账号：
- 用户名: admin
- 密码: admin123456

## 🧪 测试

### 单元测试

```bash
cd kwchat-parent
mvn test
```

### API测试

使用Knife4j进行API测试：http://localhost:8080/api/doc.html

### 性能测试

```bash
# 使用JMeter进行压力测试
jmeter -n -t test-plan.jmx -l results.jtl
```

## 📝 开发规范

### 代码规范

- 使用阿里巴巴Java开发手册规范
- 使用ESLint进行前端代码检查
- 使用Prettier进行代码格式化

### Git规范

- 提交信息格式: `type(scope): subject`
- 类型: feat, fix, docs, style, refactor, test, chore

### 分支管理

- main: 主分支
- develop: 开发分支
- feature/*: 功能分支
- hotfix/*: 紧急修复分支

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情
