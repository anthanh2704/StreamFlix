# ERD → Relational Schema Translation

Following the 7-step algorithm taught in Lecture 3 of IT079IU.

## Conventions
- Primary keys are **bold** and underlined.
- Foreign keys are *italicised* and followed by `→ Table(column)`.

---

## Step 1 — Regular (strong) entity types

Each strong entity becomes a table with only its simple attributes.

```
SUBSCRIPTION_PLAN(__plan_id__, plan_name, price, max_quality, max_devices, description)

APP_USER(__user_id__, username, email, password_hash, full_name,
         date_of_birth, country, avatar_url, role, is_active,
         created_at, last_login)

CATEGORY(__category_id__, name, description)

TAG(__tag_id__, name)
```

---

## Step 2 — Weak entity types

Not strictly present in the logical model because all children carry synthetic surrogate keys (`history_id`, `payment_id`, etc.). They behave as *identifying relationships*: the foreign key is `NOT NULL` and on-delete cascades.

---

## Step 3 — Binary 1 : 1 (owner)

`APP_USER ||—o| CHANNEL` → foreign key goes on the *optional* side (`channel`) and is marked `UNIQUE`.

```
CHANNEL(__channel_id__, *owner_user_id* UNIQUE → APP_USER(user_id),
        channel_name, description, banner_url, subscriber_count, created_at)
```

---

## Step 4 — Binary 1 : N

Put the FK on the "N" side.

```
VIDEO(__video_id__, *channel_id* → CHANNEL(channel_id),
      title, description, video_url, thumbnail_url, duration_sec,
      resolution, views_count, likes_count, dislikes_count,
      status, is_premium, upload_date)

COMMENT(__comment_id__, *video_id* → VIDEO(video_id),
        *user_id* → APP_USER(user_id),
        *parent_comment_id* → COMMENT(comment_id),   -- self-reference
        content, likes_count, created_at)

PLAYLIST(__playlist_id__, *user_id* → APP_USER(user_id),
         title, description, is_public, created_at)

NOTIFICATION(__notification_id__, *user_id* → APP_USER(user_id),
             type, content, link_url, is_read, created_at)

PAYMENT(__payment_id__, *subscription_id* → USER_SUBSCRIPTION(subscription_id),
        amount, payment_date, method, status, transaction_ref)
```

---

## Step 5 — Binary M : N

Each M:N relationship becomes its own relation whose PK is the concatenation of the two foreign keys.

```
USER_SUBSCRIPTION(__subscription_id__,            -- surrogate key
                  *user_id*  → APP_USER(user_id),
                  *plan_id*  → SUBSCRIPTION_PLAN(plan_id),
                  start_date, end_date, status, auto_renew)

VIDEO_CATEGORY(__video_id__, __category_id__)
VIDEO_TAG     (__video_id__, __tag_id__)
PLAYLIST_VIDEO(__playlist_id__, __video_id__, position)

WATCH_HISTORY(__history_id__,                     -- surrogate PK
              *user_id*  → APP_USER(user_id),
              *video_id* → VIDEO(video_id),
              watched_at, watch_duration, progress_pct, device_type)

VIDEO_REACTION(__user_id__, __video_id__, reaction, reacted_at)

CHANNEL_SUBSCRIPTION(__subscriber_user_id__, __channel_id__,
                     subscribed_at, notifications_on)
```

---

## Step 6 — Multivalued attributes

There are no pure multivalued attributes in this design; tags and categories are represented by M:N relations already handled in Step 5.

---

## Step 7 — Specialization / Generalization

The only ISA hierarchy is `APP_USER` with subtypes `USER`, `CREATOR`, `ADMIN`. We chose **single relation with one type attribute** (option C in lecture 3) because:

- Subclasses share almost all attributes (username, email, …).
- The only subclass-specific data (`channel`) is already captured via the 1:1 relationship.
- The ENUM column `role` is compact and avoids `NULL` columns.

---

## Final Schema (condensed)

16 tables total:

1. `subscription_plan`
2. `app_user`
3. `user_subscription`
4. `payment`
5. `channel`
6. `category`
7. `video`
8. `video_category`
9. `tag`
10. `video_tag`
11. `channel_subscription`
12. `video_reaction`
13. `comment`
14. `watch_history`
15. `playlist`
16. `playlist_video`
17. `notification`

All referential integrity constraints enforced with `FOREIGN KEY … ON DELETE CASCADE` (or `RESTRICT` where deletion would lose business data).
