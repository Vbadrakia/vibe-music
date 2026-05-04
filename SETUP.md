# Vibe Music App — Complete Setup Guide

## Prerequisites
- Google account with Google Drive / Sheets access
- [Supabase](https://supabase.com) account (free)
- [Render](https://render.com) account (free)
- Android Studio (Hedgehog+)

---

## Step 1 — Google Cloud: Service Account

1. Go to [console.cloud.google.com](https://console.cloud.google.com) → **New Project** → name it `vibe-music`
2. Enable these APIs:
   - **Google Drive API**
   - **Google Sheets API**
3. **IAM & Admin → Service Accounts → Create Service Account**
   - Name: `vibe-backend`
   - Role: **Viewer** (read-only is enough)
4. Click the account → **Keys tab → Add Key → JSON**
5. Download the `.json` file — this is your `GOOGLE_SA_KEY`

> [!IMPORTANT]
> Never commit this JSON key to Git. Add it to Render as a secret env var.

---

## Step 2 — Google Sheets: Catalog Database

Create a Google Sheet named **Vibe Catalog**.

### Sheet: `Songs`
| A: id | B: title | C: artist | D: artist_id | E: album | F: album_id | G: duration_secs | H: cover_url | I: drive_file_id | J: genre | K: track_number | L: year | M: lyrics_lrc_url |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| song_001 | Blinding Lights | The Weeknd | artist_001 | After Hours | album_001 | 200 | https://... | 1BxiMVs... | Pop | 1 | 2020 | |

### Sheet: `Albums`
| A: id | B: title | C: artist_id | D: artist | E: year | F: cover_url | G: song_count | H: total_duration_secs |
|---|---|---|---|---|---|---|---|
| album_001 | After Hours | artist_001 | The Weeknd | 2020 | https://... | 14 | 3360 |

### Sheet: `Artists`
| A: id | B: name | C: cover_url | D: bio | E: monthly_listeners | F: is_verified | G: genres |
|---|---|---|---|---|---|---|
| artist_001 | The Weeknd | https://... | Canadian singer... | 95000000 | true | Pop,R&B |

### Sheet: `Genres`
| A: id | B: name | C: description | D: cover_url | E: color |
|---|---|---|---|---|
| pop | Pop | Catchy feel-good hits | https://... | #FF6B6B |
| hiphop | Hip-Hop | Beats and bars | https://... | #FFD93D |

> [!TIP]
> `cover_url` can be a publicly accessible image URL. Share the folder with link sharing enabled and use the direct image URL.

5. **Share this sheet** with the service account email as **Viewer**.
6. Copy the Sheet ID from the URL bar.

---

## Step 3 — Google Drive: Audio File Folder

1. Create a folder in Drive: `Vibe Music`
2. Upload your MP3/FLAC/AAC files
3. **Share the folder** with the service account email as **Viewer**
4. Get each file's **Drive File ID** from the file URL
5. Put that `FILE_ID` in column I of the `Songs` sheet

### Optional: LRC Lyrics Files
- Upload `.lrc` files to Drive
- Put the Drive File ID in column M of the Songs sheet

---

## Step 4 — Supabase: Auth Setup

1. Go to [supabase.com](https://supabase.com) → New Project
2. Note your keys:
   - `SUPABASE_URL` = `https://xxxx.supabase.co`
   - `SUPABASE_SERVICE_ROLE_KEY` = Service role key
   - `SUPABASE_JWT_SECRET` = Settings → API → JWT Secret
3. Enable **Email** auth provider

---

## Step 5 — Deploy Backend to Render

1. Push the `backend/` folder to GitHub
2. Go to [render.com](https://render.com) → **New Web Service**
3. Connect GitHub, set **Root Directory** to `backend`
4. Settings:
   - **Build Command:** `npm install && npm run build`
   - **Start Command:** `npm start`
   - **Node Version:** 20
5. Add all **Environment Variables** from `.env.example`
6. Deploy → copy the service URL

> [!NOTE]
> Render free tier spins down after 15 min of inactivity. Use UptimeRobot (free) to ping `/health` every 14 min.

---

## Step 6 — Configure Android App

In `NetworkModule.kt`, update the base URL:
```kotlin
@Provides
@BaseUrl
fun provideBaseUrl(): String = "https://vibe-backend.onrender.com/"
```

In `app/build.gradle.kts`, add BuildConfig fields:
```kotlin
buildConfigField("String", "BASE_API_URL", "\"https://vibe-backend.onrender.com/\"")
buildConfigField("String", "SUPABASE_URL", "\"https://xxxx.supabase.co\"")
buildConfigField("String", "SUPABASE_ANON_KEY", "\"your-anon-key\"")
```

---

## Step 7 — Running locally in Android Studio

1. **Open Android Studio**.
2. Click **Open** and select the `android` folder specifically (`Spotify_clone/android`). Do not open the root `Spotify_clone` folder, as Android Studio needs to see the Gradle files directly.
3. Wait for **Gradle Sync** to finish (watch the bottom progress bar).
4. **Start the backend server locally** (Optional, if testing the local backend):
   - Open a terminal and navigate to `Spotify_clone/backend`
   - Run `npm install`
   - Run `npm run dev`
   - Change `BASE_API_URL` to `"http://10.0.2.2:3000/"` in `build.gradle.kts`.
5. **Run the App:**
   - At the top of Android Studio, select your Emulator (or physical device).
   - Click the green **Run (▶)** button (or press `Shift + F10`).

---

## Full API Reference

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/health` | — | Health check |
| GET | `/api/songs` | — | List songs |
| GET | `/api/songs/:id` | — | Song by ID |
| GET | `/api/albums` | — | List albums |
| GET | `/api/albums/:id` | — | Album + tracks |
| GET | `/api/artists` | — | List artists |
| GET | `/api/artists/:id` | — | Artist detail |
| GET | `/api/genres` | — | Genre list |
| GET | `/api/genres/:id/songs` | — | Songs in genre |
| GET | `/api/search?q=` | — | Full-text search |
| GET | `/api/home` | — | Home feed |
| GET | `/api/stream/:fileId` | — | Audio stream |
| GET | `/api/lyrics/:songId` | — | LRC lyrics |
| GET | `/api/me/playlists` | JWT | My playlists |
| POST | `/api/me/playlists` | JWT | Create playlist |
| PUT | `/api/me/playlists/:id` | JWT | Update playlist |
| DELETE | `/api/me/playlists/:id` | JWT | Delete playlist |
| POST | `/api/me/playlists/:id/songs` | JWT | Add song |
| DELETE | `/api/me/playlists/:id/songs/:sid` | JWT | Remove song |
| GET | `/api/me/liked` | JWT | Liked songs |
| POST | `/api/me/liked/:song_id` | JWT | Like song |
| DELETE | `/api/me/liked/:song_id` | JWT | Unlike song |
| GET | `/api/me/history` | JWT | Listening history |
| POST | `/api/me/history` | JWT | Record play |
| GET | `/api/me/following/artists` | JWT | Followed artists |
| POST | `/api/me/following/artists/:id` | JWT | Follow artist |
| DELETE | `/api/me/following/artists/:id` | JWT | Unfollow artist |
