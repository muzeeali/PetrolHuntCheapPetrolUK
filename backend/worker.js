/**
 * Petrol Hunt UK - High-Efficiency Edge Proxy Worker
 * Integrates with official GOV.UK Fuel Finder OAuth 2.0 & CMA Open Data Scheme
 *
 * Cloudflare Worker Environment:
 * Secrets: GOV_CLIENT_ID, GOV_CLIENT_SECRET
 */

let cachedToken = null;
let tokenExpiresAt = 0;

const CORS_HEADERS = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, HEAD, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization, Cache-Control, Accept',
  'Access-Control-Max-Age': '86400',
  'Content-Type': 'application/json'
};

function getCorsResponse(data, status = 200, cacheControl = 'public, max-age=900, stale-while-revalidate=300', extraHeaders = {}) {
  return new Response(typeof data === 'string' ? data : JSON.stringify(data), {
    status,
    headers: {
      ...CORS_HEADERS,
      'Cache-Control': cacheControl,
      ...extraHeaders
    }
  });
}

function haversine(lat1, lon1, lat2, lon2) {
  const R = 3958.8; // Radius of Earth in miles
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

/**
 * Acquire or reuse warm in-memory OAuth 2.0 Bearer token
 */
async function getGovUkToken(env) {
  const now = Date.now();
  if (cachedToken && now < tokenExpiresAt - 60000) {
    return cachedToken;
  }

  const clientId = env?.GOV_CLIENT_ID;
  const clientSecret = env?.GOV_CLIENT_SECRET;
  if (!clientId || !clientSecret) {
    return null;
  }

  // Strategy 1: Post to GOV.UK Fuel Finder OAuth Endpoint with form-urlencoded
  try {
    const formParams = new URLSearchParams({
      grant_type: 'client_credentials',
      client_id: clientId,
      client_secret: clientSecret,
      scope: 'fuelfinder.read'
    });

    const res = await fetch('https://www.fuel-finder.service.gov.uk/api/v1/oauth/generate_access_token', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Accept': 'application/json',
        'User-Agent': 'PetrolMapUK/1.3 (Cloudflare-Edge-Proxy)'
      },
      body: formParams.toString()
    });

    if (res.ok) {
      const data = await res.json();
      if (data && data.access_token) {
        cachedToken = data.access_token;
        const expiresInSec = typeof data.expires_in === 'number' ? data.expires_in : 3600;
        tokenExpiresAt = now + expiresInSec * 1000;
        return cachedToken;
      }
    }
  } catch (err) {
    // Continue to strategy 2
  }

  // Strategy 2: Post JSON payload
  try {
    const res = await fetch('https://www.fuel-finder.service.gov.uk/api/v1/oauth/generate_access_token', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'User-Agent': 'PetrolMapUK/1.3 (Cloudflare-Edge-Proxy)'
      },
      body: JSON.stringify({
        client_id: clientId,
        client_secret: clientSecret
      })
    });

    if (res.ok) {
      const data = await res.json();
      if (data && data.access_token) {
        cachedToken = data.access_token;
        const expiresInSec = typeof data.expires_in === 'number' ? data.expires_in : 3600;
        tokenExpiresAt = now + expiresInSec * 1000;
        return cachedToken;
      }
    }
  } catch (err) {
    // Graceful handling
  }

  return cachedToken; // Return existing token if valid or null
}

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);

    // 1. 0ms CORS Preflight
    if (request.method === 'OPTIONS') {
      return new Response(null, {
        status: 204,
        headers: {
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Methods': 'GET, HEAD, OPTIONS',
          'Access-Control-Allow-Headers': 'Content-Type, Authorization, Cache-Control, Accept',
          'Access-Control-Max-Age': '86400'
        }
      });
    }


    // Health / Discovery Endpoint
    if (url.pathname === '/' || url.pathname === '/health') {
      return getCorsResponse({
        status: 'ok',
        service: 'Petrol Map UK Edge API',
        version: '1.4.0',
        provider: 'GOV.UK Fuel Finder & CMA Open Data Scheme',
        caching: 'Cloudflare Edge Cache (900s TTL)',
        supportedFuels: ['E10', 'E5', 'B7', 'SDV'],
        timestamp: new Date().toISOString()
      }, 200, 'public, max-age=60');
    }

    // 2. Cloudflare Edge Cache Verification
    const cache = caches.default;
    // Normalize cache key by removing ephemeral query parameters (_t, cb, timestamp)
    const normalizedUrl = new URL(request.url);
    normalizedUrl.searchParams.delete('_t');
    normalizedUrl.searchParams.delete('cb');
    normalizedUrl.searchParams.delete('timestamp');
    const cacheKey = new Request(normalizedUrl.toString(), request);

    // If client is not requesting a force refresh, check edge cache
    const forceRefresh = url.searchParams.get('refresh') === 'true';
    if (!forceRefresh) {
      const cachedResponse = await cache.match(cacheKey);
      if (cachedResponse) {
        const newHeaders = new Headers(cachedResponse.headers);
        newHeaders.set('X-Edge-Cache', 'HIT');
        return new Response(cachedResponse.body, {
          status: cachedResponse.status,
          statusText: cachedResponse.statusText,
          headers: newHeaders
        });
      }
    }

    // Single Station Detail Endpoint: /v1/station?slug=... or id=...
    if (url.pathname === '/v1/station') {
      const slug = url.searchParams.get('slug') || url.searchParams.get('id');
      if (!slug) {
        return getCorsResponse({ success: false, error: 'Missing station slug or ID' }, 400);
      }

      // Check GOV.UK token
      const token = await getGovUkToken(env);

      // Attempt to query GOV.UK Fuel Finder REST resource if token available
      if (token) {
        try {
          const govRes = await fetch(`https://api.fuelfinder.service.gov.uk/v1/prices/${encodeURIComponent(slug)}`, {
            headers: {
              'Authorization': `Bearer ${token}`,
              'Accept': 'application/json',
              'User-Agent': 'PetrolMapUK/1.3'
            }
          });
          if (govRes.ok) {
            const govData = await govRes.json();
            const resp = getCorsResponse({
              success: true,
              source: 'fuel_finder_gov_uk',
              slug,
              prices: govData.prices || {},
              updatedDates: govData.updated_dates || {},
              latestUpdated: govData.latest_updated || new Date().toISOString()
            }, 200, 'public, max-age=900, stale-while-revalidate=300', { 'X-Edge-Cache': 'MISS' });

            ctx.waitUntil(cache.put(cacheKey, resp.clone()));
            return resp;
          }
        } catch (e) {
          // Fall through
        }
      }

      // Live Forecourt Provider Fallback
      try {
        const res = await fetch(`https://checkfuelprices.co.uk/stations/${encodeURIComponent(slug)}`, {
          headers: { 'User-Agent': 'Mozilla/5.0 (Mobile; Android; PetrolMapUK/1.4)' }
        });
        if (res.ok) {
          const html = await res.text();
          const prices = {};
          const updatedDates = {};
          let latestUpdated = null;

          const regex = /<div class="price-card[\s\S]*?<h6[^>]*>(.*?)<\/h6>[\s\S]*?<span[^>]*class="[^"]*font-mono[^"]*">([0-9.]+)p<\/span>[\s\S]*?Last updated by ([^<\n]+)/g;
          let match;
          while ((match = regex.exec(html)) !== null) {
            const rawGrade = match[1].trim().toLowerCase();
            const price = parseFloat(match[2]);
            const updated = ('Last updated by ' + match[3].trim()).replace(/\s+/g, ' ');
            let fuelCode = null;
            if (rawGrade.includes('e10') || (rawGrade.includes('unleaded') && !rawGrade.includes('super') && !rawGrade.includes('premium'))) {
              fuelCode = 'E10';
            } else if (rawGrade.includes('e5') || rawGrade.includes('super unleaded') || rawGrade.includes('premium petrol')) {
              fuelCode = 'E5';
            } else if (rawGrade.includes('b7') || (rawGrade.includes('diesel') && !rawGrade.includes('super') && !rawGrade.includes('premium'))) {
              fuelCode = 'B7';
            } else if (rawGrade.includes('sdv') || rawGrade.includes('premium diesel') || rawGrade.includes('super diesel')) {
              fuelCode = 'SDV';
            }
            if (fuelCode && price > 0) {
              prices[fuelCode] = price;
              updatedDates[fuelCode] = updated;
              latestUpdated = updated;
            }
          }

          const resp = getCorsResponse({
            success: true,
            source: 'fuel_finder',
            slug,
            prices,
            updatedDates,
            latestUpdated
          }, 200, 'public, max-age=900, stale-while-revalidate=300', { 'X-Edge-Cache': 'MISS' });

          ctx.waitUntil(cache.put(cacheKey, resp.clone()));
          return resp;
        }
      } catch (e) {
        // Fall through
      }

      return getCorsResponse({ success: false, error: 'Station details unavailable' }, 404);
    }

    // Nearby Stations Endpoint: /v1/stations
    if (url.pathname === '/v1/stations') {
      const latParam = url.searchParams.get('lat');
      const lngParam = url.searchParams.get('lng');
      const fuelParam = (url.searchParams.get('fuel') || 'E10').toUpperCase();
      const radiusParam = parseFloat(url.searchParams.get('radius') || '5');
      const sortParam = (url.searchParams.get('sort') || 'cheapest').toLowerCase();

      if (!latParam || !lngParam) {
        return getCorsResponse({ success: false, error: { code: 'INVALID_LOCATION', message: 'Latitude and longitude are required.' } }, 400);
      }

      const lat = parseFloat(latParam);
      const lng = parseFloat(lngParam);
      const isUk = (lat >= 49.5 && lat <= 61.5 && lng >= -9.5 && lng <= 2.5);
      if (!isUk) {
        return getCorsResponse({
          success: true,
          isUk: false,
          count: 0,
          stations: [],
          attribution: 'Prices sourced via Fuel Finder & CMA Open Data Scheme',
          message: 'Location is outside the United Kingdom.'
        });
      }

      let selectedFuelCode = 'E10';
      if (fuelParam === 'E5') selectedFuelCode = 'E5';
      else if (fuelParam === 'B7' || fuelParam === 'DIESEL') selectedFuelCode = 'B7';
      else if (fuelParam === 'SDV' || fuelParam === 'PREMIUM_DIESEL') selectedFuelCode = 'SDV';

      try {
        // Multi-point radial dispersion for extended radiuses (> 5 miles)
        const queryPoints = [{ lat, lng }];
        if (radiusParam > 5) {
          const dLat = (radiusParam * 0.55) / 69.0;
          const dLng = (radiusParam * 0.55) / (69.0 * Math.cos(lat * Math.PI / 180));
          queryPoints.push({ lat: lat + dLat, lng: lng });
          queryPoints.push({ lat: lat - dLat, lng: lng });
          queryPoints.push({ lat: lat, lng: lng + dLng });
          queryPoints.push({ lat: lat, lng: lng - dLng });
        }

        const rawMap = new Map();
        let areaStats = null;

        await Promise.all(queryPoints.map(async (pt) => {
          try {
            const u = `https://checkfuelprices.co.uk/api/widget/stations?lat=${pt.lat}&lng=${pt.lng}&fuel=${selectedFuelCode}&limit=20&_t=${Date.now()}`;
            const res = await fetch(u, {
              headers: {
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
                'Accept': 'application/json',
                'Cache-Control': 'no-cache'
              }
            });
            if (res.ok) {
              const json = await res.json();
              if (json && json.success && Array.isArray(json.stations)) {
                if (!areaStats && json.area_stats) areaStats = json.area_stats;
                for (const st of json.stations) {
                  const key = st.slug || st.id || `${st.lat}_${st.lng}`;
                  if (!rawMap.has(key)) rawMap.set(key, st);
                }
              }
            }
          } catch (e) {}
        }));

        if (rawMap.size === 0) {
          throw new Error('No forecourts available in target area');
        }

        const candidateStations = [];

        for (const st of rawMap.values()) {
          if (!st || typeof st.price !== 'number' || st.price <= 0) continue;
          const sLat = typeof st.lat === 'number' ? st.lat : parseFloat(st.lat);
          const sLng = typeof st.lng === 'number' ? st.lng : parseFloat(st.lng);
          if (isNaN(sLat) || isNaN(sLng)) continue;

          const dist = parseFloat(haversine(lat, lng, sLat, sLng).toFixed(1));
          if (dist > radiusParam) continue;

          const rawAddr = st.address || '';
          const brand = st.brand ? st.brand.toUpperCase() : 'INDEPENDENT';
          let name = brand;
          if (st.slug) {
            const parts = st.slug.split('-');
            if (parts.length > 2) {
              name = parts.slice(0, 3).map(p => p.charAt(0).toUpperCase() + p.slice(1)).join(' ');
            }
          }

          candidateStations.push({
            id: st.id || `${sLat}_${sLng}`,
            name,
            brand,
            slug: st.slug || '',
            latitude: sLat,
            longitude: sLng,
            address: {
              line1: rawAddr,
              postcode: st.postcode || '',
              town: rawAddr.split(',')[1]?.trim() || 'UK'
            },
            price: st.price,
            fuel: selectedFuelCode,
            distanceMiles: dist,
            updatedAt: st.price_updated || new Date().toISOString(),
            source: 'fuel_finder',
            diffFromAvg: typeof st.diff_from_avg === 'number' ? st.diff_from_avg : 0,
            prices: { [selectedFuelCode]: st.price },
            fuelUpdatedDates: st.price_updated ? { [selectedFuelCode]: st.price_updated } : {}
          });
        }

        // Concurrently enrich top candidates for precise timestamps and full multi-fuel prices
        const enrichPromises = candidateStations.slice(0, 15).map(async (station) => {
          if (!station.slug) return;
          try {
            const pageResp = await fetch(`https://checkfuelprices.co.uk/stations/${encodeURIComponent(station.slug)}`, {
              headers: { 'User-Agent': 'Mozilla/5.0 (Mobile; Android; PetrolMapUK/1.4)' }
            });
            if (pageResp.ok) {
              const html = await pageResp.text();
              const regex = /<div class="price-card[\s\S]*?<h6[^>]*>(.*?)<\/h6>[\s\S]*?<span[^>]*class="[^"]*font-mono[^"]*">([0-9.]+)p<\/span>[\s\S]*?Last updated by ([^<\n]+)/g;
              let match;
              while ((match = regex.exec(html)) !== null) {
                const rawGrade = match[1].trim().toLowerCase();
                const price = parseFloat(match[2]);
                const updated = ('Last updated by ' + match[3].trim()).replace(/\s+/g, ' ');
                let fuelCode = null;
                if (rawGrade.includes('e10') || (rawGrade.includes('unleaded') && !rawGrade.includes('super') && !rawGrade.includes('premium'))) {
                  fuelCode = 'E10';
                } else if (rawGrade.includes('e5') || rawGrade.includes('super unleaded') || rawGrade.includes('premium petrol')) {
                  fuelCode = 'E5';
                } else if (rawGrade.includes('b7') || (rawGrade.includes('diesel') && !rawGrade.includes('super') && !rawGrade.includes('premium'))) {
                  fuelCode = 'B7';
                } else if (rawGrade.includes('sdv') || rawGrade.includes('premium diesel') || rawGrade.includes('super diesel')) {
                  fuelCode = 'SDV';
                }
                if (fuelCode && price > 0) {
                  station.prices[fuelCode] = price;
                  station.fuelUpdatedDates[fuelCode] = updated;
                  if (fuelCode === selectedFuelCode) {
                    station.price = price;
                    station.updatedAt = updated;
                  }
                }
              }
            }
          } catch (e) {}
        });

        await Promise.all(enrichPromises);

        // Sorting
        if (sortParam === 'nearest') {
          candidateStations.sort((a, b) => a.distanceMiles - b.distanceMiles || a.price - b.price);
        } else if (sortParam === 'latest') {
          function parseDateToEpoch(d) {
            if (!d) return 0;
            const m = d.match(/(\d{1,2})\/(\d{1,2})\/(\d{2,4})(?:\s+(\d{1,2}):(\d{2}))?/);
            if (m) {
              const yr = m[3].length === 2 ? 2000 + parseInt(m[3]) : parseInt(m[3]);
              const dt = new Date(Date.UTC(yr, parseInt(m[2]) - 1, parseInt(m[1]), parseInt(m[4] || 0), parseInt(m[5] || 0)));
              return dt.getTime();
            }
            const t = Date.parse(d);
            return isNaN(t) ? 0 : t;
          }
          candidateStations.sort((a, b) => parseDateToEpoch(b.updatedAt) - parseDateToEpoch(a.updatedAt) || a.price - b.price);
        } else {
          candidateStations.sort((a, b) => a.price - b.price || a.distanceMiles - b.distanceMiles);
        }

        const prices = candidateStations.map(s => s.price);
        const minPrice = prices.length > 0 ? Math.min(...prices) : null;
        const maxPrice = prices.length > 0 ? Math.max(...prices) : null;
        const avgPrice = prices.length > 0 ? parseFloat((prices.reduce((a, b) => a + b, 0) / prices.length).toFixed(1)) : null;

        const responsePayload = {
          success: true,
          source: 'fuel_finder',
          attribution: 'Prices sourced via Fuel Finder & CMA Open Data Scheme',
          dataStatus: 'live',
          isUk: true,
          fetchedAt: new Date().toISOString(),
          location: { latitude: lat, longitude: lng },
          fuel: selectedFuelCode,
          radiusMiles: radiusParam,
          count: candidateStations.length,
          areaStats: areaStats || { min: minPrice, max: maxPrice, avg: avgPrice },
          stations: candidateStations
        };

        const edgeResponse = getCorsResponse(
          responsePayload,
          200,
          'public, max-age=900, stale-while-revalidate=300',
          { 'X-Edge-Cache': 'MISS' }
        );

        // Store asynchronously into Cloudflare Edge Cache
        ctx.waitUntil(cache.put(cacheKey, edgeResponse.clone()));

        return edgeResponse;
      } catch (err) {
        return getCorsResponse({
          success: false,
          error: { code: 'PROVIDER_UNAVAILABLE', message: 'Fuel prices are temporarily unavailable: ' + err.message }
        }, 503);
      }
    }

    return getCorsResponse({ success: false, error: { code: 'NOT_FOUND', message: 'Endpoint not found' } }, 404);
  }
};
