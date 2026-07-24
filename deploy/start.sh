#!/bin/bash
# KWChat 后端启动脚本

APP_NAME="kwchat-app"
JAR_FILE="/opt/kwchat/kwchat-app/target/kwchat-app-1.0.0-SNAPSHOT.jar"
LOG_DIR="/opt/kwchat/logs"
LOG_FILE="$LOG_DIR/kwchat.log"

# 检查 Java
if ! command -v java &> /dev/null; then
    echo "错误: 未找到 Java，请先安装 JDK 17"
    exit 1
fi

# 检查 JAR 包
if [ ! -f "$JAR_FILE" ]; then
    echo "错误: JAR 包不存在: $JAR_FILE"
    echo "请先执行: mvn clean package -DskipTests -pl kwchat-app -am"
    exit 1
fi

# 停止旧进程
PID=$(pgrep -f "$APP_NAME" 2>/dev/null)
if [ -n "$PID" ]; then
    echo "停止旧进程: $PID"
    kill -15 $PID
    sleep 5
    # 如果还没停，强制杀
    if kill -0 $PID 2>/dev/null; then
        kill -9 $PID
    fi
fi

# 创建日志目录
mkdir -p "$LOG_DIR"

# 启动（JVM 参数针对 4G 内存优化）
echo "启动 KWChat 后端..."
nohup java -Xms256m -Xmx512m -XX:+UseG1GC \
    -jar "$JAR_FILE" \
    > "$LOG_FILE" 2>&1 &

NEW_PID=$!
echo "启动成功, PID: $NEW_PID"
echo "日志: tail -f $LOG_FILE"

# 等待启动完成
echo "等待启动..."
for i in $(seq 1 30); do
    if curl -s http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
        echo "✅ 后端启动成功！"
        echo "API: http://localhost:8080/api/"
        exit 0
    fi
    sleep 2
done

echo "⚠️  启动超时，请检查日志: tail -f $LOG_FILE"
