-- 在 PigX 原始 Nacos 网关配置中追加 IDC 业务路由。
USE `pig_config`;

SET @route_anchor = CONCAT('            - id: pig-codegen', CHAR(10));
SET @idc_routes = CONCAT(
    '            - id: pig-dc-server-biz', CHAR(10),
    '              uri: lb://pig-dc-server-biz', CHAR(10),
    '              predicates:', CHAR(10),
    '                - Path=/api/servers/**,/api/server-types/**,/api/cpu-architectures/**', CHAR(10),
    '              metadata:', CHAR(10),
    '                response-timeout: 30000', CHAR(10),
    '            - id: pig-dc-accessory-biz', CHAR(10),
    '              uri: lb://pig-dc-accessory-biz', CHAR(10),
    '              predicates:', CHAR(10),
    '                - Path=/api/accessories/**,/api/server-accessories/**,/api/statistics/**,/api/spare-parts/**,/api/spare-part-categories/**,/api/accessory-center/**', CHAR(10),
    '              metadata:', CHAR(10),
    '                response-timeout: 30000', CHAR(10)
);

UPDATE `config_info`
SET `content` = REPLACE(`content`, @route_anchor, CONCAT(@idc_routes, @route_anchor)),
    `md5` = MD5(`content`),
    `gmt_modified` = NOW()
WHERE `data_id` = 'pig-gateway-dev.yml'
  AND `group_id` = 'DEFAULT_GROUP'
  AND `content` NOT LIKE '%pig-dc-accessory-biz%';
