-- 设备运行/在线/报警区间记录的业务事件 ID（Flink 流生成，消费端按 prevEventId 精确闭合上一段）
-- 若列已存在可跳过对应 ALTER。
ALTER TABLE t_equip_record_alarm ADD COLUMN event_id VARCHAR(64) NULL;
ALTER TABLE t_equip_record_online ADD COLUMN event_id VARCHAR(64) NULL;
ALTER TABLE t_equip_record_run ADD COLUMN event_id VARCHAR(64) NULL;

CREATE UNIQUE INDEX uk_equip_record_alarm_event_id ON t_equip_record_alarm (event_id);
CREATE UNIQUE INDEX uk_equip_record_online_event_id ON t_equip_record_online (event_id);
CREATE UNIQUE INDEX uk_equip_record_run_event_id ON t_equip_record_run (event_id);
