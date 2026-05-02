-- Hive 建表语句
-- ODS 层：行为日志原始数据（外部表）
-- DWS 层：用户统计汇总表

-- 创建数据库
CREATE DATABASE IF NOT EXISTS clx_analytics;
USE clx_analytics;

-- ODS 层：行为日志外部表（分区表）
CREATE EXTERNAL TABLE IF NOT EXISTS ods_behavior_log (
    user_id BIGINT COMMENT '用户ID',
    behavior_type STRING COMMENT '行为类型',
    target_id BIGINT COMMENT '目标ID',
    target_type STRING COMMENT '目标类型',
    extra STRING COMMENT '扩展信息(JSON)',
    ip STRING COMMENT 'IP地址',
    create_time STRING COMMENT '创建时间'
)
COMMENT '行为日志ODS表'
PARTITIONED BY (dt STRING COMMENT '日期分区')
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\t'
STORED AS TEXTFILE
LOCATION '/user/clx/behavior';

-- DWS 层：用户日统计表
CREATE TABLE IF NOT EXISTS dws_user_daily_stats (
    stat_date STRING COMMENT '统计日期',
    dau BIGINT COMMENT '日活用户数',
    wau BIGINT COMMENT '周活用户数',
    mau BIGINT COMMENT '月活用户数',
    new_users BIGINT COMMENT '新增用户数',
    new_posts BIGINT COMMENT '新增帖子数',
    new_comments BIGINT COMMENT '新增评论数',
    retention_1d DOUBLE COMMENT '次日留存率',
    retention_7d DOUBLE COMMENT '7日留存率',
    retention_30d DOUBLE COMMENT '30日留存率'
)
COMMENT '用户日统计DWS表'
STORED AS ORC;

-- DWS 层：热门帖子表
CREATE TABLE IF NOT EXISTS dws_hot_posts (
    stat_date STRING COMMENT '统计日期',
    post_id BIGINT COMMENT '帖子ID',
    title STRING COMMENT '帖子标题',
    author_id BIGINT COMMENT '作者ID',
    author_name STRING COMMENT '作者名',
    view_count BIGINT COMMENT '浏览数',
    like_count BIGINT COMMENT '点赞数',
    comment_count BIGINT COMMENT '评论数',
    rank_type STRING COMMENT '排名类型: view/like/comment'
)
COMMENT '热门帖子DWS表'
STORED AS ORC;
