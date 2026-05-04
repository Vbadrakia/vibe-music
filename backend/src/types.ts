// ── Shared Types ───────────────────────────────────────────────────────────────

export interface Song {
  id: string;
  title: string;
  artist: string;
  artist_id: string;
  album: string;
  album_id: string;
  duration_secs: number;
  cover_url: string;
  drive_file_id: string;
  genre?: string;
  track_number?: number;
  year?: number;
  lyrics_lrc_url?: string;
}

export interface Album {
  id: string;
  title: string;
  artist_id: string;
  artist: string;
  year: number;
  cover_url: string;
  song_count: number;
  total_duration_secs: number;
  songs?: Song[];
}

export interface Artist {
  id: string;
  name: string;
  cover_url: string;
  bio?: string;
  monthly_listeners: number;
  is_verified: boolean;
  genres?: string[];
}

export interface Genre {
  id: string;
  name: string;
  description: string;
  cover_url: string;
  color: string;
}

export interface PlaylistMeta {
  id: string;
  name: string;
  description?: string;
  cover_url: string;
  song_count: number;
}

export interface LyricLine {
  timeMs: number;
  text: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
}

export function ok<T>(data: T): ApiResponse<T> {
  return { success: true, data };
}

export function err(message: string, statusCode = 500) {
  return { success: false, error: message, statusCode };
}
