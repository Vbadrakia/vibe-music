import { google, sheets_v4 } from 'googleapis';
import { LRUCache } from 'lru-cache';
import { config } from '../config.js';
import type { Song, Album, Artist, Genre } from '../types.js';

// ── Auth ────────────────────────────────────────────────────────────────────────
const auth = new google.auth.GoogleAuth({
  credentials: config.google.serviceAccountKey,
  scopes: [
    'https://www.googleapis.com/auth/spreadsheets.readonly',
    'https://www.googleapis.com/auth/drive.readonly',
  ],
});

export const sheetsClient = google.sheets({ version: 'v4', auth });

// ── In-memory LRU cache ─────────────────────────────────────────────────────────
const cache = new LRUCache<string, any>({
  max: 200,
  ttl: config.cache.ttlSeconds * 1000,
});

async function cached<T>(key: string, fn: () => Promise<T>): Promise<T> {
  const hit = cache.get(key);
  if (hit !== undefined) return hit as T;
  const result = await fn();
  cache.set(key, result);
  return result;
}

// ── Sheet reader helper ─────────────────────────────────────────────────────────
async function readSheet(range: string): Promise<string[][]> {
  const res = await sheetsClient.spreadsheets.values.get({
    spreadsheetId: config.google.sheetsId,
    range,
  });
  return (res.data.values ?? []) as string[][];
}

// ──────────────────────────────────────────────────────────────────────────────
//  Sheet structure expected:
//
//  Sheet "Songs":   id | title | artist | artist_id | album | album_id | duration_secs | cover_url | drive_file_id | genre | track_number | year | lyrics_lrc_url
//  Sheet "Albums":  id | title | artist_id | artist | year | cover_url | song_count | total_duration_secs
//  Sheet "Artists": id | name | cover_url | bio | monthly_listeners | is_verified | genres(comma-sep)
//  Sheet "Genres":  id | name | description | cover_url | color(hex)
// ──────────────────────────────────────────────────────────────────────────────

// ── Songs ──────────────────────────────────────────────────────────────────────
export async function getAllSongs(): Promise<Song[]> {
  return cached('all_songs', async () => {
    const rows = await readSheet('Songs!A2:M');
    return rows.map(rowToSong).filter(Boolean) as Song[];
  });
}

function rowToSong(r: string[]): Song | null {
  if (!r[0] || !r[1]) return null;
  return {
    id: r[0],
    title: r[1],
    artist: r[2] ?? '',
    artist_id: r[3] ?? '',
    album: r[4] ?? '',
    album_id: r[5] ?? '',
    duration_secs: parseInt(r[6] ?? '0', 10),
    cover_url: r[7] ?? '',
    drive_file_id: r[8] ?? '',
    genre: r[9] ?? undefined,
    track_number: r[10] ? parseInt(r[10], 10) : undefined,
    year: r[11] ? parseInt(r[11], 10) : undefined,
    lyrics_lrc_url: r[12] ?? undefined,
  };
}

// ── Albums ─────────────────────────────────────────────────────────────────────
export async function getAllAlbums(): Promise<Album[]> {
  return cached('all_albums', async () => {
    const rows = await readSheet('Albums!A2:H');
    return rows.map(rowToAlbum).filter(Boolean) as Album[];
  });
}

function rowToAlbum(r: string[]): Album | null {
  if (!r[0] || !r[1]) return null;
  return {
    id: r[0],
    title: r[1],
    artist_id: r[2] ?? '',
    artist: r[3] ?? '',
    year: parseInt(r[4] ?? '0', 10),
    cover_url: r[5] ?? '',
    song_count: parseInt(r[6] ?? '0', 10),
    total_duration_secs: parseInt(r[7] ?? '0', 10),
  };
}

// ── Artists ────────────────────────────────────────────────────────────────────
export async function getAllArtists(): Promise<Artist[]> {
  return cached('all_artists', async () => {
    const rows = await readSheet('Artists!A2:G');
    return rows.map(rowToArtist).filter(Boolean) as Artist[];
  });
}

function rowToArtist(r: string[]): Artist | null {
  if (!r[0] || !r[1]) return null;
  return {
    id: r[0],
    name: r[1],
    cover_url: r[2] ?? '',
    bio: r[3] ?? undefined,
    monthly_listeners: parseInt(r[4] ?? '0', 10),
    is_verified: r[5]?.toLowerCase() === 'true',
    genres: r[6] ? r[6].split(',').map(g => g.trim()) : [],
  };
}

// ── Genres ─────────────────────────────────────────────────────────────────────
export async function getAllGenres(): Promise<Genre[]> {
  return cached('all_genres', async () => {
    const rows = await readSheet('Genres!A2:E');
    return rows.map(rowToGenre).filter(Boolean) as Genre[];
  });
}

function rowToGenre(r: string[]): Genre | null {
  if (!r[0] || !r[1]) return null;
  return {
    id: r[0],
    name: r[1],
    description: r[2] ?? '',
    cover_url: r[3] ?? '',
    color: r[4] ?? '#1DB954',
  };
}

// ── Invalidate cache (call after any write) ────────────────────────────────────
export function invalidateCache(keys?: string[]) {
  if (keys) keys.forEach(k => cache.delete(k));
  else cache.clear();
}
