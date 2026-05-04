import { FastifyRequest, FastifyReply } from 'fastify';
import { config } from '../config.js';

// ── Supabase JWT Verification ──────────────────────────────────────────────────
// We verify the JWT manually using the Supabase JWT secret so we don't need
// the Supabase client library in the backend.

import { createDecoder, createVerifier } from 'fast-jwt';

const verify = createVerifier({ key: config.supabase.jwtSecret });

export interface AuthPayload {
  sub: string;           // user id
  email: string;
  role: string;
  aud: string;
  exp: number;
  iat: number;
}

/**
 * Fastify preHandler — requires a valid Supabase JWT in Authorization header.
 * Attaches user payload to request.user.
 */
export async function requireAuth(
  req: FastifyRequest,
  reply: FastifyReply
): Promise<void> {
  const authHeader = req.headers['authorization'];
  if (!authHeader?.startsWith('Bearer ')) {
    return reply.code(401).send({ success: false, error: 'Missing auth token' });
  }

  const token = authHeader.slice(7);
  try {
    const payload = verify(token) as AuthPayload;
    (req as any).user = payload;
  } catch (e) {
    return reply.code(401).send({ success: false, error: 'Invalid or expired token' });
  }
}

/**
 * Optional auth — attaches user if token present, continues if not.
 */
export async function optionalAuth(
  req: FastifyRequest,
  _reply: FastifyReply
): Promise<void> {
  const authHeader = req.headers['authorization'];
  if (!authHeader?.startsWith('Bearer ')) return;
  try {
    const payload = verify(authHeader.slice(7)) as AuthPayload;
    (req as any).user = payload;
  } catch {}
}

export function getUserId(req: FastifyRequest): string {
  return ((req as any).user as AuthPayload).sub;
}
