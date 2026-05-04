import { parseStream } from 'music-metadata';
import type { IAudioMetadata } from 'music-metadata';

/**
 * Extract audio metadata from a readable stream
 * Supports: MP3, FLAC, AAC, OGG, and other formats
 */
export async function extractAudioMetadata(
  stream: NodeJS.ReadableStream
): Promise<{
  title: string;
  artist: string;
  album: string;
  duration: number;
  year?: number;
}> {
  const metadata: IAudioMetadata = await parseStream(stream);

  const {
    common: { title, artist, album, year } = {},
    format: { duration } = {},
  } = metadata;

  // Fallback to filename if title is missing
  const finalTitle = title || 'Unknown Title';
  const finalArtist = artist || 'Unknown Artist';
  const finalAlbum = album || 'Unknown Album';
  const finalDuration = Math.round(duration || 0);
  const finalYear = year || new Date().getFullYear();

  return {
    title: finalTitle,
    artist: finalArtist,
    album: finalAlbum,
    duration: finalDuration,
    year: finalYear,
  };
}

/**
 * Generate a unique song ID based on artist + title
 */
export function generateSongId(artist: string, title: string): string {
  const timestamp = Date.now();
  return `S${timestamp}`;
}

/**
 * Format duration in seconds to readable string (e.g., "3:45")
 */
export function formatDuration(seconds: number): string {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins}:${secs.toString().padStart(2, '0')}`;
}
