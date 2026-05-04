import { FastifyInstance } from 'fastify';
import { requireAuth, getUserId } from '../middleware/auth.js';
import { getAllSongs } from '../services/sheets.js';
import { ok } from '../types.js';

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

export async function userRoutes(app: FastifyInstance) {

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
