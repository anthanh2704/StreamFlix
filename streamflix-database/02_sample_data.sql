-- =====================================================================
-- StreamFlix - Sample Data
-- Run AFTER 01_schema.sql
-- =====================================================================

USE streamflix_db;

-- 1. Subscription plans ------------------------------------------------
INSERT INTO subscription_plan (plan_name, price, max_quality, max_devices, description) VALUES
('Free',     0.00,  'SD',  1, 'Ad-supported, SD only'),
('Basic',    4.99,  'HD',  1, 'HD streaming on 1 device'),
('Standard', 9.99,  'FHD', 2, 'Full HD on up to 2 devices'),
('Premium', 15.99,  '4K',  4, '4K Ultra HD on up to 4 devices'),
('Family',  19.99,  '4K',  6, 'Shared 4K for up to 6 family members');

-- 2. Users -------------------------------------------------------------
INSERT INTO app_user (username, email, password_hash, full_name, date_of_birth, country, role) VALUES
('alice',   'alice@mail.com',   '$2a$10$hash1', 'Alice Nguyen',   '1998-03-12', 'Vietnam', 'CREATOR'),
('bob',     'bob@mail.com',     '$2a$10$hash2', 'Bob Tran',       '1995-07-22', 'Vietnam', 'CREATOR'),
('charlie', 'charlie@mail.com', '$2a$10$hash3', 'Charlie Pham',   '2000-11-05', 'USA',     'CREATOR'),
('david',   'david@mail.com',   '$2a$10$hash4', 'David Le',       '2001-05-30', 'Vietnam', 'USER'),
('eve',     'eve@mail.com',     '$2a$10$hash5', 'Eve Hoang',      '1999-09-14', 'Singapore','USER'),
('frank',   'frank@mail.com',   '$2a$10$hash6', 'Frank Vo',       '2002-02-18', 'Vietnam', 'USER'),
('grace',   'grace@mail.com',   '$2a$10$hash7', 'Grace Do',       '1997-12-01', 'USA',     'USER'),
('henry',   'henry@mail.com',   '$2a$10$hash8', 'Henry Bui',      '1996-04-25', 'Vietnam', 'USER'),
('ivy',     'ivy@mail.com',     '$2a$10$hash9', 'Ivy Lam',        '2003-08-08', 'Japan',   'USER'),
('admin',   'admin@mail.com',   '$2a$10$hashA', 'System Admin',   '1990-01-01', 'Vietnam', 'ADMIN');

-- 3. User subscriptions (platform plans) -------------------------------
INSERT INTO user_subscription (user_id, plan_id, start_date, end_date, status) VALUES
(1, 4, '2026-01-01', '2026-12-31', 'ACTIVE'),    -- alice, Premium
(2, 3, '2026-02-01', '2026-12-31', 'ACTIVE'),    -- bob,   Standard
(3, 4, '2025-06-15', '2026-06-15', 'ACTIVE'),    -- charlie, Premium
(4, 2, '2026-03-01', '2026-09-01', 'ACTIVE'),    -- david, Basic
(5, 5, '2026-01-15', '2027-01-15', 'ACTIVE'),    -- eve, Family
(6, 1, '2026-01-01', '2027-01-01', 'ACTIVE'),    -- frank, Free
(7, 3, '2025-12-01', '2026-06-01', 'EXPIRED'),   -- grace, expired Standard
(8, 2, '2026-04-01', '2026-10-01', 'ACTIVE'),    -- henry, Basic
(9, 1, '2026-02-01', '2027-02-01', 'ACTIVE');    -- ivy, Free

-- 4. Payments ----------------------------------------------------------
INSERT INTO payment (subscription_id, amount, method, status, transaction_ref) VALUES
(1, 15.99, 'CREDIT_CARD',   'COMPLETED', 'TXN-0001'),
(2,  9.99, 'PAYPAL',        'COMPLETED', 'TXN-0002'),
(3, 15.99, 'CREDIT_CARD',   'COMPLETED', 'TXN-0003'),
(4,  4.99, 'MOMO',          'COMPLETED', 'TXN-0004'),
(5, 19.99, 'BANK_TRANSFER', 'COMPLETED', 'TXN-0005'),
(7,  9.99, 'PAYPAL',        'REFUNDED',  'TXN-0007'),
(8,  4.99, 'ZALOPAY',       'COMPLETED', 'TXN-0008');

-- 5. Channels (one per creator-user) -----------------------------------
INSERT INTO channel (owner_user_id, channel_name, description, subscriber_count) VALUES
(1, 'Alice Cooks',     'Vietnamese & Asian cuisine tutorials',         0),
(2, 'Bob Tech Review', 'Latest gadget reviews and tech news',          0),
(3, 'Charlie Gaming',  'Playthroughs, esports & gaming tutorials',     0);

-- 6. Categories --------------------------------------------------------
INSERT INTO category (name, description) VALUES
('Music',        'Music videos, concerts, and playlists'),
('Gaming',       'Game reviews, playthroughs and esports'),
('Education',    'Tutorials, lectures, and documentaries'),
('Food',         'Cooking shows and food reviews'),
('Tech',         'Technology reviews, tutorials and news'),
('Entertainment','Movies, TV series, and shows'),
('Sports',       'Sports highlights and analysis'),
('News',         'Current events and news reports');

-- 7. Tags --------------------------------------------------------------
INSERT INTO tag (name) VALUES
('tutorial'), ('review'), ('beginner'), ('advanced'), ('vietnamese'),
('pho'),      ('iphone'), ('android'),  ('fps'),      ('rpg'),
('vlog'),     ('recipe'), ('unboxing'), ('esports');

-- 8. Videos ------------------------------------------------------------
INSERT INTO video (channel_id, title, description, video_url, thumbnail_url, duration_sec, resolution, status, is_premium, upload_date) VALUES
-- Alice Cooks
(1, 'How to make Pho Bo at home',       'Step-by-step traditional beef pho recipe', 'https://cdn.streamflix/v/1.mp4', 'https://cdn.streamflix/t/1.jpg',  900, 'FHD', 'PUBLISHED', FALSE, '2026-03-01 10:00:00'),
(1, 'Banh Mi: 3 Easy Variations',       'Classic, vegan and seafood banh mi',       'https://cdn.streamflix/v/2.mp4', 'https://cdn.streamflix/t/2.jpg',  720, 'FHD', 'PUBLISHED', FALSE, '2026-03-15 09:30:00'),
(1, 'Masterclass: Dumplings 101',       'Pro techniques for dumpling making',       'https://cdn.streamflix/v/3.mp4', 'https://cdn.streamflix/t/3.jpg', 1800, '4K',  'PUBLISHED', TRUE,  '2026-04-01 11:00:00'),
-- Bob Tech Review
(2, 'iPhone 17 Pro Review',             'In-depth review of the iPhone 17 Pro',     'https://cdn.streamflix/v/4.mp4', 'https://cdn.streamflix/t/4.jpg', 1500, '4K',  'PUBLISHED', FALSE, '2026-03-10 15:00:00'),
(2, 'Samsung S26 vs iPhone 17',         'Flagship comparison 2026',                  'https://cdn.streamflix/v/5.mp4', 'https://cdn.streamflix/t/5.jpg', 1200, 'FHD', 'PUBLISHED', FALSE, '2026-04-05 14:00:00'),
(2, 'Best Laptops under $1000 (2026)',  'Budget laptop roundup',                     'https://cdn.streamflix/v/6.mp4', 'https://cdn.streamflix/t/6.jpg', 1050, 'FHD', 'PUBLISHED', FALSE, '2026-04-12 10:00:00'),
-- Charlie Gaming
(3, 'Elden Ring 2 — Full Walkthrough',  'No-commentary full playthrough',            'https://cdn.streamflix/v/7.mp4', 'https://cdn.streamflix/t/7.jpg', 3600, '4K',  'PUBLISHED', TRUE,  '2026-02-20 20:00:00'),
(3, 'Valorant Ranked Tips 2026',        'Climb from Bronze to Diamond',              'https://cdn.streamflix/v/8.mp4', 'https://cdn.streamflix/t/8.jpg',  900, 'FHD', 'PUBLISHED', FALSE, '2026-04-10 19:00:00'),
(3, 'Top 10 Indie Games of 2026',       'Underrated indie gems',                     'https://cdn.streamflix/v/9.mp4', 'https://cdn.streamflix/t/9.jpg',  780, 'FHD', 'PUBLISHED', FALSE, '2026-04-15 18:00:00'),
(3, 'Pro FPS Aim Training (Draft)',     'Draft — not published yet',                 'https://cdn.streamflix/v/10.mp4','https://cdn.streamflix/t/10.jpg', 600, 'FHD', 'DRAFT',     FALSE, '2026-04-18 12:00:00');

-- 9. Video ↔ Category --------------------------------------------------
INSERT INTO video_category (video_id, category_id) VALUES
(1,4),(2,4),(3,4),(3,3),
(4,5),(5,5),(6,5),(6,3),
(7,2),(7,6),(8,2),(8,3),(9,2);

-- 10. Video ↔ Tag -----------------------------------------------------
INSERT INTO video_tag (video_id, tag_id) VALUES
(1,5),(1,6),(1,12),
(2,5),(2,12),
(3,1),(3,4),(3,12),
(4,2),(4,7),(4,13),
(5,2),(5,7),(5,8),
(6,2),(6,3),
(7,10),
(8,1),(8,9),(8,14),
(9,2);

-- 11. Channel subscriptions (users → channels) ------------------------
INSERT INTO channel_subscription (subscriber_user_id, channel_id) VALUES
(4,1),(4,2),(4,3),
(5,1),(5,3),
(6,2),(6,3),
(7,1),(7,2),
(8,3),
(9,1),(9,2),(9,3);

-- 12. Reactions --------------------------------------------------------
INSERT INTO video_reaction (user_id, video_id, reaction) VALUES
(4,1,'LIKE'),(5,1,'LIKE'),(6,1,'LIKE'),(7,1,'DISLIKE'),
(4,2,'LIKE'),(5,2,'LIKE'),
(4,4,'LIKE'),(6,4,'LIKE'),(7,4,'LIKE'),(9,4,'LIKE'),
(5,5,'LIKE'),(8,5,'DISLIKE'),
(4,7,'LIKE'),(6,7,'LIKE'),(8,7,'LIKE'),(9,7,'LIKE'),
(4,8,'LIKE'),(6,8,'LIKE'),
(4,9,'DISLIKE');

-- 13. Comments ---------------------------------------------------------
INSERT INTO comment (video_id, user_id, parent_comment_id, content) VALUES
(1, 4, NULL, 'Amazing recipe! Tried it tonight.'),
(1, 5, NULL, 'My family loved this, thanks Alice!'),
(1, 6, 1,    'Agree, the broth was perfect.'),
(4, 7, NULL, 'Best iPhone review so far.'),
(4, 8, NULL, 'Battery test inaccurate though.'),
(4, 2, 5,    'Noted — I will retest in next video.'),
(7, 6, NULL, 'Elden Ring 2 is masterpiece.'),
(7, 8, NULL, 'Part 3 boss — impossible.'),
(8, 4, NULL, 'Climbed to Platinum after this, thanks!');

-- 14. Watch history ----------------------------------------------------
INSERT INTO watch_history (user_id, video_id, watch_duration, progress_pct, device_type, watched_at) VALUES
(4, 1,  900, 100.00, 'WEB',     '2026-04-01 20:10:00'),
(4, 4, 1500, 100.00, 'iOS',     '2026-04-02 21:00:00'),
(4, 7, 1800,  50.00, 'TV',      '2026-04-03 22:00:00'),
(5, 1,  450,  50.00, 'ANDROID', '2026-04-05 19:00:00'),
(5, 2,  720, 100.00, 'ANDROID', '2026-04-06 20:00:00'),
(6, 4,  750,  50.00, 'WEB',     '2026-04-07 10:00:00'),
(6, 7, 3600, 100.00, 'TV',      '2026-04-08 22:00:00'),
(7, 1,  300,  33.33, 'WEB',     '2026-04-09 18:00:00'),
(7, 4, 1500, 100.00, 'WEB',     '2026-04-10 19:00:00'),
(8, 7, 3600, 100.00, 'iOS',     '2026-04-11 21:00:00'),
(8, 8,  900, 100.00, 'iOS',     '2026-04-12 22:00:00'),
(9, 1,  900, 100.00, 'WEB',     '2026-04-13 09:00:00'),
(9, 4, 1500, 100.00, 'WEB',     '2026-04-14 10:00:00'),
(9, 7, 1800,  50.00, 'WEB',     '2026-04-15 11:00:00');

-- 15. Playlists --------------------------------------------------------
INSERT INTO playlist (user_id, title, description, is_public) VALUES
(4, 'My Favourite Recipes', 'Best cooking videos',   TRUE),
(4, 'Watch Later',           'Videos to watch soon', FALSE),
(6, 'Gaming Marathon',       'Long RPG playthroughs', TRUE);

INSERT INTO playlist_video (playlist_id, video_id, position) VALUES
(1, 1, 1), (1, 2, 2), (1, 3, 3),
(2, 4, 1), (2, 5, 2),
(3, 7, 1), (3, 9, 2);

-- 16. Notifications ----------------------------------------------------
INSERT INTO notification (user_id, type, content, link_url, is_read) VALUES
(4, 'NEW_VIDEO',     'Alice Cooks posted: Masterclass Dumplings 101', '/watch/3', FALSE),
(4, 'COMMENT_REPLY', 'Bob replied to your comment on iPhone 17 Pro Review', '/watch/4', FALSE),
(5, 'NEW_VIDEO',     'Charlie Gaming posted: Top 10 Indie Games 2026', '/watch/9', TRUE),
(7, 'SUB_EXPIRING',  'Your Standard plan expired',                     '/account', FALSE);
