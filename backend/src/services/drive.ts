import { google } from 'googleapis';
import { config } from '../config.js';
import type { LyricLine } from '../types.js';

const auth = new google.auth.GoogleAuth({
  credentials: config.google.serviceAccountKey,
  scopes: ['https://www.googleapis.com/auth/drive.readonly'],
});

export const driveClient = google.drive({ version: 'v3', auth });

// ── Get a direct streaming URL token ──────────────────────────────────────────
export async function getDriveFileStream(fileId: string) {
  const drive = driveClient;
  const res = await drive.files.get(
    { fileId, alt: 'media' },
    { responseType: 'stream' }
  );
  return res.data;
}

// ── Get file metadata ──────────────────────────────────────────────────────────
export async function getDriveFileMeta(fileId: string) {
  const res = await driveClient.files.get({
    fileId,
    fields: 'id,name,mimeType,size',
  });
  return res.data;
}

// ── Download small text file (for .lrc lyrics) ─────────────────────────────────
export async function getDriveFileText(fileId: string): Promise<string> {
  const res = await driveClient.files.get(
    { fileId, alt: 'media' },
    { responseType: 'arraybuffer' }
  );
  return Buffer.from(res.data as ArrayBuffer).toString('utf-8');
}

// ── Parse LRC lyrics format ────────────────────────────────────────────────────
// Format: [mm:ss.xx] lyric text
export function parseLrc(lrcContent: string): LyricLine[] {
  const lines: LyricLine[] = [];
  const lineRegex = /\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)/;

  for (const raw of lrcContent.split('\n')) {
    const match = raw.trim().match(lineRegex);
    if (!match) continue;

    const minutes = parseInt(match[1], 10);
    const seconds = parseInt(match[2], 10);
    const millis = parseInt(match[3].padEnd(3, '0'), 10);
    const text = match[4].trim();

    if (text) {
      lines.push({ timeMs: minutes * 60_000 + seconds * 1_000 + millis, text });
    }
  }

  return lines.sort((a, b) => a.timeMs - b.timeMs);
}
