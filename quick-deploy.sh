#!/bin/bash

# 快速部署脚本（适用于服务器端有 git 仓库的情况）
# 使用方法: ./quick-deploy.sh

echo "=========================================="
echo "  KwChat 快速部署"
echo "=========================================="

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 1. 拉取最新代码
print_info "拉取最新代码..."
cd /opt/kwchat
git pull origin master
if [ $? -ne 0 ]; then
    print_error "拉取代码失败"
    exit 1
fi

# 2. 打包前端
print_info "打包用户端..."
cd /opt/kwchat/kwchat-frontend
npm run build
if [ $? -ne 0 ]; then
    print_error "用户端打包失败"
    exit 1
fi

print_info "打包管理端..."
cd /opt/kwchat/kwchat-admin
npm run build
if [ $? -ne 0 ]; then
    print_error "管理端打包失败"
    exit 1
fi

# 3. 打包后端
print_info "打包后端..."
cd /opt/kwchat/kwchat-app
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    print_error "后端打包失败"
    exit 1
fi

# 4. 重启服务
print_info "重启后端服务..."
cd /opt/kwchat

# 停止旧服务
pkill -f "kwchat-app" || true
sleep 2

# 启动新服务
JAR_FILE=$(ls -t kwchat-app/target/*.jar | head -1)
if [ -z "$JAR_FILE" ]; then
    print_error "未找到 jar 包"
    exit 1
fi

nohup java -jar "$JAR_FILE" > logs/kwchat.log 2>&1 &
sleep 5

# 5. 检查服务状态
if pgrep -f "kwchat-app" > /dev/null; then
    echo ""
    print_info "=========================================="
    print_info "  部署成功！"
    print_info "=========================================="
    print_info "  访问地址: https://118.25.44.250"
    print_info "  管理后台: https://118.25.44.250/admin/"
    echo ""
else
    print_error "服务启动失败，请检查日志"
    exit 1
fi
