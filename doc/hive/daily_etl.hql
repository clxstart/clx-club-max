-- 每日 ETL 脚本
-- 输入：ods_behavior_log（前一天的分区）
-- 输出：dws_user_daily_stats、dws_hot_posts
-- 调度：每日凌晨 03:00

-- 设置动态分区
SET hive.exec.dynamic.partition=true;
SET hive.exec.dynamic.partition.mode=nonstrict;

-- 计算统计日期（前一天）
-- 实际执行时通过参数传入：hive -hiveconf STAT_DATE=2026-04-29 -f daily_etl.hql
-- 以下使用 ${hiveconf:STAT_DATE} 占位

-- 插入用户日统计表
INSERT OVERWRITE TABLE dws_user_daily_stats
SELECT
    '${hiveconf:STAT_DATE}' AS stat_date,
    -- DAU：当天有行为的不重复用户数
    COUNT(DISTINCT user_id) AS dau,
    -- WAU：最近 7 天有行为的不重复用户数
    (SELECT COUNT(DISTINCT user_id) FROM ods_behavior_log
     WHERE dt >= date_sub('${hiveconf:STAT_DATE}', 6)
       AND dt <= '${hiveconf:STAT_DATE}') AS wau,
    -- MAU：最近 30 天有行为的不重复用户数
    (SELECT COUNT(DISTINCT user_id) FROM ods_behavior_log
     WHERE dt >= date_sub('${hiveconf:STAT_DATE}', 29)
       AND dt <= '${hiveconf:STAT_DATE}') AS mau,
    -- 新增用户数：当天首次登录的用户
    (SELECT COUNT(DISTINCT user_id) FROM ods_behavior_log
     WHERE dt = '${hiveconf:STAT_DATE}'
       AND behavior_type = 'login'
       AND user_id NOT IN (
           SELECT DISTINCT user_id FROM ods_behavior_log
           WHERE dt < '${hiveconf:STAT_DATE}' AND behavior_type = 'login'
       )) AS new_users,
    -- 新增帖子数：当天发布的帖子
    (SELECT COUNT(*) FROM ods_behavior_log
     WHERE dt = '${hiveconf:STAT_DATE}'
       AND behavior_type = 'view_post'
       AND extra LIKE '%"is_new":true%') AS new_posts,
    -- 新增评论数：当天发表的评论
    (SELECT COUNT(*) FROM ods_behavior_log
     WHERE dt = '${hiveconf:STAT_DATE}'
       AND behavior_type = 'comment') AS new_comments,
    -- 次日留存率：前一天新用户在今天活跃的比例
    0.0 AS retention_1d,
    -- 7 日留存率
    0.0 AS retention_7d,
    -- 30 日留存率
    0.0 AS retention_30d
FROM ods_behavior_log
WHERE dt = '${hiveconf:STAT_DATE}';

-- 插入热门帖子表（按浏览量排名 Top10）
INSERT OVERWRITE TABLE dws_hot_posts
SELECT
    '${hiveconf:STAT_DATE}' AS stat_date,
    post_id,
    title,
    author_id,
    author_name,
    view_count,
    like_count,
    comment_count,
    'view' AS rank_type
FROM (
    SELECT
        bl.target_id AS post_id,
        json_extract(bl.extra, '$.title') AS title,
        json_extract(bl.extra, '$.authorId') AS author_id,
        json_extract(bl.extra, '$.authorName') AS author_name,
        SUM(CASE WHEN bl.behavior_type = 'view_post' THEN 1 ELSE 0 END) AS view_count,
        SUM(CASE WHEN bl.behavior_type = 'like_post' THEN 1 ELSE 0 END) AS like_count,
        SUM(CASE WHEN bl.behavior_type = 'comment' THEN 1 ELSE 0 END) AS comment_count,
        ROW_NUMBER() OVER (ORDER BY SUM(CASE WHEN bl.behavior_type = 'view_post' THEN 1 ELSE 0 END) DESC) AS rn
    FROM ods_behavior_log bl
    WHERE bl.dt = '${hiveconf:STAT_DATE}'
      AND bl.behavior_type IN ('view_post', 'like_post', 'comment')
      AND bl.target_type = 'post'
    GROUP BY bl.target_id, bl.extra
) t
WHERE rn <= 10;
