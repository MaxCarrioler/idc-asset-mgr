-- 初始化配件服务独立数据库（主库 dc_server_db 由 POSTGRES_DB 环境变量创建）
CREATE DATABASE dc_accessory_db;
GRANT ALL PRIVILEGES ON DATABASE dc_accessory_db TO dcasset;
