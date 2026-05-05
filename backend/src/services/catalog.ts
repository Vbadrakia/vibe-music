import { config } from '../config.js';
import { driveClient } from './drive.js';
import type { Song, Album, Artist, Genre } from '../types.js';

let cachedSongs: Song[] | null = null;
let lastCacheTime = 0;
const CACHE_TTL = 5 * 60 * 1000; // 5 minutes

function slugify(value: string) {
  const slug = value
    .normalize('NFKD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '');
  return slug || 'unknown';
}

function filenameWithoutExtension(filename: string) {
  return filename.replace(/\.[^.]+$/, '');
}

export async function clearCatalogCache() {
  cachedSongs = null;
}

export async function getAllSongs(): Promise<Song[]> {
  if (cachedSongs && Date.now() - lastCacheTime < CACHE_TTL) {
    return cachedSongs;
  }

  const drive = driveClient;
  const songs: Song[] = [];
  let pageToken: string | undefined = undefined;

  try {
    do {
      const res = await drive.files.list({
        q: `'${config.google.driveFolderId}' in parents and trashed = false and mimeType contains 'audio/'`,
        fields: 'nextPageToken, files(id, name, appProperties)',
        pageToken,
        pageSize: 1000,
      });

      const files = res.data.files ?? [];
      for (const file of files) {
        if (!file.id) continue;
        
        const props = file.appProperties ?? {};
        
        // Use appProperties if available, otherwise fallback to filename
        const title = props.title || filenameWithoutExtension(file.name || 'Unknown Title');
        const artist = props.artist || 'Unknown Artist';
        const album = props.album || 'Single';
        
        songs.push({
          id: props.id || `song_${file.id}`,
          title,
          artist,
          artist_id: slugify(artist),
          album,
          album_id: slugify(album),
          duration_secs: parseInt(props.duration_secs || '0', 10) || 180, // Default 3 mins if unknown
          cover_url: props.cover_url || 'https://placehold.co/600x600/111827/1DB954?text=Vibe+Music',
          drive_file_id: file.id,
          genre: props.genre,
          track_number: props.track_number ? parseInt(props.track_number, 10) : undefined,
          year: props.year ? parseInt(props.year, 10) : new Date().getFullYear(),
        });
      }

      pageToken = res.data.nextPageToken || undefined;
    } while (pageToken);

    cachedSongs = songs;
    lastCacheTime = Date.now();
    return songs;
  } catch (err) {
    console.error('Error fetching songs from Drive:', err);
    return cachedSongs ?? []; // return stale cache or empty
  }
}

export async function getAllAlbums(): Promise<Album[]> {
  const songs = await getAllSongs();
  const albumsMap = new Map<string, Album>();

  for (const song of songs) {
    if (!albumsMap.has(song.album_id)) {
      albumsMap.set(song.album_id, {
        id: song.album_id,
        title: song.album,
        artist_id: song.artist_id,
        artist: song.artist,
        year: song.year ?? new Date().getFullYear(),
        cover_url: song.cover_url,
        song_count: 0,
        total_duration_secs: 0,
      });
    }
    
    const album = albumsMap.get(song.album_id)!;
    album.song_count += 1;
    album.total_duration_secs += song.duration_secs;
  }

  return Array.from(albumsMap.values());
}

export async function getAllArtists(): Promise<Artist[]> {
  const songs = await getAllSongs();
  const artistsMap = new Map<string, Artist>();

  for (const song of songs) {
    if (!artistsMap.has(song.artist_id)) {
      artistsMap.set(song.artist_id, {
        id: song.artist_id,
        name: song.artist,
        cover_url: song.cover_url,
        monthly_listeners: Math.floor(Math.random() * 1000000) + 50000, // mock data
        is_verified: true,
        genres: [],
      });
    }
    
    const artist = artistsMap.get(song.artist_id)!;
    if (song.genre && !artist.genres!.includes(song.genre)) {
      artist.genres!.push(song.genre);
    }
  }

  return Array.from(artistsMap.values());
}

export async function getAllGenres(): Promise<Genre[]> {
  const songs = await getAllSongs();
  const genresMap = new Map<string, Genre>();

  const COLORS = [
    '#E13300', '#7358FF', '#1DB954', '#E8115B', '#148A08', '#BC5900', '#E91429', '#8D67AB'
  ];

  let colorIdx = 0;
  for (const song of songs) {
    if (!song.genre) continue;
    
    const genreId = slugify(song.genre);
    if (!genresMap.has(genreId)) {
      genresMap.set(genreId, {
        id: genreId,
        name: song.genre,
        description: `${song.genre} Music`,
        cover_url: song.cover_url,
        color: COLORS[colorIdx % COLORS.length]!,
      });
      colorIdx++;
    }
  }

  return Array.from(genresMap.values());
}

// Function to store song metadata onto the drive file's appProperties
export async function appendSongToCatalog(song: Song, extraData?: any) {
  const drive = driveClient;
  
  // We just update the appProperties of the existing Drive file
  const props: Record<string, string> = {
    id: song.id,
    title: song.title,
    artist: song.artist,
    album: song.album,
    duration_secs: String(song.duration_secs),
    cover_url: song.cover_url,
  };
  
  if (song.genre) props.genre = song.genre;
  if (song.track_number) props.track_number = String(song.track_number);
  if (song.year) props.year = String(song.year);
  if (extraData?.uploadedBy) props.uploadedBy = String(extraData.uploadedBy);

  try {
    await drive.files.update({
      fileId: song.drive_file_id,
      requestBody: {
        appProperties: props
      }
    });
    // Invalidate cache since we added a new song
    await clearCatalogCache();
  } catch (err) {
    console.error('Failed to update Drive appProperties for catalog', err);
  }
}
