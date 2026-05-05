import { FastifyInstance } from 'fastify';
import {
  getAllSongs,
  getAllAlbums,
  getAllArtists,
  getAllGenres,
} from '../services/catalog.js';
import { ok } from '../types.js';
import type { Song, Album, Artist } from '../types.js';

export async function catalogRoutes(app: FastifyInstance) {

  // ── GET /api/songs ─────────────────────────────────────────────────────────
  app.get('/api/songs', {
    schema: {
      querystring: {
        type: 'object',
        properties: {
          genre:     { type: 'string' },
          artist_id: { type: 'string' },
          album_id:  { type: 'string' },
          q:         { type: 'string' },
          limit:     { type: 'integer', default: 50 },
          offset:    { type: 'integer', default: 0 },
        },
      },
    },
  }, async (req, reply) => {
    const { genre, artist_id, album_id, q, limit = 50, offset = 0 } =
      req.query as any;

    let songs = await getAllSongs();

    if (genre)     songs = songs.filter(s => s.genre?.toLowerCase() === genre.toLowerCase());
    if (artist_id) songs = songs.filter(s => s.artist_id === artist_id);
    if (album_id)  songs = songs.filter(s => s.album_id  === album_id);
    if (q) {
      const lq = q.toLowerCase();
      songs = songs.filter(s =>
        s.title.toLowerCase().includes(lq) ||
        s.artist.toLowerCase().includes(lq)
      );
    }

    const paginated = songs.slice(offset, offset + limit);
    return reply.send(ok(paginated));
  });

  // ── GET /api/songs/:id ─────────────────────────────────────────────────────
  app.get<{ Params: { id: string } }>('/api/songs/:id', async (req, reply) => {
    const songs = await getAllSongs();
    const song = songs.find(s => s.id === req.params.id);
    if (!song) return reply.code(404).send({ success: false, error: 'Song not found' });
    return reply.send(ok(song));
  });

  // ── GET /api/albums ────────────────────────────────────────────────────────
  app.get('/api/albums', {
    schema: {
      querystring: {
        type: 'object',
        properties: {
          artist_id: { type: 'string' },
          limit: { type: 'integer', default: 20 },
        },
      },
    },
  }, async (req, reply) => {
    const { artist_id, limit = 20 } = req.query as any;
    let albums = await getAllAlbums();
    if (artist_id) albums = albums.filter(a => a.artist_id === artist_id);
    return reply.send(ok(albums.slice(0, limit)));
  });

  // ── GET /api/albums/:id ────────────────────────────────────────────────────
  app.get<{ Params: { id: string } }>('/api/albums/:id', async (req, reply) => {
    const [albums, songs] = await Promise.all([getAllAlbums(), getAllSongs()]);
    const album = albums.find(a => a.id === req.params.id);
    if (!album) return reply.code(404).send({ success: false, error: 'Album not found' });
    const albumSongs = songs
      .filter(s => s.album_id === album.id)
      .sort((a, b) => (a.track_number ?? 999) - (b.track_number ?? 999));
    return reply.send(ok({ ...album, songs: albumSongs }));
  });

  // ── GET /api/artists ───────────────────────────────────────────────────────
  app.get('/api/artists', {
    schema: {
      querystring: {
        type: 'object',
        properties: {
          q:     { type: 'string' },
          limit: { type: 'integer', default: 20 },
        },
      },
    },
  }, async (req, reply) => {
    const { q, limit = 20 } = req.query as any;
    let artists = await getAllArtists();
    if (q) {
      const lq = q.toLowerCase();
      artists = artists.filter(a => a.name.toLowerCase().includes(lq));
    }
    return reply.send(ok(artists.slice(0, limit)));
  });

  // ── GET /api/artists/:id ───────────────────────────────────────────────────
  app.get<{ Params: { id: string } }>('/api/artists/:id', async (req, reply) => {
    const artists = await getAllArtists();
    const artist = artists.find(a => a.id === req.params.id);
    if (!artist) return reply.code(404).send({ success: false, error: 'Artist not found' });
    return reply.send(ok(artist));
  });

  // ── GET /api/genres ────────────────────────────────────────────────────────
  app.get('/api/genres', async (_req, reply) => {
    const genres = await getAllGenres();
    return reply.send(ok(genres));
  });

  // ── GET /api/genres/:id/songs ──────────────────────────────────────────────
  app.get<{ Params: { id: string } }>('/api/genres/:id/songs', async (req, reply) => {
    const songs = await getAllSongs();
    const filtered = songs.filter(
      s => s.genre?.toLowerCase().replace(/\s/g, '-') === req.params.id
    );
    return reply.send(ok(filtered));
  });

  // ── GET /api/search ────────────────────────────────────────────────────────
  app.get('/api/search', {
    schema: { querystring: { type: 'object', properties: { q: { type: 'string' } }, required: ['q'] } },
  }, async (req, reply) => {
    const { q } = req.query as { q: string };
    const lq = q.toLowerCase();

    const [allSongs, allAlbums, allArtists] = await Promise.all([
      getAllSongs(), getAllAlbums(), getAllArtists(),
    ]);

    const songs   = allSongs.filter(s => s.title.toLowerCase().includes(lq) || s.artist.toLowerCase().includes(lq));
    const albums  = allAlbums.filter(a => a.title.toLowerCase().includes(lq) || a.artist.toLowerCase().includes(lq));
    const artists = allArtists.filter(a => a.name.toLowerCase().includes(lq));

    const topResult: Song | null = songs[0] ?? null;

    return reply.send(ok({ topResult, songs, albums, artists }));
  });

  // ── GET /api/home ──────────────────────────────────────────────────────────
  app.get('/api/home', async (_req, reply) => {
    const [songs, albums] = await Promise.all([getAllSongs(), getAllAlbums()]);

    // Shuffle a subset for "recently played" (demo — real version uses user history)
    const shuffled = [...songs].sort(() => Math.random() - 0.5);

    const homeData = {
      recentlyPlayed: shuffled.slice(0, 8),
      madeForYou: [
        {
          id: 'discover-weekly',
          name: 'Discover Weekly',
          description: 'Your weekly mixtape of fresh music.',
          coverUrl: songs[0]?.cover_url ?? '',
          songCount: songs.length,
        },
        {
          id: 'daily-mix-1',
          name: 'Daily Mix 1',
          description: 'Made for you.',
          coverUrl: songs[1]?.cover_url ?? '',
          songCount: 30,
        },
      ],
      newReleases: albums.sort((a, b) => b.year - a.year).slice(0, 6),
      featuredPlaylists: [
        {
          id: 'top-hits',
          name: 'Top Hits',
          description: "Today's biggest tracks.",
          coverUrl: songs[2]?.cover_url ?? '',
          songCount: songs.length,
        },
      ],
    };

    return reply.send(ok(homeData));
  });
}
