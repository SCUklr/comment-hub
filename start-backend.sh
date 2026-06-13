#!/bin/bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_JAR="$ROOT_DIR/ssp-comment-center-start/target/ssp-comment-center-start-1.0.0-SNAPSHOT.jar"
DDL_FILE="$ROOT_DIR/docs/05-database/ddl-sharding.sql"
BACKEND_LOG="/tmp/ssp-comment-center-backend.log"
BACKEND_PID="/tmp/ssp-comment-center-backend.pid"
MYSQL_CONTAINER="mysql-comment"

export PATH="/opt/homebrew/bin:$PATH"

is_port_listening() {
  local port="$1"
  lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1
}

wait_for_port() {
  local port="$1"
  local name="$2"
  local retry=30

  for _ in $(seq 1 "$retry"); do
    if is_port_listening "$port"; then
      echo "✅ $name 已监听端口 $port"
      return 0
    fi
    sleep 1
  done

  echo "❌ $name 启动超时，未监听端口 $port"
  return 1
}

wait_for_backend_health() {
  local retry=30

  for _ in $(seq 1 "$retry"); do
    if curl -fsS http://localhost:8080/actuator/health >/dev/null 2>&1; then
      echo "✅ 后端健康检查通过：http://localhost:8080/actuator/health"
      return 0
    fi
    sleep 1
  done

  echo "❌ 后端健康检查失败，请查看日志：$BACKEND_LOG"
  return 1
}

stop_by_pid_file() {
  if [ -f "$BACKEND_PID" ]; then
    local pid
    pid="$(cat "$BACKEND_PID")"
    if [ -n "$pid" ] && kill -0 "$pid" >/dev/null 2>&1; then
      echo "停止旧的后端进程：$pid"
      kill "$pid" >/dev/null 2>&1 || true
      sleep 2
    fi
    rm -f "$BACKEND_PID"
  fi
}

start_mysql() {
  echo "[1/5] 启动 MySQL Docker 容器..."

  if ! docker info >/dev/null 2>&1; then
    echo "Docker daemon 未就绪，尝试打开 Docker Desktop..."
    open -a Docker || true
    for _ in $(seq 1 60); do
      if docker info >/dev/null 2>&1; then
        break
      fi
      sleep 2
    done
  fi

  if ! docker info >/dev/null 2>&1; then
    echo "❌ Docker 未启动，请先打开 Docker Desktop"
    exit 1
  fi

  if docker ps --format '{{.Names}}' | grep -q "^${MYSQL_CONTAINER}$"; then
    echo "✅ MySQL 容器已运行"
  elif docker ps -a --format '{{.Names}}' | grep -q "^${MYSQL_CONTAINER}$"; then
    docker start "$MYSQL_CONTAINER" >/dev/null
    echo "✅ MySQL 容器已启动"
  else
    docker run -d --name "$MYSQL_CONTAINER" \
      -e MYSQL_ROOT_PASSWORD=root \
      -p 3306:3306 mysql:8.0 >/dev/null
    echo "✅ MySQL 容器已创建并启动"
  fi

  for _ in $(seq 1 30); do
    if docker exec "$MYSQL_CONTAINER" mysqladmin ping -u root -proot --silent >/dev/null 2>&1; then
      echo "✅ MySQL 已就绪"
      return 0
    fi
    sleep 2
  done

  echo "❌ MySQL 启动超时"
  exit 1
}

init_mysql_schema() {
  echo "[2/5] 初始化 MySQL 分库分表 DDL..."

  if [ ! -f "$DDL_FILE" ]; then
    echo "❌ DDL 文件不存在：$DDL_FILE"
    exit 1
  fi

  docker exec -i "$MYSQL_CONTAINER" mysql -u root -proot < "$DDL_FILE"
  echo "✅ DDL 执行完成"
}

start_redis() {
  echo "[3/5] 启动 Redis..."

  if command -v redis-cli >/dev/null 2>&1 && redis-cli ping >/dev/null 2>&1; then
    echo "✅ Redis 已运行"
    return 0
  fi

  if ! command -v redis-server >/dev/null 2>&1; then
    echo "❌ 未找到 redis-server，请先安装 Redis 或手动启动 Redis 6379"
    exit 1
  fi

  redis-server --daemonize yes --port 6379

  for _ in $(seq 1 10); do
    if redis-cli ping >/dev/null 2>&1; then
      echo "✅ Redis 已启动"
      return 0
    fi
    sleep 1
  done

  echo "❌ Redis 启动失败"
  exit 1
}

build_backend() {
  echo "[4/5] 编译打包后端..."

  cd "$ROOT_DIR"
  mvn clean package -DskipTests

  if [ ! -f "$BACKEND_JAR" ]; then
    echo "❌ 后端 jar 不存在：$BACKEND_JAR"
    exit 1
  fi

  echo "✅ 后端打包完成"
}

start_backend() {
  echo "[5/5] 启动 Spring Boot 后端..."

  stop_by_pid_file

  if is_port_listening 8080; then
    if curl -fsS http://localhost:8080/actuator/health >/dev/null 2>&1; then
      echo "✅ 后端已运行，跳过启动"
      echo "后端健康检查：http://localhost:8080/actuator/health"
      echo "注意：访问 http://localhost:8080/ 出现 Whitelabel 404 是正常的，因为项目没有根路径页面。"
      return 0
    fi
    echo "❌ 端口 8080 已被其他服务占用"
    lsof -nP -iTCP:8080 -sTCP:LISTEN
    exit 1
  fi

  cd "$ROOT_DIR"
  nohup java -jar "$BACKEND_JAR" > "$BACKEND_LOG" 2>&1 &
  echo $! > "$BACKEND_PID"

  wait_for_port 8080 "后端"
  wait_for_backend_health

  echo "后端日志：$BACKEND_LOG"
  echo "注意：访问 http://localhost:8080/ 出现 Whitelabel 404 是正常的，因为项目没有根路径页面。"
}

main() {
  echo "========================================"
  echo "启动 ssp-comment-center 后端"
  echo "项目目录：$ROOT_DIR"
  echo "========================================"

  start_mysql
  init_mysql_schema
  start_redis
  build_backend
  start_backend
}

main "$@"
