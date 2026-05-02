#!/bin/bash
# 数据导出脚本：从 MySQL behavior_log 导出到 HDFS
# 用法：./export_behavior.sh 2026-04-29

set -e

STAT_DATE=${1:-$(date -d "yesterday" +%Y-%m-%d)}
MYSQL_HOST=${MYSQL_HOST:-localhost}
MYSQL_PORT=${MYSQL_PORT:-3306}
MYSQL_USER=${MYSQL_USER:-root}
MYSQL_PASS=${MYSQL_PASS:-root}
MYSQL_DB=${MYSQL_DB:-clx_analytics}
HDFS_PATH="/user/clx/behavior/dt=${STAT_DATE}"
TEMP_FILE="/tmp/behavior_${STAT_DATE}.tsv"

echo "===== 导出行为日志: ${STAT_DATE} ====="

# 1. 从 MySQL 导出数据
echo "[1/3] 从 MySQL 导出数据..."
mysql -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASS} ${MYSQL_DB} -N -B -e "
    SELECT
        user_id,
        behavior_type,
        IFNULL(target_id, ''),
        IFNULL(target_type, ''),
        IFNULL(extra, ''),
        IFNULL(ip, ''),
        DATE_FORMAT(create_time, '%Y-%m-%d %H:%i:%s')
    FROM behavior_log
    WHERE DATE(create_time) = '${STAT_DATE}'
" | sed 's/\t/\\t/g' > ${TEMP_FILE}

ROW_COUNT=$(wc -l < ${TEMP_FILE})
echo "导出 ${ROW_COUNT} 行数据"

# 2. 上传到 HDFS
echo "[2/3] 上传到 HDFS..."
hdfs dfs -mkdir -p ${HDFS_PATH}
hdfs dfs -put -f ${TEMP_FILE} ${HDFS_PATH}/behavior.log

# 3. 清理临时文件
echo "[3/3] 清理临时文件..."
rm -f ${TEMP_FILE}

echo "===== 导出完成: ${HDFS_PATH}/behavior.log ====="
