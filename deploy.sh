#!/bin/bash

# KwChat 部署脚本
# 使用方法: ./deploy.sh

echo "=========================================="
echo "  KwChat 自动部署脚本"
echo "=========================================="

# 配置
SERVER="root@118.25.44.250"
REMOTE_DIR="/opt/kwchat"
LOCAL_DIR=$(pwd)

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# 检查本地是否有未提交的修改
check_git_status() {
    print_info "检查 Git 状态..."
    if [ -n "$(git status -s)" ]; then
        print_warning "检测到未提交的修改"
        read -p "是否继续部署？(y/n): " confirm
        if [ "$confirm" != "y" ]; then
            print_error "部署已取消"
            exit 1
        fi
    fi
}

# 打包前端
build_frontend() {
    print_info "打包用户端前端..."
    cd "$LOCAL_DIR/kwchat-frontend"
    npm run build
    if [ $? -ne 0 ]; then
        print_error "用户端打包失败"
        exit 1
    fi

    print_info "打包管理端前端..."
    cd "$LOCAL_DIR/kwchat-admin"
    npm run build
    if [ $? -ne 0 ]; then
        print_error "管理端打包失败"
        exit 1
    fi

    cd "$LOCAL_DIR"
}

# 打包后端
build_backend() {
    print_info "打包后端..."
    cd "$LOCAL_DIR/kwchat-app"
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        print_error "后端打包失败"
        exit 1
    fi
    cd "$LOCAL_DIR"
}

# 上传文件
upload_files() {
    print_info "上传前端文件到服务器..."
    scp -r "$LOCAL_DIR/kwchat-frontend/dist/"* "$SERVER:$REMOTE_DIR/kwchat-frontend/dist/"

    print_info "上传管理端文件到服务器..."
    scp -r "$LOCAL_DIR/kwchat-admin/dist/"* "$SERVER:$REMOTE_DIR/kwchat-admin/dist/"

    print_info "上传后端 jar 包到服务器..."
    JAR_FILE=$(ls -t "$LOCAL_DIR/kwchat-app/target/"*.jar | head -1)
    scp "$JAR_FILE" "$SERVER:$REMOTE_DIR/"
}

# 重启服务
restart_service() {
    print_info "重启后端服务..."
    ssh "$SERVER" << 'EOF'
        # 停止旧服务
        pkill -f "kwchat-app" || true

        # 等待进程完全停止
        sleep 2

        # 查找最新的 jar 包
        cd /opt/kwchat
        JAR_FILE=$(ls -t kwchat-app-*.jar | head -1)

        if [ -z "$JAR_FILE" ]; then
            echo "错误: 未找到 jar 包"
            exit 1
        fi

        # 启动新服务
        nohup java -jar "$JAR_FILE" > /dev/null 2>&1 &

        # 等待服务启动
        sleep 5

        # 检查服务是否启动成功
        if pgrep -f "kwchat-app" > /dev/null; then
            echo "服务启动成功"
        else
            echo "服务启动失败"
            exit 1
        fi
EOF
}

# 验证部署
verify_deployment() {
    print_info "验证部署..."

    # 检查服务是否运行
    ssh "$SERVER" "pgrep -f kwchat-app > /dev/null"
    if [ $? -eq 0 ]; then
        print_info "后端服务运行正常"
    else
        print_error "后端服务未运行"
        exit 1
    fi

    # 检查 HTTP 访问
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "http://118.25.44.250" --max-time 10)
    if [ "$HTTP_STATUS" = "301" ] || [ "$HTTP_STATUS" = "200" ]; then
        print_info "HTTP 访问正常 (状态码: $HTTP_STATUS)"
    else
        print_warning "HTTP 访问异常 (状态码: $HTTP_STATUS)"
    fi

    # 检查 HTTPS 访问
    HTTPS_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "https://118.25.44.250" -k --max-time 10)
    if [ "$HTTPS_STATUS" = "200" ]; then
        print_info "HTTPS 访问正常 (状态码: $HTTPS_STATUS)"
    else
        print_warning "HTTPS 访问异常 (状态码: $HTTPS_STATUS)"
    fi

    echo ""
    print_info "部署完成！"
    echo "=========================================="
    echo "  访问地址: https://118.25.44.250"
    echo "  管理后台: https://118.25.44.250/admin/"
    echo "=========================================="
}

# 主流程
main() {
    echo ""
    print_info "开始部署..."
    echo ""

    # 检查 git 状态
    check_git_status

    # 打包前端
    build_frontend

    # 打包后端
    build_backend

    # 上传文件
    upload_files

    # 重启服务
    restart_service

    # 验证部署
    verify_deployment
}

# 执行主流程
main
