-- 确保 config 表有正确的 MQTT 主题配置
USE iotthermo;

INSERT IGNORE INTO config (k, v, upt_time, remark) VALUES 
('devTopicPost', '/iot/cloud/devices/post', NOW(), '设备上报主题'),
('devTopicPostReply', '/iot/cloud/devices/post_reply', NOW(), '设备上报回复主题'),
('devTopicReceive', '/iot/cloud/devices/receive', NOW(), '设备接收主题'),
('devTopicReceiveReply', '/iot/cloud/devices/receive_reply', NOW(), '设备接收回复主题');

-- 查看配置
SELECT * FROM config;
