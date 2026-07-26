#!/bin/bash
export SPRING_DATASOURCE_USERNAME=dcasset
export SPRING_DATASOURCE_PASSWORD=dcasset123
export NACOS_SERVER_ADDR=nacos:8848
export SERVER_DB_URL=jdbc:postgresql://postgres:5432/dc_server_db
export ACCESSORY_DB_URL=jdbc:postgresql://postgres:5432/dc_accessory_db
echo "开发环境变量已加载"
echo "  NACOS_SERVER_ADDR=$NACOS_SERVER_ADDR"
echo "  SERVER_DB_URL=$SERVER_DB_URL"
echo "  ACCESSORY_DB_URL=$ACCESSORY_DB_URL"
