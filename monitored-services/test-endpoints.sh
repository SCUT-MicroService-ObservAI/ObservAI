#!/bin/bash

# 测试三个 demo 服务的接口
# 使用方法: ./test-endpoints.sh

echo "=========================================="
echo "测试 Demo Services 接口"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试函数
test_endpoint() {
    local service=$1
    local port=$2
    local endpoint=$3
    local description=$4
    
    echo -e "${YELLOW}测试 ${service} - ${description}${NC}"
    echo "URL: http://localhost:${port}${endpoint}"
    
    response=$(curl -s -w "\n%{http_code}" http://localhost:${port}${endpoint} 2>/dev/null)
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ]; then
        echo -e "${GREEN}✓ 成功 (HTTP $http_code)${NC}"
        echo "响应: $body" | head -c 200
        echo ""
    else
        echo -e "${RED}✗ 失败 (HTTP $http_code)${NC}"
    fi
    echo ""
}

# 检查服务是否运行
check_service() {
    local port=$1
    local service=$2
    
    if curl -s http://localhost:${port}/health > /dev/null 2>&1; then
        echo -e "${GREEN}✓ ${service} 运行中 (端口 ${port})${NC}"
        return 0
    else
        echo -e "${RED}✗ ${service} 未运行 (端口 ${port})${NC}"
        return 1
    fi
}

echo "1. 检查服务状态"
echo "----------------------------------------"
check_service 8091 "demo-user-service"
check_service 8092 "demo-order-service"
check_service 8093 "demo-payment-service"
echo ""

echo "2. 测试健康检查接口"
echo "----------------------------------------"
test_endpoint "demo-user-service" 8091 "/health" "健康检查"
test_endpoint "demo-order-service" 8092 "/health" "健康检查"
test_endpoint "demo-payment-service" 8093 "/health" "健康检查"

echo "3. 测试运行指标接口"
echo "----------------------------------------"
test_endpoint "demo-user-service" 8091 "/metrics" "运行指标"
test_endpoint "demo-order-service" 8092 "/metrics" "运行指标"
test_endpoint "demo-payment-service" 8093 "/metrics" "运行指标"

echo "4. 测试异常日志接口"
echo "----------------------------------------"
test_endpoint "demo-user-service" 8091 "/test/error" "异常日志"
test_endpoint "demo-order-service" 8092 "/test/error" "异常日志"
test_endpoint "demo-payment-service" 8093 "/test/error" "异常日志"

echo "5. 测试慢接口（需要等待 800-1200ms）"
echo "----------------------------------------"
echo -e "${YELLOW}注意: 此测试会花费较长时间...${NC}"
test_endpoint "demo-user-service" 8091 "/test/slow" "慢接口"
test_endpoint "demo-order-service" 8092 "/test/slow" "慢接口"
test_endpoint "demo-payment-service" 8093 "/test/slow" "慢接口"

echo "=========================================="
echo "测试完成！"
echo "=========================================="
