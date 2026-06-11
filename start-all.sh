#!/bin/bash
set -e

echo "[1/3] 启动 Docker Desktop..."
open -a Docker
sleep 15  # 等待 Docker daemon 就绪

echo "[2/3] 启动 MySQL..."
docker start mysql-comment
sleep 3

echo "[3/3] 启动 Redis..."
redis-server --daemonize yes --port 6379

echo ""
echo "✅ 依赖服务已就绪"
echo "   MySQL:  localhost:3306"
echo "   Redis:  localhost:6379"
echo ""
echo "现在可以在 IDEA 中启动 CommentCenterApplication 了"
