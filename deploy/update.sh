#!/bin/bash
# KWChat 一键更新脚本
# 使用方法: ./update.sh

echo "=== 开始更新 KWChat ==="

# 拉取最新代码
echo "1. 拉取最新代码..."
cd /opt/kwchat/kwchat-master
git pull origin main

# 更新前端
echo "2. 构建用户端..."
cd /opt/kwchat/kwchat-frontend
npm run build

# 更新管理端
echo "3. 构建管理端..."
cd /opt/kwchat/kwchat-admin
npm run build

# 更新后端
echo "4. 构建后端..."
cd /opt/kwchat
mvn clean package -DskipTests -pl kwchat-app -am

# 重启后端
echo "5. 重启后端服务..."
kill $(pgrep -f kwchat-app) 2>/dev/null
nohup java -Xms256m -Xmx512m -XX:+UseG1GC \
    -jar /opt/kwchat/kwchat-app/target/kwchat-app-1.0.0-SNAPSHOT.jar \
    > /opt/kwchat/logs/kwchat.log 2>&1 &

echo "=== 更新完成！==="
echo "用户端: http://<服务器IP>/"
echo "管理端: http://<服务器IP>/admin/"
