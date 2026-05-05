import { FastifyInstance } from 'fastify';
import { getDriveFileStream, getDriveFileMeta } from '../services/drive.js';
import { getAllSongs } from '../services/catalog.js';

export async function streamRoutes(app: FastifyInstance) {

  // ── GET /api/stream/:fileId ────────────────────────────────────────────────
  // Proxies the audio file from Google Drive to the client.
  // Supports HTTP Range requests for seeking.
  app.get<{ Params: { fileId: string } }>(
    '/api/stream/:fileId',
    async (req, reply) => {
      const { fileId } = req.params;

      try {
        // Get file metadata for size & mime type
        const meta = await getDriveFileMeta(fileId);
        const fileSize = parseInt(meta.size ?? '0', 10);
        const mimeType = meta.mimeType ?? 'audio/mpeg';

        const rangeHeader = req.headers.range;

        if (rangeHeader) {
          // ── Range request (seeking) ──────────────────────────────────────
          const parts = rangeHeader.replace(/bytes=/, '').split('-');
          const start = parseInt(parts[0], 10);
          const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;
          const chunkSize = end - start + 1;

          reply.code(206).headers({
            'Content-Range': `bytes ${start}-${end}/${fileSize}`,
            'Accept-Ranges': 'bytes',
            'Content-Length': String(chunkSize),
            'Content-Type': mimeType,
          });
        } else {
          // ── Full file request ────────────────────────────────────────────
          reply.headers({
            'Content-Length': String(fileSize),
            'Content-Type': mimeType,
            'Accept-Ranges': 'bytes',
          });
        }

        // Stream the file from Drive
        const stream = await getDriveFileStream(fileId);
        return reply.send(stream);

      } catch (err: any) {
        app.log.error(err, `Stream error for fileId=${fileId}`);
        return reply.code(502).send({ success: false, error: 'Failed to stream file from Drive' });
      }
    }
  );

  // ── GET /api/stream/by-song/:songId ───────────────────────────────────────
  // Convenience: look up song by ID, then redirect to /api/stream/:driveFileId
  app.get<{ Params: { songId: string } }>(
    '/api/stream/by-song/:songId',
    async (req, reply) => {
      const songs = await getAllSongs();
      const song = songs.find(s => s.id === req.params.songId);
      if (!song) return reply.code(404).send({ success: false, error: 'Song not found' });
      return reply.redirect(`/api/stream/${song.drive_file_id}`);
    }
  );
}
