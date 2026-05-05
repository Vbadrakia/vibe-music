import 'dotenv/config';

// ── Validate required env vars at startup ──────────────────────────────────────
const required = [
  'GOOGLE_SA_KEY',
  'DRIVE_FOLDER_ID',
  'SUPABASE_URL',
  'SUPABASE_SERVICE_ROLE_KEY',
  'SUPABASE_JWT_SECRET',
];

for (const key of required) {
  if (!process.env[key]) {
    throw new Error(`Missing required environment variable: ${key}`);
  }
}

export const config = {
  port: parseInt(process.env.PORT ?? '3000', 10),
  nodeEnv: process.env.NODE_ENV ?? 'development',
  isDev: (process.env.NODE_ENV ?? 'development') === 'development',

  google: {
    // Parse the SA JSON (can be raw JSON string or file path in prod)
    serviceAccountKey: JSON.parse(process.env.GOOGLE_SA_KEY!),
    driveFolderId: process.env.DRIVE_FOLDER_ID!,
  },

  supabase: {
    url: process.env.SUPABASE_URL!,
    serviceRoleKey: process.env.SUPABASE_SERVICE_ROLE_KEY!,
    jwtSecret: process.env.SUPABASE_JWT_SECRET!,
  },

  cache: {
    ttlSeconds: parseInt(process.env.CACHE_TTL_SECONDS ?? '300', 10),
  },
} as const;
