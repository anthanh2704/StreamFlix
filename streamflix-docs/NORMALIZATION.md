# Normalization Analysis

This document walks every relation through the normal forms taught in Lecture 8 (1NF → 2NF → 3NF → BCNF).

## 0. Why normalize?

Without normalization we would hit the classic anomalies described in the lectures:

| Anomaly     | Example in an un-normalized design |
|-------------|------------------------------------|
| **Insertion** | Cannot add a new category until at least one video uses it. |
| **Update**    | Changing a channel's name requires updating every video row. |
| **Deletion**  | Deleting the last video of a channel loses the channel's metadata. |

Our design is entirely **3NF** and, in the majority of tables, **BCNF**.

---

## 1. First Normal Form (1NF)

**Rule:** every attribute is atomic; no repeating groups.

Every relation passes 1NF:
- Categories and tags — the natural "multivalued" attributes of a video — are externalized into `video_category` / `video_tag`.
- No comma-separated fields, no arrays stored as strings.

---

## 2. Second Normal Form (2NF)

**Rule:** 1NF + no partial dependency on a composite key (every non-prime attribute fully depends on the whole PK).

Only the M:N relations have composite keys:

| Relation | PK | Non-prime attributes | Verdict |
|----------|----|--------------------|---------|
| `video_category` | (video_id, category_id) | *none* | 2NF trivially |
| `video_tag`      | (video_id, tag_id)      | *none* | 2NF trivially |
| `video_reaction` | (user_id, video_id) | reaction, reacted_at | reaction & reacted_at depend on both keys — **2NF** |
| `channel_subscription` | (subscriber_user_id, channel_id) | subscribed_at, notifications_on | depend on the full key — **2NF** |
| `playlist_video` | (playlist_id, video_id) | position, added_at | depend on both — **2NF** |

All tables with single-column PKs are automatically in 2NF.

---

## 3. Third Normal Form (3NF)

**Rule:** 2NF + no transitive dependency of a non-prime attribute on the primary key.

### Case analysis

**`app_user`** — FDs: `user_id → {everything else}`. No FD among non-prime attributes (e.g. `country` does not determine `role`). ✅ 3NF.

**`video`** — FDs: `video_id → {title, channel_id, duration_sec, … }`. Note that `channel_id → ` other attributes is *not* the case here because we store only the FK, not the denormalized channel name. ✅ 3NF.

**`comment`** — `comment_id → {video_id, user_id, parent_comment_id, content, likes_count, created_at}`. Nothing transitive. ✅ 3NF.

**`user_subscription`** — `subscription_id → {user_id, plan_id, start_date, end_date, status, auto_renew}`.
The price is *not* stored here (it lives in `subscription_plan`), so there is no `plan_id → price → subscription_id` transitive dependency. ✅ 3NF.

**`payment`** — `payment_id → {subscription_id, amount, method, status, transaction_ref, payment_date}`. ✅ 3NF.

**`channel`** — `channel_id → {owner_user_id, channel_name, …}`. `owner_user_id` is an FK and is unique, so `owner_user_id → channel_id` also holds — this makes both columns keys but does not break 3NF (both sides are superkeys). ✅ 3NF / also BCNF.

All other tables contain only PK + FKs, and are trivially in 3NF.

---

## 4. Boyce-Codd Normal Form (BCNF)

**Rule:** 3NF + for every non-trivial FD `X → Y`, `X` must be a superkey.

### Analysis

Every table except those with multiple candidate keys is already BCNF (since the only determinant is the single PK).

The one relation with **two candidate keys** is `channel`:
- `channel_id`        → all
- `owner_user_id` (UNIQUE) → all

Both are superkeys, and both FDs respect the BCNF condition. ✅ BCNF.

`payment.transaction_ref` is `UNIQUE`, therefore a candidate key:
- `payment_id`     → all
- `transaction_ref` → all
Both are superkeys. ✅ BCNF.

`app_user.username` and `app_user.email` are also `UNIQUE`, so they are alternative candidate keys. Every FD `username → …` or `email → …` has a superkey on the LHS. ✅ BCNF.

---

## 5. Fourth Normal Form (4NF)

**Rule:** BCNF + no non-trivial multi-valued dependencies besides the key.

The archetypal 4NF violation is the pair (user, skill) vs. (user, language) stored together in one relation. In our design, independent multi-valued facts about a video — **categories** and **tags** — are already split into separate tables (`video_category`, `video_tag`). There is no relation that mixes independent sets, so the whole schema satisfies 4NF.

---

## 6. Intentional denormalization

A handful of counter columns are **kept denormalized** for read performance:

| Column                        | Derivable from               | Maintained by |
|-------------------------------|------------------------------|---------------|
| `video.views_count`           | `COUNT(watch_history)`       | `trg_after_watch_insert` |
| `video.likes_count`           | `COUNT(video_reaction LIKE)` | `trg_after_reaction_insert/delete` |
| `video.dislikes_count`        | `COUNT(video_reaction DISLIKE)` | same |
| `channel.subscriber_count`    | `COUNT(channel_subscription)`| `trg_after_sub_insert/delete` |

These violate 3NF in theory (the counter is derivable from other tables) but are required at scale — recomputing counts on every page load would be prohibitive. The triggers guarantee the counters stay consistent, so the denormalization is safe.

---

## 7. Summary

| Table | Normal form | Reason |
|-------|-------------|--------|
| `app_user`            | BCNF |  Multiple UNIQUE candidate keys, all superkeys |
| `subscription_plan`   | BCNF |  single PK |
| `user_subscription`   | 3NF  |  No transitive; plan price stays in `subscription_plan` |
| `payment`             | BCNF |  `payment_id` and `transaction_ref` both superkeys |
| `channel`             | BCNF |  Two candidate keys, both superkeys |
| `category` / `tag`    | BCNF |  Single PK + UNIQUE name |
| `video`               | 3NF  |  Denormalized counters intentional |
| `comment`             | BCNF |  Single PK |
| `watch_history`       | BCNF |  Surrogate PK |
| `video_reaction`      | BCNF |  Composite PK is the only determinant |
| `channel_subscription`| BCNF |  Composite PK |
| `playlist` / `playlist_video` | BCNF | surrogate PK / composite PK |
| `notification`        | BCNF |  single PK |

**Overall: the schema is in BCNF**, with a small, clearly-justified denormalization for counter columns protected by triggers.
