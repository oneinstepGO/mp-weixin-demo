# 启动 minikube
minikube start

# 删除之前的所有资源
#kubectl delete deployment elasticsearch-elk
#kubectl delete deployment elk-boot-demo
#kubectl delete deployment filebeat-elk
#kubectl delete deployment kibana-elk
#kubectl delete deployment logstash-elk
#
#kubectl delete service elasticsearch-elk
#kubectl delete service elk-boot-demo
#kubectl delete service filebeat-elk
#kubectl delete service kibana-elk
#kubectl delete service logstash-elk
#
#kubectl delete configmap filebeat-elk-config
#kubectl delete configmap kibana-elk-config
#kubectl delete configmap logstash-elk-config
#
## 进入 Minikube 虚拟机
#minikube ssh
#
##!/bin/bash
#
## 定义要删除的镜像关键字
#images=("elk-boot-demo" "filebeat" "kibana" "logstash" "elasticsearch")
#
## 获取所有镜像
#all_images=$(docker images --format "{{.Repository}}:{{.Tag}}")
#
## 遍历所有镜像并删除匹配的镜像
#for image in $all_images; do
#  for keyword in "${images[@]}"; do
#    if [[ $image == *$keyword* ]]; then
#      echo "Deleting image: $image"
#      docker rmi $image
#    fi
#  done
#done
#
## 确认已被删除
#docker images
#
## 退出 Minikube 虚拟机
#exit


# 将所有清单文件应用到 k8s 集群中
kubectl apply -f elasticsearch-deployment.yaml
kubectl apply -f kibana-deployment.yaml
kubectl apply -f logstash-deployment.yaml
kubectl apply -f filebeat-deployment.yaml
kubectl apply -f elk-boot-demo-deployment.yaml

# 确保所有 pod 都正常运行
kubectl get pods
kubectl get services


# 访问 kibana
#minikube service kibana-elk