ALTER TABLE user_profile
    ADD COLUMN timezone VARCHAR(64) DEFAULT 'Asia/Shanghai';

UPDATE user_profile
SET timezone = 'Asia/Shanghai'
WHERE timezone IS NULL;
