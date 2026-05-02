#!/bin/bash
# ETL 执行脚本：触发 Hive ETL 作业
# 用法：./run_etl.sh 2026-04-29

set -e

STAT_DATE=${1:-$(date -d "yesterday" +%Y-%m-%d)}
HIVE_SCRIPTS=${HIVE_SCRIPTS:-/opt/hive/scripts}

echo "===== 执行 Hive ETL: ${STAT_DATE} ====="

# 执行 Hive ETL
echo "[1/1] 执行 daily_etl.hql..."
hive -hiveconf STAT_DATE=${STAT_DATE} -f ${HIVE_SCRIPTS}/daily_etl.hql

echo "===== ETL 完成 ====="