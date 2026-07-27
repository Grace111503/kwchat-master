# KWChat 日常更新部署指南

> 首次部署请参考 `README.md`，本文档用于日常代码更新。

---

## 更新流程

### 1. 本地推送代码到 GitHub

```bash
cd D:\KuaiTong\kwchat
git add .
git commit -m "更新说明"
git push origin master
```

### 2. 服务器拉取最新代码

```bash
ssh root@<服务器IP>
cd /opt/kwchat
git pull origin master
```

### 3. 根据改动类型重新构建

#### 改了前端（kwchat-frontend）

```bash
cd /opt/kwchat/kwchat-frontend
npm run build
```

#### 改了管理端（kwchat-admin）

```bash
cd /opt/kwchat/kwchat-admin
npm run build
```

#### 改了后端（kwchat-app）

```bash
# 重新构建
cd /opt/kwchat
mvn clean package -DskipTests -pl kwchat-app -am

# 停止旧进程
kill $(pgrep -f kwchat-app)

# 启动新进程
nohup java -Xms256m -Xmx512m -XX:+UseG1GC \
    -jar /opt/kwchat/kwchat-app/target/kwchat-app-1.0.0-SNAPSHOT.jar \
    > /opt/kwchat/logs/kwchat.log 2>&1 &
```

如果配置了 systemd 自启，可以用：

```bash
sudo systemctl restart kwchat
```

---

## 一键更新脚本

将以下内容保存为 `update.sh`，每次更新直接执行：

```bash
#!/bin/bash
cd /opt/kwchat
git pull origin master

# 前端
cd /opt/kwchat/kwchat-frontend && npm run build

# 管理端
cd /opt/kwchat/kwchat-admin && npm run build

# 后端
cd /opt/kwchat
mvn clean package -DskipTests -pl kwchat-app -am
kill $(pgrep -f kwchat-app)
nohup java -Xms256m -Xmx512m -XX:+UseG1GC \
    -jar /opt/kwchat/kwchat-app/target/kwchat-app-1.0.0-SNAPSHOT.jar \
    > /opt/kwchat/logs/kwchat.log 2>&1 &

echo "更新完成！"
```

使用方法：

```bash
chmod +x update.sh
./update.sh
```

---

## 常见问题

**Q: git pull 报错 `error: Your local changes would be overwritten`？**
A: 本地有修改，先暂存再拉取：
```bash
git stash
git pull origin master
git stash pop  # 如果需要恢复本地修改
```

**Q: git pull 报错 `fatal: refusing to merge unrelated histories`？**
A: 执行：
```bash
git pull origin master --allow-unrelated-histories
```

**Q: 前端构建后页面没变化？**
A: 浏览器缓存问题，强制刷新：`Ctrl + Shift + R`（Windows）或 `Cmd + Shift + R`（Mac）。

**Q: 后端启动后立即退出？**
A: 查看日志排查原因：
```bash
tail -100 /opt/kwchat/logs/kwchat.log
```

**Q: 怎么查看后端是否在运行？**
```bash
pgrep -f kwchat-app
# 有输出表示在运行，没输出表示已停止
```
