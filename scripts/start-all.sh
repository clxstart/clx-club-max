#!/bin/bash
# CLX 微服务启动脚本
# 按依赖顺序启动所有服务

echo "=========================================="
echo "CLX 微服务启动脚本"
echo "=========================================="

# 切换到项目根目录
cd E:/clx

# 启动顺序（按依赖关系）
services=(
    "clx-auth:9100"
    "clx-user:9201"
    "clx-post:9300"
    "clx-search:9400"
    "clx-message:9500"
    "clx-quiz:9600"
    "clx-admin:9700"
    "clx-analytics:9800"
    "clx-gateway:8080"
)

for service in "${services[@]}"; do
    name=$(echo $service | cut -d: -f1)
    port=$(echo $service | cut -d: -f2)
    echo "启动 $name (端口 $port)..."
    mvn spring-boot:run -pl $name -Dspring-boot.run.profiles=dev > /tmp/clx-$name.log 2>&1 &
    sleep 5
done

echo "=========================================="
echo "等待服务启动完成..."
echo "=========================================="
sleep 30

# 检查服务状态
echo "服务状态检查:"
for service in "${services[@]}"; do
    name=$(echo $service | cut -d: -f1)
    port=$(echo $service | cut -d: -f2)
    status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health 2>/dev/null || echo "000")
    if [ "$status" = "200" ]; then
        echo "  ✅ $name: Running"
    else
        echo "  ❌ $name: Not responding (status: $status)"
    fi
done

echo "=========================================="
echo "查看 Nacos 注册情况: http://localhost:8848/nacos"
echo "查看 Zipkin 链路: http://localhost:9411"
echo "查看 Sentinel: http://localhost:8858"
echo "=========================================="