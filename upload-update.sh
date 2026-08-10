#!/bin/bash
# KwChat 热更新上传脚本
# 使用方法: ./upload-update.sh

# ============================================
# 配置区域 - 修改这里的版本号和更新说明
# ============================================
VERSION="1.0.17"
NOTES="优化信息图标点击逻辑，支持单聊和群聊信息查看"
# ============================================

echo "=========================================="
echo "  KwChat 热更新上传脚本"
echo "=========================================="

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# 检查版本号是否已存在
check_version() {
    local zip_file="app-v${VERSION}.zip"
    if [ -f "kwchat-frontend/$zip_file" ]; then
        print_warning "版本 v${VERSION} 的 zip 包已存在"
        read -p "是否覆盖？(y/n): " confirm
        if [ "$confirm" != "y" ]; then
            print_error "上传已取消"
            exit 1
        fi
        rm -f "kwchat-frontend/$zip_file"
    fi
}

# 构建前端
build_frontend() {
    print_info "构建前端代码..."
    cd kwchat-frontend

    npm run build
    if [ $? -ne 0 ]; then
        print_error "前端构建失败"
        exit 1
    fi

    cd ..
    print_info "前端构建完成"
}

# 打包热更新包
package_update() {
    print_info "打包热更新 zip 包..."
    cd kwchat-frontend/dist

    zip -r "../app-v${VERSION}.zip" ./*
    if [ $? -ne 0 ]; then
        print_error "打包失败"
        exit 1
    fi

    cd ../..
    print_info "打包完成: app-v${VERSION}.zip"
}

# 上传到服务器
upload_to_server() {
    print_info "上传 zip 包到服务器..."

    # 上传 zip 文件
    scp "kwchat-frontend/app-v${VERSION}.zip" root@118.25.44.250:/opt/kwchat/uploads/updates/
    if [ $? -ne 0 ]; then
        print_error "上传 zip 包失败"
        exit 1
    fi

    print_info "上传版本文件..."

    # 创建版本文件
    cat > app-version.json << EOF
{
  "version": "${VERSION}",
  "url": "http://118.25.44.250:8080/api/uploads/updates/app-v${VERSION}.zip",
  "notes": "${NOTES}",
  "forceUpdate": false
}
EOF

    # 上传版本文件
    scp app-version.json root@118.25.44.250:/opt/kwchat/uploads/updates/
    if [ $? -ne 0 ]; then
        print_error "上传版本文件失败"
        exit 1
    fi

    print_info "上传完成"
}

# 清理本地临时文件
cleanup() {
    print_info "清理临时文件..."
    rm -f kwchat-frontend/app-v${VERSION}.zip
    rm -f app-version.json
}

# 显示结果
show_result() {
    echo ""
    echo "=========================================="
    print_info "热更新上传完成！"
    echo "=========================================="
    echo "  版本号: v${VERSION}"
    echo "  更新说明: ${NOTES}"
    echo "  下载地址: http://118.25.44.250:8080/api/uploads/updates/app-v${VERSION}.zip"
    echo ""
    echo "  用户打开APP后会自动检查更新"
    echo "  下次启动时生效"
    echo "=========================================="
}

# 主流程
main() {
    echo ""

    # 检查版本是否已存在
    check_version

    # 构建前端
    build_frontend

    # 打包热更新包
    package_update

    # 上传到服务器
    upload_to_server

    # 清理临时文件
    cleanup

    # 显示结果
    show_result
}

# 执行主流程
main
