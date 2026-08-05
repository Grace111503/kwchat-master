# KWChat 服务器部署指南

> 适用于：OpenCloudOS 9 / 腾讯云 4核4G 3M带宽

---

## 一、服务器初始配置

### 1.1 SSH 登录服务器后，更新系统

```bash
sudo yum update -y
```

### 1.2 安装 Docker + Docker Compose

```bash
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
sudo yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo systemctl enable --now docker

docker --version
docker compose version
```

### 1.3 安装 JDK 17

```bash
sudo yum install -y java-17-openjdk java-17-openjdk-devel
java -version
```

### 1.4 安装 Maven 3.9

```bash
cd /tmp
wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
sudo tar -xzf apache-maven-3.9.6-bin.tar.gz -C /opt/
sudo ln -s /opt/apache-maven-3.9.6/bin/mvn /usr/local/bin/mvn
mvn -version
```

### 1.5 安装 Node.js 18+

```bash
curl -fsSL https://rpm.nodesource.com/setup_18.x | sudo bash -
sudo yum install -y nodejs
node -v && npm -v
```

### 1.6 安装 Git 和 Nginx

```bash
sudo yum install -y git nginx
```

### 1.7 配置防火墙和安全组

```bash
# 开放端口（或在腾讯云安全组放行）
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --reload
```

**⚠️ 腾讯云安全组配置（重要）：**

登录腾讯云控制台 → 云服务器 → 安全组，添加以下入站规则：

| 端口 | 协议 | 用途 | 是否必须 |
|------|------|------|----------|
| 22 | TCP | SSH登录 | ✅ 必须 |
| 80 | TCP | HTTP (Nginx) | ✅ 必须 |
| 443 | TCP | HTTPS (可选) | ⚪ 可选 |
| 9001 | TCP | MinIO控制台 | ⚪ 可选（管理文件用） |
| 15672 | TCP | RabbitMQ管理 | ⚪ 可选（调试用） |

> **注意**：MySQL(3307)、Redis(6379)、MinIO(9000)、RabbitMQ(5672)、Elasticsearch(9200) 等端口**不需要对外暴露**，因为都在Docker内网中，通过localhost访问。

---

## 二、上传项目代码

### 方式一：Git 拉取（推荐）

```bash
cd /opt
git clone https://github.com/Grace111503/kwchat-master.git kwchat
```

### 方式二：本地打包上传

本地执行：
```bash
cd kwchat-master
tar -czf kwchat.tar.gz --exclude=node_modules --exclude=target --exclude=.git .
scp kwchat.tar.gz root@<服务器IP>:/opt/
```

服务器上解压：
```bash
cd /opt && mkdir -p kwchat && tar -xzf kwchat.tar.gz -C kwchat
```

---

## 三、启动中间件

### 3.1 启动所有服务

使用项目自带的 `docker-compose.yml`：

```bash
cd /opt/kwchat/docker
docker compose up -d
```

等待 1-2 分钟，验证启动状态：
```bash
docker compose ps
```

### 3.2 关闭不需要的服务（省内存）

4G 内存跑全部服务会很紧张，关掉 Prometheus 和 Grafana 省 ~400MB：

```bash
docker stop kwchat-prometheus kwchat-grafana
```

最终保留的服务：

| 服务 | 端口 | 状态 |
|------|------|------|
| MySQL | 3306 | ✅ 运行 |
| Redis | 6379 | ✅ 运行 |
| MinIO | 9000/9001 | ✅ 运行 |
| RabbitMQ | 5672/15672 | ✅ 运行 |
| Elasticsearch | 9200/9300 | ✅ 运行 |
| Kibana | 5601 | ✅ 运行 |
| Prometheus | 9090 | ❌ 已关闭 |
| Grafana | 3001 | ❌ 已关闭 |

### 3.3 创建文件存储目录

```bash
# 创建本地文件存储目录（代替MinIO，节省内存）
mkdir -p /opt/kwchat/uploads/{image,video,voice,avatar,file}
```

### 3.4 验证 MySQL 初始化

```bash
docker exec -it kwchat-mysql mysql -uroot -proot123456 -e "USE kuaitong; SHOW TABLES;"
```

---

## 四、修改后端配置

### 4.1 密码确认（已正确，无需修改）

`docker-compose.yml` 和 `application.yml` 中的密码已经一致，**不需要修改**：

| 服务 | docker-compose.yml | application.yml | 状态 |
|------|-------------------|-----------------|------|
| MySQL root | `root123456` | `root123456` | ✅ 已一致 |
| Redis | `redis123` | `redis123` | ✅ 已一致 |
| RabbitMQ | `dev_admin` / `123456` | `dev_admin` / `123456` | ✅ 已一致 |
| MinIO | `minioadmin` / `minioadmin123` | `minioadmin` / `minioadmin123` | ✅ 已一致 |

### 4.2 修改生产环境配置

编辑 `kwchat-app/src/main/resources/application.yml`，修改以下内容：

```yaml
# 1. 切换为生产环境（约第10行）
spring.profiles.active: prod              # 原来是 dev

# 2. 关闭 SQL 日志、DEBUG 日志（省内存）
# 删掉或注释掉 StdOutImpl 那行（约第136行）：
#   log-impl: org.apache.ibatis.logging.stdout.StdOutImpl    ← 删掉这行

# 3. 日志级别改为 INFO（约第162行）
logging.level.com.kwp.chat: INFO         # 原来是 DEBUG
logging.level.org.springframework.web: INFO  # 原来是 DEBUG

# 4. 关闭 Knife4j API 文档（约第157行）
knife4j.enable: false                     # 原来是 true

# 5. 本地文件存储路径（约第154行）
file.storage.local.path: /opt/kwchat/uploads  # 原来是 Windows 路径
```

### 4.3 前端配置确认

编辑 `kwchat-frontend/.env.production`，确认 WebSocket 地址：

```bash
VITE_WS_URL=ws://118.25.44.250/ws   # 替换为你的服务器IP
```

### 4.3 其他优化配置（可选但建议）

```yaml
# Netty WebSocket 配置保持不变
netty.websocket.port: 9092

# AI 关闭
ai.enabled: false

# Actuator 只暴露基础端点
management.endpoints.web.exposure.include: health,info
```

---

## 五、构建并启动后端

### 5.1 构建 JAR 包

```bash
cd /opt/kwchat
mvn clean package -DskipTests -pl kwchat-app -am
```

构建成功后产物在：`kwchat-app/target/kwchat-app-1.0.0-SNAPSHOT.jar`

### 5.2 启动后端

```bash
# 创建日志目录和文件存储目录
mkdir -p /opt/kwchat/logs
mkdir -p /opt/kwchat/uploads/{image,video,voice,avatar,file}

# 启动（JVM 内存针对 4G 优化）
nohup java -Xms256m -Xmx512m -XX:+UseG1GC \
    -jar /opt/kwchat/kwchat-app/target/kwchat-app-1.0.0-SNAPSHOT.jar \
    > /opt/kwchat/logs/kwchat.log 2>&1 &

echo "PID: $!"
```

查看日志确认启动成功：
```bash
tail -f /opt/kwchat/logs/kwchat.log
```

看到 `Started KwchatApplication in X seconds` 就表示成功。

### 5.3 创建开机自启（可选）

```bash
# 先确认JVM路径
JAVA_PATH=$(dirname $(dirname $(readlink -f $(which java))))
echo "JVM路径: $JAVA_PATH"

sudo tee /etc/systemd/system/kwchat.service << EOF
[Unit]
Description=KWChat Backend
After=network.target docker.service
Requires=docker.service

[Service]
Type=simple
WorkingDirectory=/opt/kwchat
ExecStart=$JAVA_PATH/bin/java -Xms256m -Xmx512m -XX:+UseG1GC -jar /opt/kwchat/kwchat-app/target/kwchat-app-1.0.0-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=append:/opt/kwchat/logs/kwchat.log
StandardError=append:/opt/kwchat/logs/kwchat.log

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable kwchat
sudo systemctl start kwchat
```

---

## 六、构建前端

### 6.1 用户端 (kwchat-frontend)

```bash
cd /opt/kwchat/kwchat-frontend
npm install --registry=https://registry.npmmirror.com
npm run build
```

### 6.2 管理端 (kwchat-admin)

```bash
cd /opt/kwchat/kwchat-admin
npm install --registry=https://registry.npmmirror.com
npm run build
```

---

## 七、配置 Nginx

### 7.1 写入配置（无域名，用 IP 直接访问）

```bash
sudo tee /etc/nginx/conf.d/kwchat.conf << 'NGINX_EOF'
server {
    listen 80;
    server_name _;

    # 用户端
    location / {
        root /opt/kwchat/kwchat-frontend/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 管理端（通过 /admin 路径访问）
    location /admin/ {
        alias /opt/kwchat/kwchat-admin/dist/;
        index index.html;
        try_files $uri $uri/ /admin/index.html;
    }

    # 后端 API
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 30s;
        proxy_read_timeout 60s;
        client_max_body_size 100m;
    }

    # WebSocket
    location /ws {
        proxy_pass http://127.0.0.1:9092;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_read_timeout 3600s;
        proxy_send_timeout 3600s;
    }

    # 静态资源（上传的文件）
    location /uploads/ {
        alias /opt/kwchat/uploads/;
        expires 30d;
        add_header Cache-Control "public, immutable";
        # 允许跨域
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods "GET, POST, OPTIONS";
        add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range";
    }
}
NGINX_EOF
```

### 7.2 测试并重载

```bash
sudo nginx -t
sudo systemctl reload nginx
```

---

## 八、验证部署

```bash
# 检查中间件
cd /opt/kwchat/docker && docker compose ps

# 检查后端
curl http://localhost:8080/api/actuator/health

# 检查 Nginx
curl -I http://localhost
```

浏览器打开 `http://<服务器IP>`，登录后控制台显示 `WebSocket连接成功` 即部署完成。

---

## 九、内存分配（4G 服务器）

| 组件 | 内存 |
|------|------|
| 系统 | ~400MB |
| MySQL | ~512MB |
| Redis | ~256MB |
| MinIO | ~256MB |
| RabbitMQ | ~256MB |
| Elasticsearch | ~512MB |
| Kibana | ~500MB |
| Java 后端 | 512MB (Xmx) |
| Nginx | ~50MB |
| **合计** | **~3.2GB** |

剩余 ~800MB 缓冲。如果还紧张，随时可以：
```bash
docker stop kwchat-kibana     # 省 ~500MB
docker stop kwchat-elasticsearch  # 省 ~512MB
```

---

## 十、常见问题

**Q: MySQL 没有表？**
A: 删除 volume 重新初始化：
```bash
cd /opt/kwchat/docker
docker compose down -v
docker compose up -d
```

**Q: 前端白屏？**
A: 检查 Nginx 配置的 `try_files` 确保 SPA 路由回退到 `index.html`。

**Q: WebSocket 连不上？**
A: 确认 Nginx 的 `/ws` 配置了 `Upgrade` 和 `Connection` 头。

**Q: 后端启动失败？**
A: 检查密码是否对齐：`docker exec -it kwchat-mysql mysql -uroot -proot123456 -e "SELECT 1"`

**Q: 重启服务器后服务没了？**
A: Docker 已设置 `restart: always`，会自动重启。后端需要配置 systemd 自启（见 5.3 节）。

**Q: MinIO上传文件报错？**
A: 检查MinIO bucket是否创建：访问 `http://<服务器IP>:9001`，用 `minioadmin`/`minioadmin123` 登录，确保 `kuaitong` bucket存在。

**Q: Elasticsearch启动失败？**
A: 4G内存可能不够，可以关闭ES：`docker stop kwchat-elasticsearch kwchat-kibana`

**Q: 如何查看后端日志？**
A: `tail -f /opt/kwchat/logs/kwchat.log` 或 `docker logs kwchat-mysql`

---

## 快速部署命令汇总

```bash
# 1. 安装环境
sudo yum update -y
sudo yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin java-17-openjdk java-17-openjdk-devel git nginx
sudo systemctl enable --now docker

# 2. 安装 Maven
cd /tmp && wget https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz && sudo tar -xzf apache-maven-3.9.9-bin.tar.gz -C /opt/ && sudo ln -s /opt/apache-maven-3.9.9/bin/mvn /usr/local/bin/mvn

# 3. 安装 Node.js
curl -fsSL https://rpm.nodesource.com/setup_18.x | sudo bash - && sudo yum install -y nodejs

# 4. 上传代码
cd /opt && git clone <仓库地址> kwchat

# 5. 启动中间件 + 关掉不需要的
cd /opt/kwchat/docker && docker compose up -d
docker stop kwchat-prometheus kwchat-grafana

# 6. 确认 application.yml 密码（已正确，无需修改）
# MySQL→root123456, Redis→redis123, 但需要改：
#   - spring.profiles.active: prod
#   - 删除 log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
#   - 日志级别改为 INFO

# 7. 构建后端
cd /opt/kwchat && mvn clean package -DskipTests -pl kwchat-app -am

# 8. 启动后端
mkdir -p /opt/kwchat/logs
mkdir -p /opt/kwchat/uploads/{image,video,voice,avatar,file}
nohup java -Xms256m -Xmx512m -XX:+UseG1GC -jar kwchat-app/target/kwchat-app-1.0.0-SNAPSHOT.jar > /opt/kwchat/logs/kwchat.log 2>&1 &

# 9. 构建前端
cd /opt/kwchat/kwchat-frontend && npm install --registry=https://registry.npmmirror.com && npm run build
cd /opt/kwchat/kwchat-admin && npm install --registry=https://registry.npmmirror.com && npm run build

# 10. 配置 Nginx（复制上面的配置到 /etc/nginx/conf.d/kwchat.conf）
sudo nginx -t && sudo systemctl reload nginx

# 11. 访问 http://<服务器IP>
```
