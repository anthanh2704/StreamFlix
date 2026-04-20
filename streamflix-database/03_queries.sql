-- StreamFlix - Query Catalogue
-- Demonstrates: SELECT / JOIN / GROUP BY / HAVING / subqueries /
-- CTEs / window functions / set operations.
USE streamflix_db;

-- BASIC QUERIES

-- Q1. List all published videos with their channel name
SELECT v.video_id, v.title, c.channel_name, v.views_count
FROM   video v
JOIN   channel c ON c.channel_id = v.channel_id
WHERE  v.status = 'PUBLISHED'
ORDER  BY v.upload_date DESC;

-- Q2. Users who registered from Vietnam
SELECT user_id, username, email, country
FROM   app_user
WHERE  country = 'Vietnam'
ORDER  BY created_at DESC;

-- Q3. Videos longer than 20 minutes, sorted by length
SELECT title, duration_sec, ROUND(duration_sec/60, 1) AS duration_min
FROM   video
WHERE  duration_sec > 1200
ORDER  BY duration_sec DESC;

-- Q4. Search videos containing a keyword (LIKE)
SELECT video_id, title
FROM   video
WHERE  title LIKE '%iPhone%' OR description LIKE '%iPhone%';

-- Q5. Full-text search (using FULLTEXT index)
SELECT video_id, title,
       MATCH(title, description) AGAINST ('iphone review' IN NATURAL LANGUAGE MODE) AS score
FROM   video
WHERE  MATCH(title, description) AGAINST ('iphone review' IN NATURAL LANGUAGE MODE)
ORDER  BY score DESC;


-- JOINS

-- Q6. Videos with every category they belong to (M:N join)
SELECT v.title, GROUP_CONCAT(cat.name ORDER BY cat.name SEPARATOR ', ') AS categories
FROM   video v
JOIN   video_category vc ON vc.video_id = v.video_id
JOIN   category cat      ON cat.category_id = vc.category_id
GROUP  BY v.video_id, v.title;

-- Q7. Channels and the list of users subscribed to them
SELECT c.channel_name, u.username AS subscriber
FROM   channel c
LEFT JOIN channel_subscription cs ON cs.channel_id = c.channel_id
LEFT JOIN app_user u              ON u.user_id = cs.subscriber_user_id
ORDER  BY c.channel_name, u.username;

-- Q8. Users who NEVER watched a video (LEFT JOIN + NULL check)
SELECT u.user_id, u.username
FROM   app_user u
LEFT JOIN watch_history h ON h.user_id = u.user_id
WHERE  h.history_id IS NULL
  AND  u.role <> 'ADMIN';


-- AGGREGATES / GROUP BY / HAVING

-- Q9. Videos per channel
SELECT c.channel_name, COUNT(v.video_id) AS total_videos
FROM   channel c
LEFT JOIN video v ON v.channel_id = c.channel_id
GROUP  BY c.channel_id, c.channel_name
ORDER  BY total_videos DESC;

-- Q10. Average watch duration per video (and % of total length)
SELECT v.title,
       v.duration_sec,
       ROUND(AVG(h.watch_duration), 0)                    AS avg_watched_sec,
       ROUND(AVG(h.watch_duration)*100/v.duration_sec, 1) AS avg_pct
FROM   video v
JOIN   watch_history h ON h.video_id = v.video_id
GROUP  BY v.video_id, v.title, v.duration_sec
ORDER  BY avg_pct DESC;

-- Q11. Channels with more than 2 videos AND total views > 0
SELECT c.channel_name,
       COUNT(v.video_id)  AS videos,
       SUM(v.views_count) AS total_views
FROM   channel c
JOIN   video v ON v.channel_id = c.channel_id
GROUP  BY c.channel_id, c.channel_name
HAVING COUNT(v.video_id) > 2
   AND SUM(v.views_count) > 0;

-- Q12. Daily new signup count in the last 30 days
SELECT DATE(created_at) AS day, COUNT(*) AS new_users
FROM   app_user
WHERE  created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP  BY DATE(created_at)
ORDER  BY day;


-- SUBQUERIES

-- Q13. Videos whose view count is above the average
SELECT title, views_count
FROM   video
WHERE  views_count > (SELECT AVG(views_count) FROM video);

-- Q14. Users who subscribed to EVERY channel (division)
SELECT u.username
FROM   app_user u
WHERE  NOT EXISTS (
          SELECT 1
          FROM   channel c
          WHERE  NOT EXISTS (
                    SELECT 1
                    FROM   channel_subscription cs
                    WHERE  cs.channel_id = c.channel_id
                      AND  cs.subscriber_user_id = u.user_id));

-- Q15. For every channel, find its most viewed video (correlated subquery)
SELECT c.channel_name, v.title, v.views_count
FROM   channel c
JOIN   video   v ON v.channel_id = c.channel_id
WHERE  v.views_count = (SELECT MAX(v2.views_count)
                          FROM   video v2
                          WHERE  v2.channel_id = c.channel_id);

-- Q16. Users who liked AT LEAST ONE video from channel 'Alice Cooks'
SELECT DISTINCT u.username
FROM   app_user u
WHERE  u.user_id IN (
          SELECT vr.user_id
          FROM   video_reaction vr
          JOIN   video v   ON v.video_id = vr.video_id
          JOIN   channel c ON c.channel_id = v.channel_id
          WHERE  c.channel_name = 'Alice Cooks'
            AND  vr.reaction   = 'LIKE');


-- SET OPERATIONS

-- Q17. Users who are creators OR have an active paid plan (UNION)
SELECT user_id, username, 'creator' AS reason FROM app_user WHERE role = 'CREATOR'
UNION
SELECT u.user_id, u.username, 'paid'
FROM   app_user u
JOIN   user_subscription us ON us.user_id = u.user_id
JOIN   subscription_plan sp ON sp.plan_id  = us.plan_id
WHERE  us.status = 'ACTIVE' AND sp.price > 0;

-- Q18. Users who watched videos but never commented (EXCEPT / NOT IN)
SELECT DISTINCT u.user_id, u.username
FROM   app_user u
JOIN   watch_history h ON h.user_id = u.user_id
WHERE  u.user_id NOT IN (SELECT user_id FROM comment);


-- CTEs AND WINDOW FUNCTIONS (advanced / analytics)

-- Q19. Top-3 most viewed videos per channel (CTE + ROW_NUMBER)
WITH ranked AS (
  SELECT v.channel_id, v.video_id, v.title, v.views_count,
         ROW_NUMBER() OVER (PARTITION BY v.channel_id
                            ORDER BY v.views_count DESC) AS rn
  FROM   video v
  WHERE  v.status = 'PUBLISHED'
)
SELECT c.channel_name, r.title, r.views_count, r.rn
FROM   ranked r
JOIN   channel c ON c.channel_id = r.channel_id
WHERE  r.rn <= 3
ORDER  BY c.channel_name, r.rn;

-- Q20. Running total of monthly revenue
WITH monthly AS (
  SELECT DATE_FORMAT(payment_date, '%Y-%m') AS ym,
         SUM(amount) AS monthly_revenue
  FROM   payment
  WHERE  status = 'COMPLETED'
  GROUP  BY DATE_FORMAT(payment_date, '%Y-%m')
)
SELECT ym,
       monthly_revenue,
       SUM(monthly_revenue) OVER (ORDER BY ym) AS cumulative_revenue
FROM   monthly;

-- Q21. Recommendation: similar users (users who watched the same video as user 4)
WITH u4_videos AS (
  SELECT DISTINCT video_id FROM watch_history WHERE user_id = 4
)
SELECT u.user_id, u.username,
       COUNT(*) AS shared_videos
FROM   watch_history h
JOIN   app_user u ON u.user_id = h.user_id
WHERE  h.video_id IN (SELECT video_id FROM u4_videos)
  AND  h.user_id  <> 4
GROUP  BY u.user_id, u.username
ORDER  BY shared_videos DESC
LIMIT  5;

-- Q22. Retention: of users that signed up in Jan 2026, how many are still active?
SELECT COUNT(DISTINCT u.user_id) AS jan_signups,
       COUNT(DISTINCT CASE WHEN us.status='ACTIVE' THEN u.user_id END) AS still_active
FROM   app_user u
LEFT JOIN user_subscription us ON us.user_id = u.user_id
WHERE  u.created_at BETWEEN '2026-01-01' AND '2026-01-31';

-- Q23. Revenue by plan and country
SELECT u.country, sp.plan_name, SUM(p.amount) AS revenue
FROM   payment p
JOIN   user_subscription us ON us.subscription_id = p.subscription_id
JOIN   subscription_plan sp ON sp.plan_id = us.plan_id
JOIN   app_user u ON u.user_id = us.user_id
WHERE  p.status = 'COMPLETED'
GROUP  BY u.country, sp.plan_name WITH ROLLUP;

-- Q24. Engagement score per video: views + 2*likes - dislikes
SELECT v.video_id, v.title,
       v.views_count,
       v.likes_count,
       v.dislikes_count,
       (v.views_count + 2 * v.likes_count - v.dislikes_count) AS engagement_score
FROM   video v
WHERE  v.status = 'PUBLISHED'
ORDER  BY engagement_score DESC
LIMIT  10;

-- Q25. Find users who reached EVERY category (watched at least 1 video in each)
-- Relational division using double-negation
SELECT u.user_id, u.username
FROM   app_user u
WHERE  NOT EXISTS (
    SELECT 1 FROM category cat
    WHERE NOT EXISTS (
        SELECT 1
        FROM watch_history h
        JOIN video_category vc ON vc.video_id = h.video_id
        WHERE h.user_id = u.user_id
          AND vc.category_id = cat.category_id));