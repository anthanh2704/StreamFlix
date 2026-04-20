-- StreamFlix - Video Streaming Database Schema
-- Principles of Database Management (IT079IU)
-- Database: MySQL 8.0+
-- Normalization: 3NF / BCNF where applicable

DROP DATABASE IF EXISTS streamflix_db;
CREATE DATABASE streamflix_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE streamflix_db;

-- 1. USER PROFILES AND AUTHENTICATION

-- Subscription plans (Basic, Premium, Family, etc.)
CREATE TABLE subscription_plan (
    plan_id       INT AUTO_INCREMENT PRIMARY KEY,
    plan_name     VARCHAR(50)   NOT NULL UNIQUE,
    price         DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    max_quality   VARCHAR(10)   NOT NULL DEFAULT 'HD',    -- SD, HD, FHD, 4K
    max_devices   INT           NOT NULL DEFAULT 1 CHECK (max_devices >= 1),
    description   TEXT
) ENGINE=InnoDB;

-- Users of the platform
CREATE TABLE app_user (
    user_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    email           VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100),
    date_of_birth   DATE,
    country         VARCHAR(50),
    avatar_url      VARCHAR(500),
    role            ENUM('USER','CREATOR','ADMIN') DEFAULT 'USER',
    is_active       BOOLEAN      DEFAULT TRUE,
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    last_login      TIMESTAMP    NULL,
    CONSTRAINT chk_email CHECK (email LIKE '%_@_%._%')
) ENGINE=InnoDB;

-- User's current subscription to a plan (1 user : N subscription records over time)
CREATE TABLE user_subscription (
    subscription_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    plan_id         INT    NOT NULL,
    start_date      DATE   NOT NULL,
    end_date        DATE   NOT NULL,
    status          ENUM('ACTIVE','EXPIRED','CANCELLED') DEFAULT 'ACTIVE',
    auto_renew      BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES subscription_plan(plan_id) ON DELETE RESTRICT,
    CONSTRAINT chk_dates CHECK (end_date > start_date)
) ENGINE=InnoDB;

-- Payments recorded per subscription
CREATE TABLE payment (
    payment_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    payment_date    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    method          ENUM('CREDIT_CARD','PAYPAL','BANK_TRANSFER','MOMO','ZALOPAY') NOT NULL,
    status          ENUM('PENDING','COMPLETED','FAILED','REFUNDED') DEFAULT 'PENDING',
    transaction_ref VARCHAR(100) UNIQUE,
    FOREIGN KEY (subscription_id) REFERENCES user_subscription(subscription_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 2. CONTENT MANAGEMENT (Channels, Categories, Videos)

-- A channel is owned by a user (creator). 1:1 in this simplified model
CREATE TABLE channel (
    channel_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_user_id     BIGINT NOT NULL UNIQUE,            -- one channel per user
    channel_name      VARCHAR(100) NOT NULL,
    description       TEXT,
    banner_url        VARCHAR(500),
    subscriber_count  BIGINT DEFAULT 0,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_user_id) REFERENCES app_user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Category (Music, Gaming, Education...)
CREATE TABLE category (
    category_id   INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(50) NOT NULL UNIQUE,
    description   VARCHAR(255)
) ENGINE=InnoDB;

-- Video: the main entity of the platform
CREATE TABLE video (
    video_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    channel_id      BIGINT       NOT NULL,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    video_url       VARCHAR(500) NOT NULL,     -- link to storage (S3/CDN)
    thumbnail_url   VARCHAR(500),
    duration_sec    INT          NOT NULL CHECK (duration_sec > 0),
    resolution      VARCHAR(10)  DEFAULT 'HD', -- SD, HD, FHD, 4K
    views_count     BIGINT       DEFAULT 0,
    likes_count     BIGINT       DEFAULT 0,
    dislikes_count  BIGINT       DEFAULT 0,
    status          ENUM('DRAFT','PUBLISHED','PRIVATE','REMOVED') DEFAULT 'PUBLISHED',
    is_premium      BOOLEAN      DEFAULT FALSE,   -- premium content requires subscription
    upload_date     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (channel_id) REFERENCES channel(channel_id) ON DELETE CASCADE,
    INDEX idx_video_upload (upload_date DESC),
    INDEX idx_video_views  (views_count DESC),
    FULLTEXT INDEX ft_video_text (title, description)
) ENGINE=InnoDB;

-- M:N - a video can be in multiple categories
CREATE TABLE video_category (
    video_id     BIGINT NOT NULL,
    category_id  INT    NOT NULL,
    PRIMARY KEY (video_id, category_id),
    FOREIGN KEY (video_id)    REFERENCES video(video_id)       ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Tags allow finer search
CREATE TABLE tag (
    tag_id   INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE video_tag (
    video_id BIGINT NOT NULL,
    tag_id   INT    NOT NULL,
    PRIMARY KEY (video_id, tag_id),
    FOREIGN KEY (video_id) REFERENCES video(video_id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id)   REFERENCES tag(tag_id)     ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. USER INTERACTION

-- A user subscribes to a channel (not the platform plan)
CREATE TABLE channel_subscription (
    subscriber_user_id BIGINT NOT NULL,
    channel_id         BIGINT NOT NULL,
    subscribed_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notifications_on   BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (subscriber_user_id, channel_id),
    FOREIGN KEY (subscriber_user_id) REFERENCES app_user(user_id)  ON DELETE CASCADE,
    FOREIGN KEY (channel_id)         REFERENCES channel(channel_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Like or dislike on a video
CREATE TABLE video_reaction (
    user_id    BIGINT NOT NULL,
    video_id   BIGINT NOT NULL,
    reaction   ENUM('LIKE','DISLIKE') NOT NULL,
    reacted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, video_id),
    FOREIGN KEY (user_id)  REFERENCES app_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (video_id) REFERENCES video(video_id)   ON DELETE CASCADE
) ENGINE=InnoDB;

-- Comments (supports nested replies via parent_comment_id)
CREATE TABLE comment (
    comment_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_id          BIGINT NOT NULL,
    user_id           BIGINT NOT NULL,
    parent_comment_id BIGINT NULL,
    content           TEXT   NOT NULL,
    likes_count       INT    DEFAULT 0,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (video_id)          REFERENCES video(video_id)   ON DELETE CASCADE,
    FOREIGN KEY (user_id)           REFERENCES app_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES comment(comment_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Watch history: every play session
CREATE TABLE watch_history (
    history_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT NOT NULL,
    video_id       BIGINT NOT NULL,
    watched_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    watch_duration INT      NOT NULL DEFAULT 0,    -- seconds actually watched
    progress_pct   DECIMAL(5,2) DEFAULT 0.00,       -- 0.00–100.00
    device_type    VARCHAR(30),                      -- WEB, iOS, ANDROID, TV
    FOREIGN KEY (user_id)  REFERENCES app_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (video_id) REFERENCES video(video_id)   ON DELETE CASCADE,
    INDEX idx_history_user_time (user_id, watched_at DESC)
) ENGINE=InnoDB;

-- Playlists
CREATE TABLE playlist (
    playlist_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    title       VARCHAR(150) NOT NULL,
    description TEXT,
    is_public   BOOLEAN  DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE playlist_video (
    playlist_id BIGINT NOT NULL,
    video_id    BIGINT NOT NULL,
    position    INT    NOT NULL,
    added_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (playlist_id, video_id),
    FOREIGN KEY (playlist_id) REFERENCES playlist(playlist_id) ON DELETE CASCADE,
    FOREIGN KEY (video_id)    REFERENCES video(video_id)       ON DELETE CASCADE,
    UNIQUE KEY uk_playlist_position (playlist_id, position)
) ENGINE=InnoDB;

-- Notifications (new video from subscribed channel, reply to comment, etc.)
CREATE TABLE notification (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    type            ENUM('NEW_VIDEO','COMMENT_REPLY','SUB_EXPIRING','SYSTEM') NOT NULL,
    content         VARCHAR(500) NOT NULL,
    link_url        VARCHAR(500),
    is_read         BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE CASCADE,
    INDEX idx_notif_user_read (user_id, is_read, created_at DESC)
) ENGINE=InnoDB;

-- 4. TRIGGERS – keep denormalized counters in sync

DELIMITER $$

-- When a view is recorded, increment the counter on the video
CREATE TRIGGER trg_after_watch_insert
AFTER INSERT ON watch_history
FOR EACH ROW
BEGIN
    UPDATE video
       SET views_count = views_count + 1
     WHERE video_id = NEW.video_id;
END$$

-- Increment/decrement like & dislike counters on video
CREATE TRIGGER trg_after_reaction_insert
AFTER INSERT ON video_reaction
FOR EACH ROW
BEGIN
    IF NEW.reaction = 'LIKE' THEN
        UPDATE video SET likes_count = likes_count + 1 WHERE video_id = NEW.video_id;
    ELSE
        UPDATE video SET dislikes_count = dislikes_count + 1 WHERE video_id = NEW.video_id;
    END IF;
END$$

CREATE TRIGGER trg_after_reaction_delete
AFTER DELETE ON video_reaction
FOR EACH ROW
BEGIN
    IF OLD.reaction = 'LIKE' THEN
        UPDATE video SET likes_count = GREATEST(likes_count - 1, 0) WHERE video_id = OLD.video_id;
    ELSE
        UPDATE video SET dislikes_count = GREATEST(dislikes_count - 1, 0) WHERE video_id = OLD.video_id;
    END IF;
END$$

-- Maintain channel subscriber_count
CREATE TRIGGER trg_after_sub_insert
AFTER INSERT ON channel_subscription
FOR EACH ROW
BEGIN
    UPDATE channel
       SET subscriber_count = subscriber_count + 1
     WHERE channel_id = NEW.channel_id;
END$$

CREATE TRIGGER trg_after_sub_delete
AFTER DELETE ON channel_subscription
FOR EACH ROW
BEGIN
    UPDATE channel
       SET subscriber_count = GREATEST(subscriber_count - 1, 0)
     WHERE channel_id = OLD.channel_id;
END$$

DELIMITER ;

-- 5. VIEWS – useful read-only projections for the recommendation engine

-- Trending videos: high views in the last 7 days
CREATE OR REPLACE VIEW vw_trending_videos AS
SELECT v.video_id,
       v.title,
       c.channel_name,
       v.views_count,
       v.likes_count,
       v.upload_date,
       DATEDIFF(NOW(), v.upload_date) AS days_since_upload
  FROM video v
  JOIN channel c ON c.channel_id = v.channel_id
 WHERE v.status = 'PUBLISHED'
   AND v.upload_date >= DATE_SUB(NOW(), INTERVAL 7 DAY);

-- Public channel overview with latest activity
CREATE OR REPLACE VIEW vw_channel_stats AS
SELECT c.channel_id,
       c.channel_name,
       u.username              AS owner,
       c.subscriber_count,
       COUNT(v.video_id)       AS total_videos,
       COALESCE(SUM(v.views_count),0) AS total_views,
       MAX(v.upload_date)      AS last_upload
  FROM channel c
  JOIN app_user u  ON u.user_id = c.owner_user_id
  LEFT JOIN video v ON v.channel_id = c.channel_id AND v.status = 'PUBLISHED'
 GROUP BY c.channel_id, c.channel_name, u.username, c.subscriber_count;
