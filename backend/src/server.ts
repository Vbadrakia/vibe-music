import 'dotenv/config';
import Fastify from 'fastify';
import cors from '@fastify/cors';
import multipart from '@fastify/multipart';
import rateLimit from '@fastify/rate-limit';
import { config } from './config.js';
import { catalogRoutes } from './routes/catalog.js';
import { streamRoutes } from './routes/stream.js';
import { lyricsRoutes } from './routes/lyrics.js';
import { userRoutes } from './routes/user.js';

const app = Fastify({
  logger: {
    transport: config.isDev
      ? { target: 'pino-pretty', options: { colorize: true } }
      : undefined,
    level: config.isDev ? 'debug' : 'info',
  },
});

const start = async () => {
// ── Plugins ────────────────────────────────────────────────────────────────────
  await app.register(cors, {
    origin: true, // Allow all in dev — restrict to your app domain in prod
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  });

  await app.register(multipart, {
    limits: {
      fileSize: 50 * 1024 * 1024,
      files: 1,
    },
  });

  await app.register(rateLimit, {
    max: 300,
    timeWindow: '1 minute',
  });

// ── Health check ───────────────────────────────────────────────────────────────
  app.get('/health', async () => ({
    status: 'ok',
    timestamp: new Date().toISOString(),
    version: '1.0.0',
  }));

// ── Routes ─────────────────────────────────────────────────────────────────────
  await app.register(catalogRoutes);
  await app.register(streamRoutes);
  await app.register(lyricsRoutes);
  await app.register(userRoutes);

// ── Global error handler ───────────────────────────────────────────────────────
  app.setErrorHandler((error, _req, reply) => {
    app.log.error(error);
    const statusCode = error.statusCode ?? 500;
    reply.code(statusCode).send({
      success: false,
      error: statusCode === 500 ? 'Internal Server Error' : error.message,
    });
  });

// ── 404 handler ────────────────────────────────────────────────────────────────
  app.setNotFoundHandler((_req, reply) => {
    reply.code(404).send({ success: false, error: 'Route not found' });
  });

// ── Start ──────────────────────────────────────────────────────────────────────
  try {
    await app.listen({ port: config.port, host: '0.0.0.0' });
    app.log.info(`🎵 Vibe backend running on port ${config.port}`);
  } catch (err) {
    app.log.error(err);
    process.exit(1);
  }
};

start();

export default app;
