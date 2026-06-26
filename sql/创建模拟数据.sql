INSERT INTO farm (id, farm_name, location, owner_name, status)
VALUES
(1, 'Green Valley Farm', 'Malaysia', 'Yin Sizhe', 'ACTIVE'),
(2, 'Sunshine Farm', 'Kuala Lumpur', 'Tom', 'ACTIVE');

INSERT INTO esg_score (id, farm_id, period, environmental_score, social_score, governance_score, total_score)
VALUES
(1, 1, '2026-Q2', 85, 90, 88, 87.67),
(2, 2, '2026-Q2', 80, 82, 84, 82.00);

INSERT INTO token_record (id, farm_id, token_amount, reason)
VALUES
(1, 1, 100, 'ESG reward'),
(2, 2, 80, 'ESG reward');

INSERT INTO transaction_record (id, farm_id, transaction_type, amount, description)
VALUES
(1, 1, 'TOKEN_REWARD', 100, 'Reward for ESG score'),
(2, 2, 'TOKEN_REWARD', 80, 'Reward for ESG score');