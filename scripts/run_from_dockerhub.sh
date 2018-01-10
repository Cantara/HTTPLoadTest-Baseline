#!/bin/bash
# This file will build a Docker image, create a data volume container and run an application container
# from the image. Use it to test that your Docker configuration builds successfully.

PROJECT_NAME=HTTPLoadtest
DOCKERHUB_ORG=cantara
IMAGE_NAME=httploadtest-baseline
IMAGE_NAME_DATA=cantara-httploadtestbaseline-data

sudo docker rm -v -f $IMAGE_NAME
sudo docker pull $DOCKERHUB_ORG/$IMAGE_NAME
sudo docker create --name $IMAGE_NAME_DATA $DOCKERHUB_ORG/$IMAGE_NAME
sudo docker run -d -p 18086:8086 --name $IMAGE_NAME --volumes-from $IMAGE_NAME_DATA $DOCKERHUB_ORG/$IMAGE_NAME

wget http://localhost:18086/HTTPLoadTest-baseline/health
cat health
echo ""
echo ""
echo "Starting instance. Do 'sudo docker exec -it $DOCKERHUB_ORG/$IMAGE_NAME bash' to get shell"
echo ""
echo "http://localhost:18086/HTTPLoadTest-baseline/config"

