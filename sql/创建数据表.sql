-- =========================================================
-- Farm2Future Backend Database Init SQL
-- Compatible with API_CONTRACT.md v1.1
-- MySQL 8.x
-- =========================================================

CREATE DATABASE IF NOT EXISTS farm2future
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE farm2future;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS dashboard_chart_point;
DROP TABLE IF EXISTS dashboard_alert;
DROP TABLE IF EXISTS esg_report_export;
DROP TABLE IF EXISTS esg_risk_flag;
DROP TABLE IF EXISTS esg_report_score;
DROP TABLE IF EXISTS esg_report;
DROP TABLE IF EXISTS esg_score;
DROP TABLE IF EXISTS transaction_record;
DROP TABLE IF EXISTS token_transfer_record;
DROP TABLE IF EXISTS token_record;
DROP TABLE IF EXISTS iot_snapshot;
DROP TABLE IF EXISTS farm_batch;
DROP TABLE IF EXISTS farm;
DROP TABLE IF EXISTS app_user;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 1. 用户表：支持 POST /api/auth/login
-- =========================================================

CREATE TABLE app_user (
                          id VARCHAR(50) PRIMARY KEY COMMENT '用户ID，例如 u1',
                          name VARCHAR(100) NOT NULL COMMENT '用户姓名',
                          email VARCHAR(100) NOT NULL UNIQUE COMMENT '登录邮箱',
                          password VARCHAR(255) NOT NULL COMMENT '密码，开发阶段可明文，正式环境应加密',
                          role VARCHAR(30) NOT NULL COMMENT '角色：farmer/regulator/admin',
                          entity_name VARCHAR(100) COMMENT '所属实体名称，例如 Green Valley Farm',

                          deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
                          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='用户表';

-- =========================================================
-- 2. 农场表：支持 farmId、Dashboard farms 列表
-- =========================================================

CREATE TABLE farm (
                      id VARCHAR(50) PRIMARY KEY COMMENT '农场ID，例如 farm_001',
                      farm_name VARCHAR(100) NOT NULL COMMENT '农场名称',
                      owner_user_id VARCHAR(50) COMMENT '所属用户ID',
                      location VARCHAR(255) COMMENT '农场位置',
                      owner_name VARCHAR(100) COMMENT '负责人姓名',

                      deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
                      create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                      CONSTRAINT fk_farm_owner_user
                          FOREIGN KEY (owner_user_id) REFERENCES app_user(id)
                              ON DELETE SET NULL
                              ON UPDATE CASCADE
) COMMENT='农场表';

-- =========================================================
-- 3. 农场批次数据表：支持 POST /api/farms/{farmId}/data
-- =========================================================

CREATE TABLE farm_batch (
                            id VARCHAR(50) PRIMARY KEY COMMENT '批次ID，例如 BCH-2024-8921',
                            farm_id VARCHAR(50) NOT NULL COMMENT '农场ID',

                            crop_type VARCHAR(100) NOT NULL COMMENT '作物类型',
                            batch_date DATE NOT NULL COMMENT '批次日期',
                            yield_kg DECIMAL(12, 2) NOT NULL COMMENT '产量kg',
                            water_usage_l DECIMAL(12, 2) NOT NULL COMMENT '用水量L',
                            fertiliser_type VARCHAR(100) NOT NULL COMMENT '肥料类型',
                            fertiliser_usage_kg DECIMAL(12, 2) NOT NULL COMMENT '肥料用量kg',

                            sale_quantity_kg DECIMAL(12, 2) NOT NULL COMMENT '销售数量kg',
                            sale_unit_price_rm DECIMAL(12, 2) NOT NULL COMMENT '销售单价RM/kg',
                            buyer_name VARCHAR(100) NOT NULL COMMENT '买家名称',
                            seed_cost_rm DECIMAL(12, 2) NOT NULL COMMENT '种子成本RM',
                            fertiliser_cost_rm DECIMAL(12, 2) NOT NULL COMMENT '肥料成本RM',

                            tx_hash VARCHAR(100) COMMENT '区块链交易Hash',
                            submitted_at DATETIME COMMENT '提交时间',

                            deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
                            create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                            CONSTRAINT fk_farm_batch_farm
                                FOREIGN KEY (farm_id) REFERENCES farm(id)
                                    ON DELETE CASCADE
                                    ON UPDATE CASCADE
) COMMENT='农场批次数据表';

-- =========================================================
-- 4. IoT 快照表：支持 iot_snapshot
-- =========================================================

CREATE TABLE iot_snapshot (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'IoT快照ID',
                              batch_id VARCHAR(50) NOT NULL COMMENT '批次ID',
                              farm_id VARCHAR(50) NOT NULL COMMENT '农场ID',

                              soil_moisture_pct DECIMAL(5, 2) NOT NULL COMMENT '土壤湿度百分比',
                              temperature_c DECIMAL(5, 2) NOT NULL COMMENT '温度摄氏度',
                              humidity_pct DECIMAL(5, 2) NOT NULL COMMENT '空气湿度百分比',
                              ph_level DECIMAL(4, 2) NOT NULL COMMENT 'PH值',

                              deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
                              create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                              CONSTRAINT fk_iot_snapshot_batch
                                  FOREIGN KEY (batch_id) REFERENCES farm_batch(id)
                                      ON DELETE CASCADE
                                      ON UPDATE CASCADE,

                              CONSTRAINT fk_iot_snapshot_farm
                                  FOREIGN KEY (farm_id) REFERENCES farm(id)
                                      ON DELETE CASCADE
                                      ON UPDATE CASCADE
) COMMENT='IoT传感器快照表';

-- =========================================================
-- 5. Token 表：支持 POST /api/tokens、GET /api/tokens
-- =========================================================

CREATE TABLE token_record (
                              id VARCHAR(50) PRIMARY KEY COMMENT 'Token ID，例如 TKN-2024-001',
                              batch_id VARCHAR(50) COMMENT '关联批次ID',
                              farm_id VARCHAR(50) COMMENT '农场ID',

                              crop_type VARCHAR(100) NOT NULL COMMENT '作物类型',
                              asset VARCHAR(100) NOT NULL COMMENT '资产名称，例如 Wheat Batch A',
                              quantity_kg DECIMAL(12, 2) NOT NULL COMMENT 'Token代表的农产品数量kg',

    -- 兼容你之前 Dashboard 代码里的 SUM(token_amount)
                              token_amount DECIMAL(18, 2) NOT NULL DEFAULT 0 COMMENT 'Token数量，兼容旧统计字段',

                              owner VARCHAR(100) NOT NULL COMMENT '当前Owner名称',
                              owner_address VARCHAR(100) COMMENT '当前Owner区块链地址',

                              status VARCHAR(30) NOT NULL DEFAULT 'normal' COMMENT 'normal/flagged/at-risk',
                              tx_hash VARCHAR(100) COMMENT '发行交易Hash',
                              issue_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发行时间',

                              deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
                              create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                              CONSTRAINT fk_token_batch
                                  FOREIGN KEY (batch_id) REFERENCES farm_batch(id)
                                      ON DELETE SET NULL
                                      ON UPDATE CASCADE,

                              CONSTRAINT fk_token_farm
                                  FOREIGN KEY (farm_id) REFERENCES farm(id)
                                      ON DELETE SET NULL
                                      ON UPDATE CASCADE
) COMMENT='Token记录表';

-- =========================================================
-- 6. Token 转移记录表：支持 POST /api/tokens/{tokenId}/transfer
-- =========================================================

CREATE TABLE token_transfer_record (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '转移记录ID',
                                       token_id VARCHAR(50) NOT NULL COMMENT 'Token ID',

                                       old_owner VARCHAR(100) COMMENT '旧Owner名称',
                                       old_owner_address VARCHAR(100) COMMENT '旧Owner地址',
                                       new_owner VARCHAR(100) COMMENT '新Owner名称',
                                       new_owner_address VARCHAR(100) NOT NULL COMMENT '新Owner地址',

                                       tx_hash VARCHAR(100) COMMENT '区块链交易Hash',
                                       transferred_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '转移时间',

                                       deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
                                       create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                                       CONSTRAINT fk_token_transfer_token
                                           FOREIGN KEY (token_id) REFERENCES token_record(id)
                                               ON DELETE CASCADE
                                               ON UPDATE CASCADE
) COMMENT='Token转移记录表';

-- =========================================================
-- 7. 交易记录表：支持 GET /api/transactions
-- 注意：from 和 to 是关键字，所以用 from_party / to_party
-- =========================================================

CREATE TABLE transaction_record (
                                    id VARCHAR(50) PRIMARY KEY COMMENT '交易ID，例如 TXN-2024-101',
                                    token_id VARCHAR(50) COMMENT 'Token ID',

                                    from_party VARCHAR(100) NOT NULL COMMENT '转出方',
                                    to_party VARCHAR(100) NOT NULL COMMENT '接收方',

                                    tx_type VARCHAR(30) COMMENT '交易类型：issue/transfer/farm_data_anchor/report_export',
                                    tx_hash VARCHAR(100) COMMENT '区块链交易Hash',
                                    tx_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
                                    status VARCHAR(30) NOT NULL DEFAULT 'completed' COMMENT 'completed/pending/failed',

                                    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
                                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                                    CONSTRAINT fk_transaction_token
                                        FOREIGN KEY (token_id) REFERENCES token_record(id)
                                            ON DELETE SET NULL
                                            ON UPDATE CASCADE
) COMMENT='区块链交易记录表';

-- =========================================================
-- 8. ESG 原始评分表：支持 Dashboard 统计
-- =========================================================

CREATE TABLE esg_score (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ESG评分ID',
                           farm_id VARCHAR(50) COMMENT '农场ID',

                           environmental_score DECIMAL(5, 2) NOT NULL DEFAULT 0 COMMENT '环境评分',
                           social_score DECIMAL(5, 2) NOT NULL DEFAULT 0 COMMENT '社会评分',
                           governance_score DECIMAL(5, 2) NOT NULL DEFAULT 0 COMMENT '治理评分',
                           total_score DECIMAL(5, 2) NOT NULL DEFAULT 0 COMMENT 'ESG总评分',

                           period VARCHAR(20) COMMENT '评分周期，例如 2026-Q2',

                           deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
                           create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                           CONSTRAINT fk_esg_score_farm
                               FOREIGN KEY (farm_id) REFERENCES farm(id)
                                   ON DELETE SET NULL
                                   ON UPDATE CASCADE
) COMMENT='ESG评分表';

-- =========================================================
-- 9. ESG 报告表：支持 POST /api/reports/esg/generate
-- =========================================================

CREATE TABLE esg_report (
                            id VARCHAR(50) PRIMARY KEY COMMENT '报告ID，例如 RPT-2024-001',

                            period_from DATE NOT NULL COMMENT '报告开始日期',
                            period_to DATE NOT NULL COMMENT '报告结束日期',
                            entity VARCHAR(100) NOT NULL COMMENT '实体名称，例如 All Entities 或 Green Valley Farm',

                            overall_score DECIMAL(5, 2) NOT NULL DEFAULT 0 COMMENT '综合ESG分数',
                            environmental_score DECIMAL(5, 2) NOT NULL DEFAULT 0 COMMENT '环境评分',
                            social_score DECIMAL(5, 2) NOT NULL DEFAULT 0 COMMENT '社会评分',
                            governance_score DECIMAL(5, 2) NOT NULL DEFAULT 0 COMMENT '治理评分',

                            generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',

                            deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
                            create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='ESG报告表';

-- =========================================================
-- 10. ESG 报告评分明细表：支持 scores 数组
-- =========================================================

CREATE TABLE esg_report_score (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评分明细ID',
                                  report_id VARCHAR(50) NOT NULL COMMENT '报告ID',

                                  label VARCHAR(100) NOT NULL COMMENT 'Environmental (E)/Social (S)/Governance (G)',
                                  score DECIMAL(5, 2) NOT NULL COMMENT '分数',
                                  note VARCHAR(500) COMMENT '说明',

                                  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
                                  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                                  CONSTRAINT fk_report_score_report
                                      FOREIGN KEY (report_id) REFERENCES esg_report(id)
                                          ON DELETE CASCADE
                                          ON UPDATE CASCADE
) COMMENT='ESG报告评分明细表';

-- =========================================================
-- 11. ESG 风险提示表：支持 risk_flags 数组
-- =========================================================

CREATE TABLE esg_risk_flag (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '风险ID',
                               report_id VARCHAR(50) NOT NULL COMMENT '报告ID',

                               type VARCHAR(30) NOT NULL COMMENT 'success/warning/danger',
                               title VARCHAR(200) NOT NULL COMMENT '风险标题',
                               description VARCHAR(1000) COMMENT '风险描述，对应接口里的 desc',

                               deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
                               create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                               CONSTRAINT fk_risk_flag_report
                                   FOREIGN KEY (report_id) REFERENCES esg_report(id)
                                       ON DELETE CASCADE
                                       ON UPDATE CASCADE
) COMMENT='ESG风险提示表';

-- =========================================================
-- 12. ESG 报告导出记录表：支持 GET /api/reports/esg/export
-- =========================================================

CREATE TABLE esg_report_export (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '导出记录ID',
                                   report_id VARCHAR(50) COMMENT '报告ID',

                                   format VARCHAR(20) NOT NULL COMMENT 'csv/pdf',
                                   download_url VARCHAR(1000) NOT NULL COMMENT '下载地址',
                                   expires_at DATETIME NOT NULL COMMENT '过期时间',

                                   deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
                                   create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                                   CONSTRAINT fk_report_export_report
                                       FOREIGN KEY (report_id) REFERENCES esg_report(id)
                                           ON DELETE SET NULL
                                           ON UPDATE CASCADE
) COMMENT='ESG报告导出记录表';

-- =========================================================
-- 13. Dashboard Alert 表：支持 dashboard alerts
-- =========================================================

CREATE TABLE dashboard_alert (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '提醒ID',

                                 title VARCHAR(200) NOT NULL COMMENT '提醒标题',
                                 entity VARCHAR(100) NOT NULL COMMENT '实体名称',
                                 severity VARCHAR(30) NOT NULL COMMENT 'normal/flagged/at-risk/warning/danger',
                                 alert_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提醒时间',

                                 deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
                                 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='Dashboard提醒表';

-- =========================================================
-- 14. Dashboard 图表数据表：支持 chart.labels / chart.values
-- =========================================================

CREATE TABLE dashboard_chart_point (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '图表点ID',

                                       entity VARCHAR(100) NOT NULL DEFAULT 'All Farms' COMMENT '实体名称',
                                       chart_month VARCHAR(20) NOT NULL COMMENT '月份，例如 Jan',
                                       chart_value DECIMAL(5, 2) NOT NULL COMMENT '图表数值',
                                       chart_year INT NOT NULL COMMENT '年份',

                                       deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
                                       create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='Dashboard图表数据表';

-- =========================================================
-- Indexes
-- =========================================================

CREATE INDEX idx_app_user_email_role ON app_user(email, role);
CREATE INDEX idx_farm_owner_user_id ON farm(owner_user_id);
CREATE INDEX idx_farm_batch_farm_id ON farm_batch(farm_id);
CREATE INDEX idx_farm_batch_date ON farm_batch(batch_date);
CREATE INDEX idx_token_record_status ON token_record(status);
CREATE INDEX idx_token_record_owner ON token_record(owner);
CREATE INDEX idx_token_record_issue_date ON token_record(issue_date);
CREATE INDEX idx_transaction_record_date ON transaction_record(tx_date);
CREATE INDEX idx_transaction_record_token_id ON transaction_record(token_id);
CREATE INDEX idx_esg_score_farm_period ON esg_score(farm_id, period);
CREATE INDEX idx_esg_report_entity_period ON esg_report(entity, period_from, period_to);
CREATE INDEX idx_dashboard_alert_entity ON dashboard_alert(entity);
CREATE INDEX idx_dashboard_chart_entity_year ON dashboard_chart_point(entity, chart_year);

-- =========================================================
-- Test Data
-- =========================================================

-- 1. Users
INSERT INTO app_user (id, name, email, password, role, entity_name)
VALUES
    ('u1', 'Joseph', 'demo@farmer.com', '123456', 'farmer', 'Green Valley Farm'),
    ('u2', 'Regulator Admin', 'demo@regulator.com', '123456', 'regulator', 'All Entities'),
    ('u3', 'System Admin', 'demo@admin.com', '123456', 'admin', 'Farm2Future');

-- 2. Farms
INSERT INTO farm (id, farm_name, owner_user_id, location, owner_name)
VALUES
    ('farm_001', 'Green Valley Farm', 'u1', 'Malaysia', 'Joseph'),
    ('farm_002', 'Sunrise Organics', NULL, 'Malaysia', 'Alice'),
    ('farm_003', 'Highland Pastures', NULL, 'Malaysia', 'Bob');

-- 3. Farm Batch
INSERT INTO farm_batch (
    id,
    farm_id,
    crop_type,
    batch_date,
    yield_kg,
    water_usage_l,
    fertiliser_type,
    fertiliser_usage_kg,
    sale_quantity_kg,
    sale_unit_price_rm,
    buyer_name,
    seed_cost_rm,
    fertiliser_cost_rm,
    tx_hash,
    submitted_at
)
VALUES
    (
        'BCH-2024-8921',
        'farm_001',
        'Wheat',
        '2026-06-12',
        2500.00,
        15000.00,
        'Organic Compost',
        120.00,
        2000.00,
        3.50,
        'EcoFoods Corp',
        450.00,
        280.00,
        '0x9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b',
        '2026-06-12 08:30:00'
    ),
    (
        'BCH-2024-8922',
        'farm_002',
        'Corn',
        '2026-06-13',
        3000.00,
        18000.00,
        'Bio Fertiliser',
        150.00,
        2600.00,
        2.80,
        'GreenMart',
        500.00,
        320.00,
        '0x1111111111111111111111111111111111111111111111111111111111111111',
        '2026-06-13 09:00:00'
    ),
    (
        'BCH-2024-8923',
        'farm_003',
        'Rice',
        '2026-06-14',
        4000.00,
        25000.00,
        'Organic Compost',
        200.00,
        3500.00,
        2.20,
        'Asia Foods',
        600.00,
        400.00,
        '0x2222222222222222222222222222222222222222222222222222222222222222',
        '2026-06-14 10:00:00'
    );

-- 4. IoT Snapshots
INSERT INTO iot_snapshot (
    batch_id,
    farm_id,
    soil_moisture_pct,
    temperature_c,
    humidity_pct,
    ph_level
)
VALUES
    ('BCH-2024-8921', 'farm_001', 42.00, 24.00, 68.00, 6.50),
    ('BCH-2024-8922', 'farm_002', 39.00, 26.00, 65.00, 6.80),
    ('BCH-2024-8923', 'farm_003', 55.00, 25.00, 72.00, 6.40);

-- 5. Token Records
INSERT INTO token_record (
    id,
    batch_id,
    farm_id,
    crop_type,
    asset,
    quantity_kg,
    token_amount,
    owner,
    owner_address,
    status,
    tx_hash,
    issue_date
)
VALUES
    (
        'TKN-2024-001',
        'BCH-2024-8921',
        'farm_001',
        'Wheat',
        'Wheat Batch A',
        5000.00,
        5000.00,
        'Green Valley Farm',
        '0xGreenValleyOwner',
        'normal',
        '0xabc123',
        '2024-10-12 09:30:00'
    ),
    (
        'TKN-2024-002',
        'BCH-2024-8922',
        'farm_002',
        'Corn',
        'Corn Batch B',
        3000.00,
        3000.00,
        'Sunrise Organics',
        '0xSunriseOwner',
        'flagged',
        '0xdef456',
        '2024-10-13 10:30:00'
    ),
    (
        'TKN-2024-003',
        'BCH-2024-8923',
        'farm_003',
        'Rice',
        'Rice Batch C',
        4000.00,
        4000.00,
        'Highland Pastures',
        '0xHighlandOwner',
        'at-risk',
        '0xghi789',
        '2024-10-14 11:30:00'
    );

-- 6. Token Transfer Records
INSERT INTO token_transfer_record (
    token_id,
    old_owner,
    old_owner_address,
    new_owner,
    new_owner_address,
    tx_hash,
    transferred_at
)
VALUES
    (
        'TKN-2024-001',
        'Green Valley Farm',
        '0xGreenValleyOwner',
        'EcoFoods Corp',
        '0xNewOwner123',
        '0xtransfer001',
        '2026-06-12 08:35:00'
    );

-- 7. Transactions
INSERT INTO transaction_record (
    id,
    token_id,
    from_party,
    to_party,
    tx_type,
    tx_hash,
    tx_date,
    status
)
VALUES
    (
        'TXN-2024-101',
        'TKN-2024-001',
        'System',
        'Green Valley Farm',
        'issue',
        '0xabc123',
        '2024-10-12 09:30:00',
        'completed'
    ),
    (
        'TXN-2024-102',
        'TKN-2024-002',
        'System',
        'Sunrise Organics',
        'issue',
        '0xdef456',
        '2024-10-13 10:30:00',
        'completed'
    ),
    (
        'TXN-2024-103',
        'TKN-2024-003',
        'System',
        'Highland Pastures',
        'issue',
        '0xghi789',
        '2024-10-14 11:30:00',
        'completed'
    ),
    (
        'TXN-2024-104',
        'TKN-2024-001',
        'Green Valley Farm',
        'EcoFoods Corp',
        'transfer',
        '0xtransfer001',
        '2026-06-12 08:35:00',
        'completed'
    );

-- 8. ESG Scores
INSERT INTO esg_score (
    farm_id,
    environmental_score,
    social_score,
    governance_score,
    total_score,
    period
)
VALUES
    ('farm_001', 82.00, 91.00, 88.00, 87.00, '2026-Q2'),
    ('farm_002', 78.00, 85.00, 83.00, 82.00, '2026-Q2'),
    ('farm_003', 75.00, 80.00, 79.00, 78.00, '2026-Q2');

-- 9. ESG Report
INSERT INTO esg_report (
    id,
    period_from,
    period_to,
    entity,
    overall_score,
    environmental_score,
    social_score,
    governance_score,
    generated_at
)
VALUES
    (
        'RPT-2024-001',
        '2024-01-01',
        '2024-12-31',
        'All Entities',
        87.00,
        82.00,
        91.00,
        88.00,
        '2024-12-31 23:59:59'
    );

-- 10. ESG Report Scores
INSERT INTO esg_report_score (report_id, label, score, note)
VALUES
    ('RPT-2024-001', 'Environmental (E)', 82.00, 'Carbon footprint reduced by 12% YoY.'),
    ('RPT-2024-001', 'Social (S)', 91.00, 'Fair labor practices verified across 100% of supply chain.'),
    ('RPT-2024-001', 'Governance (G)', 88.00, 'All compliance audits passed successfully.');

-- 11. ESG Risk Flags
INSERT INTO esg_risk_flag (report_id, type, title, description)
VALUES
    ('RPT-2024-001', 'success', 'Water Usage Optimization', 'Water usage is within the expected range.'),
    ('RPT-2024-001', 'warning', 'Fertilizer Data Gap', 'Some fertilizer data may need manual review.'),
    ('RPT-2024-001', 'danger', 'Carbon Offset Expiry', 'Some carbon offset records are close to expiry.');

-- 12. ESG Export Record
INSERT INTO esg_report_export (
    report_id,
    format,
    download_url,
    expires_at
)
VALUES
    (
        'RPT-2024-001',
        'pdf',
        'https://s3.example.com/report-f2f-2024.pdf?X-Amz-Signature=demo',
        '2026-06-12 09:30:00'
    );

-- 13. Dashboard Alerts
INSERT INTO dashboard_alert (title, entity, severity, alert_time)
VALUES
    ('Unusual water usage detected', 'Green Valley Farm', 'at-risk', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
    ('Fertilizer data requires review', 'Sunrise Organics', 'flagged', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
    ('Carbon offset record near expiry', 'Highland Pastures', 'at-risk', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 14. Dashboard Chart Points
INSERT INTO dashboard_chart_point (entity, chart_month, chart_value, chart_year)
VALUES
    ('All Farms', 'Jan', 72.00, 2026),
    ('All Farms', 'Feb', 75.00, 2026),
    ('All Farms', 'Mar', 74.00, 2026),
    ('All Farms', 'Apr', 78.00, 2026),
    ('All Farms', 'May', 80.00, 2026),
    ('All Farms', 'Jun', 82.00, 2026),
    ('All Farms', 'Jul', 81.00, 2026),
    ('All Farms', 'Aug', 84.00, 2026),
    ('All Farms', 'Sep', 85.00, 2026),
    ('All Farms', 'Oct', 86.00, 2026),
    ('All Farms', 'Nov', 87.00, 2026),
    ('All Farms', 'Dec', 87.00, 2026),

    ('Green Valley Farm', 'Jan', 74.00, 2026),
    ('Green Valley Farm', 'Feb', 76.00, 2026),
    ('Green Valley Farm', 'Mar', 78.00, 2026),
    ('Green Valley Farm', 'Apr', 80.00, 2026),
    ('Green Valley Farm', 'May', 82.00, 2026),
    ('Green Valley Farm', 'Jun', 84.00, 2026),
    ('Green Valley Farm', 'Jul', 85.00, 2026),
    ('Green Valley Farm', 'Aug', 86.00, 2026),
    ('Green Valley Farm', 'Sep', 86.00, 2026),
    ('Green Valley Farm', 'Oct', 87.00, 2026),
    ('Green Valley Farm', 'Nov', 87.00, 2026),
    ('Green Valley Farm', 'Dec', 87.00, 2026);