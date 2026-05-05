import { FastifyInstance } from 'fastify';
import { config } from '../config.js';
import { requireAuth, getUserId } from '../middleware/auth.js';
import { getAllSongs, appendSongToCatalog } from '../services/catalog.js';
import { uploadDriveFile } from '../services/drive.js';
import { ok } from '../types.js';
import { parseBuffer } from 'music-metadata';
import { randomUUID } from 'node:crypto';
import type { Song } from '../types.js';

// ── In-memory user store (replace with Supabase DB in production) ──────────────
// Structure: { userId -> { likedSongs: Set<string>, playlists: Map<string, Playlist>, history: HistoryEntry[] } }

interface Playlist {
  id: string;
  name: string;
  description?: string;
  cover_url?: string;
  owner_id: string;
  owner_name: string;
  song_ids: string[];
  created_at: number;
}

interface HistoryEntry {
  song_id: string;
  played_at: number;
  duration_ms: number;
}

const userStore = new Map<string, {
  likedSongs: Set<string>;
  playlists: Map<string, Playlist>;
  history: HistoryEntry[];
}>();

function getUser(userId: string) {
  if (!userStore.has(userId)) {
    userStore.set(userId, { likedSongs: new Set(), playlists: new Map(), history: [] });
  }
  return userStore.get(userId)!;
}

function generateId() {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;
}

function slugify(value: string) {
  const slug = value
    .normalize('NFKD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '');

  return slug || 'unknown';
}

function pickString(value: unknown) {
  if (typeof value !== 'string') return '';
  return value.trim();
}

function parseNumber(value: unknown) {
  if (typeof value !== 'string' || !value.trim()) return undefined;
  const parsed = Number.parseInt(value, 10);
  return Number.isFinite(parsed) ? parsed : undefined;
}

function filenameWithoutExtension(filename: string) {
  return filename.replace(/\.[^.]+$/, '');
}

async function streamToBuffer(stream: NodeJS.ReadableStream) {
  const chunks: Buffer[] = [];
  for await (const chunk of stream) {
    chunks.push(Buffer.isBuffer(chunk) ? chunk : Buffer.from(chunk));
  }
  return Buffer.concat(chunks);
}

export async function userRoutes(app: FastifyInstance) {

  app.post('/api/me/songs', { preHandler: requireAuth }, async (req, reply) => {
    const uid = getUserId(req);

    let fileBuffer: Buffer | undefined;
    let fileName = `upload-${Date.now()}`;
    let mimeType = 'audio/mpeg';
    const fields: Record<string, string> = {};

    for await (const part of req.parts()) {
      if (part.type === 'file') {
        if (part.fieldname !== 'file') {
          part.file.resume();
          continue;
        }

        fileName = part.filename ?? fileName;
        mimeType = part.mimetype ?? mimeType;
        fileBuffer = await streamToBuffer(part.file);
      } else {
        fields[part.fieldname] = String(part.value ?? '');
      }
    }

    if (!fileBuffer) {
      return reply.code(400).send({ success: false, error: 'Audio file is required' });
    }

    let metadata: Awaited<ReturnType<typeof parseBuffer>> | null = null;
    try {
      metadata = await parseBuffer(fileBuffer, mimeType);
    } catch {
      metadata = null;
    }

    const title = pickString(fields.title) || metadata?.common.title?.trim() || filenameWithoutExtension(fileName);
    const artist = pickString(fields.artist) || metadata?.common.artists?.[0]?.trim() || 'Unknown Artist';
    const album = pickString(fields.album) || metadata?.common.album?.trim() || 'Single';
    const genre = pickString(fields.genre) || metadata?.common.genre?.[0]?.trim() || undefined;
    const trackNumber = parseNumber(fields.track_number) ?? (typeof metadata?.common.track?.no === 'number' ? metadata.common.track.no : undefined);
    const year = parseNumber(fields.year) ?? parseNumber(String(metadata?.common.year ?? ''));
    const durationValue = Number(metadata?.format.duration ?? fields.duration_secs ?? 0);
    const durationSecs = Number.isFinite(durationValue) ? Math.max(1, Math.round(durationValue)) : 1;

    const songId = `song_${Date.now()}_${randomUUID().slice(0, 8)}`;
    const uploadedFile = await uploadDriveFile({
      fileName: `${songId}-${fileName}`,
      mimeType,
      buffer: fileBuffer,
      folderId: config.google.driveFolderId,
    });

    if (!uploadedFile.id) {
      return reply.code(502).send({ success: false, error: 'Failed to upload file to Drive' });
    }

    const song: Song = {
      id: songId,
      title,
      artist,
      artist_id: slugify(artist),
      album,
      album_id: slugify(album),
      duration_secs: durationSecs,
      cover_url: 'https://placehold.co/600x600/111827/1DB954?text=Vibe+Music',
      drive_file_id: uploadedFile.id,
      genre,
      track_number: trackNumber,
      year,
      lyrics_lrc_url: undefined,
    };

    await appendSongToCatalog(song, {
      uploadedBy: uid,
      isUserUploaded: true,
      createdAt: new Date().toISOString(),
    });

    return reply.code(201).send(ok(song));
  });

  // ── Playlists ──────────────────────────────────────────────────────────────

  app.get('/api/me/playlists', { preHandler: requireAuth }, async (req, reply) => {
    const uid = getUserId(req);
    const { playlists } = getUser(uid);
    const songs = await getAllSongs();

    const list = [...playlists.values()].map(pl => ({
      id: pl.id,
      name: pl.name,
      description: pl.description,
      cover_url: pl.cover_url ?? pl.song_ids[0]
        ? songs.find(s => s.id === pl.song_ids[0])?.cover_url
        : undefined,
      owner_id: pl.owner_id,
      owner_name: pl.owner_name,
      song_count: pl.song_ids.length,
      total_duration_secs: pl.song_ids.reduce((acc, sid) => {
        const s = songs.find(x => x.id === sid);
        return acc + (s?.duration_secs ?? 0);
      }, 0),
      songs: pl.song_ids.map(sid => songs.find(s => s.id === sid)).filter(Boolean),
      created_at: pl.created_at,
    }));

    return reply.send(ok(list));
  });

  app.get<{ Params: { id: string } }>('/api/me/playlists/:id', { preHandler: requireAuth }, async (req, reply) => {
    const uid = getUserId(req);
    const { playlists } = getUser(uid);
    const pl = playlists.get(req.params.id);
    if (!pl) return reply.code(404).send({ success: false, error: 'Playlist not found' });

    const songs = await getAllSongs();
    const plSongs = pl.song_ids.map(sid => songs.find(s => s.id === sid)).filter(Boolean);

    return reply.send(ok({
      id: pl.id, name: pl.name, description: pl.description,
      cover_url: pl.cover_url, owner_id: pl.owner_id, owner_name: pl.owner_name,
      song_count: pl.song_ids.length, songs: plSongs, created_at: pl.created_at,
    }));
  });

  app.post<{ Body: { name: string; description?: string } }>(
    '/api/me/playlists', { preHandler: requireAuth },
    async (req, reply) => {
      const uid = getUserId(req);
      const { playlists } = getUser(uid);
      const { name, description } = req.body;
      if (!name?.trim()) return reply.code(400).send({ success: false, error: 'Name is required' });

      const pl: Playlist = {
        id: generateId(), name: name.trim(), description,
        owner_id: uid, owner_name: 'Me', song_ids: [], created_at: Date.now(),
      };
      playlists.set(pl.id, pl);
      return reply.code(201).send(ok({ ...pl, song_count: 0, songs: [] }));
    }
  );

  app.put<{ Params: { id: string }; Body: { name?: string; description?: string; cover_url?: string; song_ids?: string[] } }>(
    '/api/me/playlists/:id', { preHandler: requireAuth },
    async (req, reply) => {
      const uid = getUserId(req);
      const { playlists } = getUser(uid);
      const pl = playlists.get(req.params.id);
      if (!pl) return reply.code(404).send({ success: false, error: 'Playlist not found' });

      if (req.body.name)        pl.name = req.body.name;
      if (req.body.description !== undefined) pl.description = req.body.description;
      if (req.body.cover_url)   pl.cover_url = req.body.cover_url;
      if (req.body.song_ids)    pl.song_ids = req.body.song_ids;

      return reply.send(ok(pl));
    }
  );

  app.delete<{ Params: { id: string } }>(
    '/api/me/playlists/:id', { preHandler: requireAuth },
    async (req, reply) => {
      const uid = getUserId(req);
      getUser(uid).playlists.delete(req.params.id);
      return reply.send(ok(null));
    }
  );

  app.post<{ Params: { id: string }; Body: { songId: string } }>(
    '/api/me/playlists/:id/songs', { preHandler: requireAuth },
    async (req, reply) => {
      const uid = getUserId(req);
      const pl = getUser(uid).playlists.get(req.params.id);
      if (!pl) return reply.code(404).send({ success: false, error: 'Playlist not found' });
      const { songId } = req.body;
      if (!pl.song_ids.includes(songId)) pl.song_ids.push(songId);
      return reply.send(ok(null));
    }
  );

  app.delete<{ Params: { playlist_id: string; song_id: string } }>(
    '/api/me/playlists/:playlist_id/songs/:song_id', { preHandler: requireAuth },
    async (req, reply) => {
      const uid = getUserId(req);
      const pl = getUser(uid).playlists.get(req.params.playlist_id);
      if (pl) pl.song_ids = pl.song_ids.filter(id => id !== req.params.song_id);
      return reply.send(ok(null));
    }
  );

  // ── Liked Songs ────────────────────────────────────────────────────────────

  app.get('/api/me/liked', { preHandler: requireAuth }, async (req, reply) => {
    const uid = getUserId(req);
    const { likedSongs } = getUser(uid);
    const songs = await getAllSongs();
    const liked = songs.filter(s => likedSongs.has(s.id));
    return reply.send(ok(liked));
  });

  app.post<{ Params: { song_id: string } }>(
    '/api/me/liked/:song_id', { preHandler: requireAuth },
    async (req, reply) => {
      getUser(getUserId(req)).likedSongs.add(req.params.song_id);
      return reply.code(201).send(ok(null));
    }
  );

  app.delete<{ Params: { song_id: string } }>(
    '/api/me/liked/:song_id', { preHandler: requireAuth },
    async (req, reply) => {
      getUser(getUserId(req)).likedSongs.delete(req.params.song_id);
      return reply.send(ok(null));
    }
  );

  // ── Listening History ──────────────────────────────────────────────────────

  app.get('/api/me/history', { preHandler: requireAuth }, async (req, reply) => {
    const uid = getUserId(req);
    const { history } = getUser(uid);
    const songs = await getAllSongs();

    const entries = history
      .sort((a, b) => b.played_at - a.played_at)
      .slice(0, 100)
      .map(h => {
        const song = songs.find(s => s.id === h.song_id);
        return song ? { song, played_at: h.played_at, type: 'song' } : null;
      })
      .filter(Boolean);

    return reply.send(ok(entries));
  });

  app.post<{ Body: { songId: string; durationMs: number } }>(
    '/api/me/history', { preHandler: requireAuth },
    async (req, reply) => {
      const uid = getUserId(req);
      const { history } = getUser(uid);
      const { songId, durationMs } = req.body;
      history.push({ song_id: songId, played_at: Date.now(), duration_ms: durationMs });
      // Keep last 500 entries
      if (history.length > 500) history.splice(0, history.length - 500);
      return reply.code(201).send(ok(null));
    }
  );

  // ── Following Artists ──────────────────────────────────────────────────────
  const followedArtists = new Map<string, Set<string>>(); // userId -> Set<artistId>

  app.get('/api/me/following/artists', { preHandler: requireAuth }, async (req, reply) => {
    const uid = getUserId(req);
    const followed = followedArtists.get(uid) ?? new Set<string>();
    return reply.send(ok([...followed]));
  });

  app.post<{ Params: { id: string } }>(
    '/api/me/following/artists/:id', { preHandler: requireAuth },
    async (req, reply) => {
      const uid = getUserId(req);
      if (!followedArtists.has(uid)) followedArtists.set(uid, new Set());
      followedArtists.get(uid)!.add(req.params.id);
      return reply.code(201).send(ok(null));
    }
  );

  app.delete<{ Params: { id: string } }>(
    '/api/me/following/artists/:id', { preHandler: requireAuth },
    async (req, reply) => {
      followedArtists.get(getUserId(req))?.delete(req.params.id);
      return reply.send(ok(null));
    }
  );
}
