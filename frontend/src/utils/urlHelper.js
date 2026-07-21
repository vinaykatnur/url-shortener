export const getShortUrl = (code) => {
  if (!code) return '';
  const defaultBackendOrigin = 'https://url-shortener-umev.onrender.com';
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'https://url-shortener-umev.onrender.com/api/v1';
  
  try {
    const url = new URL(apiBaseUrl);
    return `${url.origin}/${code}`;
  } catch (e) {
    return `${defaultBackendOrigin}/${code}`;
  }
};
