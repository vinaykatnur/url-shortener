export const getShortUrl = (code) => {
  if (!code) return '';
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'https://url-shortener-umev.onrender.com/api/v1';
  try {
    const url = new URL(apiBaseUrl);
    return `${url.protocol}//${url.host}/${code}`;
  } catch (e) {
    return `${window.location.protocol}//${window.location.hostname}/${code}`;
  }
};
