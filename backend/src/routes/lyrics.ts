import { FastifyInstance } from 'fastify';
import { getDriveFileText, parseLrc } from '../services/drive.js';
import { getAllSongs } from '../services/sheets.js';
import { ok } from '../types.js';
import { LRUCache } from 'lru-cache';
import { config } from '../config.js';

const lyricsCache = new LRUCache<string, any>({
  max: 500,
  ttl: config.cache.ttlSeconds * 1000 * 6, // lyrics cached 30 min
});

export async function lyricsRoutes(app: FastifyInstance) {

  // ── GET /api/lyrics/:songId ────────────────────────────────────────────────
  app.get<{ Params: { songId: string } }>('/api/lyrics/:songId', async (req, reply) => {
    const { songId } = req.params;

    const cached = lyricsCache.get(songId);
    if (cached) return reply.send(ok(cached));

    const songs = await getAllSongs();
    const song = songs.find(s => s.id === songId);

    if (!song) {
      return reply.code(404).send({ success: false, error: 'Song not found' });
    }

    if (!song.lyrics_lrc_url) {
      return reply.send(ok([])); // No lyrics available
    }

    try {
      // lyrics_lrc_url can be a Drive file ID or a full https URL
      let lrcText: string;

      if (song.lyrics_lrc_url.startsWith('http')) {
        // External URL (e.g., LRCLIB)
        const res = await fetch(song.lyrics_lrc_url);
        lrcText = await res.text();
      } else {
        // Drive file ID
        lrcText = await getDriveFileText(song.lyrics_lrc_url);
      }

      const lines = parseLrc(lrcText);
      lyricsCache.set(songId, lines);
      return reply.send(ok(lines));

    } catch (err: any) {
      app.log.error(err, `Lyrics fetch error for songId=${songId}`);
      return reply.send(ok([]));
    }
  });
}
