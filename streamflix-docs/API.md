# REST API Reference

Base URL: `http://localhost:8080/api`
All responses are JSON envelopes: `{ "success": boolean, "message": "…", "data": {...} }`.

## Authentication

| Method | Path             | Auth | Purpose |
|--------|------------------|------|---------|
| POST   | `/auth/register` | — | Create a user account |
| GET    | `/auth/me`       | Basic | Return the current user |
| GET    | `/auth/health`   | — | Liveness probe |

Every authenticated endpoint uses **HTTP Basic** — the client sends `Authorization: Basic base64(username:password)`.

## Videos

| Method | Path                         | Auth | Purpose |
|--------|------------------------------|------|---------|
| GET    | `/videos`                    | —    | Paged list of published videos |
| GET    | `/videos/search?q=…`         | —    | Keyword search |
| GET    | `/videos/trending?days=N`    | —    | Top videos in the last N days |
| GET    | `/videos/category/{id}`      | —    | Videos in a category |
| GET    | `/videos/{id}`               | —    | Fetch one video |
| POST   | `/videos/channel/{channelId}`| Basic (creator) | Upload |
| DELETE | `/videos/{id}`               | Basic (creator/admin) | Soft-delete |
| POST   | `/videos/{id}/watch`         | Basic | Record a watch event |
| POST   | `/videos/{id}/like`          | Basic | Toggle like |
| POST   | `/videos/{id}/dislike`       | Basic | Toggle dislike |
| GET    | `/videos/recommendations`    | Basic | Personalised recommendations |

## Comments

| Method | Path                          | Auth | Purpose |
|--------|-------------------------------|------|---------|
| GET    | `/comments/video/{videoId}`   | —    | Top-level comments (paged) |
| GET    | `/comments/{commentId}/replies`| —   | All replies to a comment |
| POST   | `/comments/video/{videoId}`   | Basic | Post a comment or reply |
| DELETE | `/comments/{id}`              | Basic | Delete |

## Channels

| Method | Path                          | Auth | Purpose |
|--------|-------------------------------|------|---------|
| GET    | `/channels`                   | —    | List all channels |
| GET    | `/channels/{id}`              | —    | Channel details |
| POST   | `/channels`                   | Basic | Create a channel (promotes user → creator) |
| POST   | `/channels/{id}/subscribe`    | Basic | Subscribe / unsubscribe toggle |

## Users

| Method | Path                 | Auth | Purpose |
|--------|----------------------|------|---------|
| GET    | `/users/{id}`        | —    | Public profile |
| GET    | `/users/me/history`  | Basic | Your watch history (paged) |

## Categories

| Method | Path          | Auth | Purpose |
|--------|---------------|------|---------|
| GET    | `/categories` | —    | All categories |

## Error response format

```json
{
  "success": false,
  "message": "Video not found with id 99",
  "data": null
}
```

HTTP status codes:

| Code | When |
|------|------|
| 200  | OK |
| 400  | Validation or business rule violation |
| 401  | Bad/missing credentials |
| 403  | Authenticated but lacking permissions |
| 404  | Resource does not exist |
| 500  | Unhandled server error |
