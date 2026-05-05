const https = require('https');
const crypto = require('crypto');

const SA_KEY = {
  client_email: 'vibe-backend@vibe-music-495120.iam.gserviceaccount.com',
  private_key: '-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCkBoSIMYpsuO1k\nVim/9GuU2lZgxcwStvn/Dl8I0VN1NyY2MMEFjpX4N1sfhokXCOPHxZVPulSchV3g\nRT/iTXZOuKfTkQ2qxON/VP44YBkNY1WflCvJuoPwPu4sjCyyWQ2xpqOoj9WfY+xn\n0qxQoLAd2+jnDmJxQM2Wz9zNXX/I5GK6huhxQ7bDyC53nRbx+MFS0sZT+7nu9p2w\nyzgUfBTLoaa2GL7U1yDoCmQzM81oczQ3XXORyb8XD7Fax/aKvLNZhmZA5MH8gBcO\nMDv5yIANUE4Vbrx93qnDVR+Jo5xFkCw3WJB2j5regieNbqoayfzmtqqSdlYIFdkT\n/MOUkAkXAgMBAAECggEACZAQYaj9vdlJjHwngqJF3rpHUeyw4/XDUnbshyTpEuA3\nEUQQuiRgzZqwlysZSqdWhvrtJGZOcNz8/EpymdzT78Kn6izpQ+enBHFDQax8mWwN\nUw3o6vcS98qQyTZs6VhJdO4sWXNKoY3KURfruub7YupDbwEcL9J231Auunxf1amU\ndn9Eyk+pSICMEUv+aRpIFkJFPD4QUx6+5iDy83NvVgPakS12+3LKtTB1jxBWBOy5\nlBEUz2nWcSVchGBv9tsHhuqpTR5rw4SQvcoMKjXAiojQyTTFx/UhJTxVTaXbss5b\n2EPZ2kJp89imXTNjbBMX3iOofTgZjltuKL1hZwvDeQKBgQDNDh9X4r7+KkGzep8O\ngNQSr1p5/WOSlpurIg0nbkJHmGF5TfXBUt8diIzMIsExVxVJ2N8smEzCbxBiBF0/\ngz0P1BP+0RNqTkfZVkjbc53LGbG/dh9NbROrdzOY/VQ7a7kPl4bamJHTp+fBl93p\nHDZiH8dXKIUxc4aji1TZETb+TwKBgQDMxtUGc3EsO451xcpceioV3LiCCezQ9Q4P\n/ZseXnEXfEToF5DT7ZwqowzNqeBGEM2mma2TvF+SbVcA399ll5iVCp8LsYrGoJXT\nAazMgF2ZhJBA1SRJN4aLekvP04KfQzqqUsQXFA30tkFzjy6Zp/cpMdVX2BjSlcQu\n/WQ5aWseuQKBgBP/5J61xtsTVAUlWI4ZkpWf9LshM60Ac35le9L0thI1kow6RlXs\nt0YyI0llumINlE33kQzQbewo2Pg2ZMHlEveQP8MCsVoU+H0CJWkCBcnhX6zE4Ji0\nos5+edHaI3UINKyIouZ/KvnxznDbVxF9ZvB7GP4vPY5tLhyVYlkpjKuRAoGBALtq\nmxtdWs4d8AOQM+YxlQUPTvsNNGYGcolmFatUynKxNKuqrc2ZIZ3QPu6YNIBL7rw0\n2K1m8Z2lXsSZIO/tMCPwZaIhKx8haB6H4OwF7CSONCxyJtSv8f8DvhEGxv0WXcH3\nICrzGtbGW970w7S0Crz0NAtC520Mz1Imba0x821ZAoGAJcsVbl0AiDLAe91xDZJj\nvVBZbulPVR8+60Dhe1x6P6LYfpq6gbpltUWB8hts7ENLOXyJ4HbirATqt95nlUUr\nJ9GmPlV232qUCF5GLnwdj7e5BmrTyHZmmY4OkalmrQUDtIbDfsUHJWVpvzVnOv9c\nP2dyTEOWcjdWshPezsD4ZB0=\n-----END PRIVATE KEY-----\n'
};
const SHEET_ID = '19ZxwzxdrDOKo6utRLvbEFjZ2qcBo-ysLIH4SwkobfQE';

function base64url(buf) {
  return buf.toString('base64').replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
}

function makeJWT() {
  const now = Math.floor(Date.now() / 1000);
  const header = base64url(Buffer.from(JSON.stringify({ alg: 'RS256', typ: 'JWT' })));
  const payload = base64url(Buffer.from(JSON.stringify({
    iss: SA_KEY.client_email,
    scope: 'https://www.googleapis.com/auth/spreadsheets.readonly',
    aud: 'https://oauth2.googleapis.com/token',
    exp: now + 3600,
    iat: now
  })));
  const sign = crypto.createSign('RSA-SHA256');
  sign.update(header + '.' + payload);
  const sig = base64url(sign.sign(SA_KEY.private_key));
  return header + '.' + payload + '.' + sig;
}

function httpPost(url, data) {
  return new Promise(function(res, rej) {
    const body = Buffer.from(data);
    const opts = new URL(url);
    const req = https.request({
      hostname: opts.hostname,
      path: opts.pathname + opts.search,
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Content-Length': body.length }
    }, function(r) {
      let d = '';
      r.on('data', function(c) { d += c; });
      r.on('end', function() { res(JSON.parse(d)); });
    });
    req.on('error', rej);
    req.write(body);
    req.end();
  });
}

function httpGet(url, token) {
  return new Promise(function(res, rej) {
    const opts = new URL(url);
    const req = https.request({
      hostname: opts.hostname,
      path: opts.pathname + opts.search,
      method: 'GET',
      headers: { Authorization: 'Bearer ' + token }
    }, function(r) {
      let d = '';
      r.on('data', function(c) { d += c; });
      r.on('end', function() { res(JSON.parse(d)); });
    });
    req.on('error', rej);
    req.end();
  });
}

async function main() {
  const jwt = makeJWT();
  const tokenRes = await httpPost(
    'https://oauth2.googleapis.com/token',
    'grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=' + jwt
  );

  if (!tokenRes.access_token) {
    console.error('Token error:', JSON.stringify(tokenRes));
    return;
  }

  // Get sheet names
  const meta = await httpGet('https://sheets.googleapis.com/v4/spreadsheets/' + SHEET_ID, tokenRes.access_token);
  const sheetNames = meta.sheets ? meta.sheets.map(function(s) { return s.properties.title; }) : [];
  console.log('AVAILABLE SHEETS:', JSON.stringify(sheetNames));

  for (const sheetName of sheetNames) {
    const data = await httpGet(
      'https://sheets.googleapis.com/v4/spreadsheets/' + SHEET_ID + '/values/' + encodeURIComponent(sheetName) + '!A:Z',
      tokenRes.access_token
    );

    if (!data.values || data.values.length === 0) {
      console.log('Sheet "' + sheetName + '" is empty');
      continue;
    }

    const headers = data.values[0];
    const rows = data.values.slice(1);
    console.log('\n=== Sheet: ' + sheetName + ' ===');
    console.log('Headers:', JSON.stringify(headers));
    console.log('Total rows:', rows.length);
    rows.forEach(function(row, i) {
      const obj = {};
      headers.forEach(function(h, j) {
        if (row[j] !== undefined && row[j] !== '') obj[h] = row[j];
      });
      console.log((i + 1) + '. ' + JSON.stringify(obj));
    });
  }
}

main().catch(console.error);
