#!/bin/bash
set -e
exec > >(tee /logs/batch-setup.log) 2>&1

echo "Starting k3s agent installation..."
echo "Node Name: __NODE_NAME__"

# k3s 설치 및 컨트롤 플레인 조인
curl -sfL https://get.k3s.io | K3S_URL=https://__CONTROL_PLANE_IP__:6443 \
  K3S_TOKEN=__K3S_TOKEN__ \
  K3S_NODE_NAME=__NODE_NAME__ \
  sh -s - agent

# 노드 준비 완료 시그널
echo "✅ k3s agent installation completed"
