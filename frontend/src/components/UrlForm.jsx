import { useState } from 'react';
import apiClient from '../api/axiosClient';

const UrlForm = ({ onCreate }) => {
  const [originalUrl, setOriginalUrl] = useState('');
  const [customAlias, setCustomAlias] = useState('');
  const [expiresAt, setExpiresAt] = useState('');
  const [expirationType, setExpirationType] = useState('never'); // 'never' or 'custom'
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    setSuccess(null);
    setLoading(true);

    if (expirationType === 'custom' && expiresAt) {
      const selectedDate = new Date(expiresAt);
      if (selectedDate <= new Date()) {
        setError('Expiration date must be in the future.');
        setLoading(false);
        return;
      }
    }

    try {
      const payload = {
        originalUrl,
        customAlias: customAlias.trim() || null,
        expiresAt: expirationType === 'custom' && expiresAt ? new Date(expiresAt).toISOString() : null
      };

      const response = await apiClient.post('/urls', payload);
      const newUrl = response.data.data;
      
      const shortUrl = `${window.location.protocol}//${window.location.hostname}:8080/${newUrl.shortCode}`;
      setSuccess({
        shortCode: newUrl.shortCode,
        shortUrl: shortUrl
      });
      
      setOriginalUrl('');
      setCustomAlias('');
      setExpiresAt('');
      setExpirationType('never');
      onCreate(newUrl);
      showToast('Short link created successfully!', 'success');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create short URL. Alias might be taken.');
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = () => {
    if (success) {
      navigator.clipboard.writeText(success.shortUrl);
      showToast('Short URL copied to clipboard!', 'success');
    }
  };

  return (
    <div className="card mb-4">
      <div className="card-header d-flex align-items-center">
        <svg className="me-2" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ color: '#60a5fa' }}>
          <path d="M12 5v14M5 12h14"></path>
        </svg>
        <span className="text-white font-weight-bold">Create a Short Link</span>
      </div>
      <div className="card-body">
        {error && <div className="alert alert-danger py-2 px-3 small mb-3">{error}</div>}
        {success && (
          <div className="alert alert-success d-flex justify-content-between align-items-center mb-3" style={{ background: 'rgba(16, 185, 129, 0.1)', border: '1px solid rgba(16, 185, 129, 0.2)' }}>
            <div>
              <span className="small d-block text-secondary">Short link created successfully:</span>
              <a href={success.shortUrl} target="_blank" rel="noreferrer" className="text-white font-weight-bold text-decoration-underline small">
                {success.shortUrl}
              </a>
            </div>
            <button className="btn btn-primary btn-sm py-1.5 px-3" onClick={handleCopy} style={{ fontSize: '0.8rem' }}>
              Copy Link
            </button>
          </div>
        )}
        
        <form onSubmit={handleSubmit}>
          <div className="row gy-3">
            <div className="col-12">
              <label className="form-label text-start d-block">Original URL</label>
              <input 
                type="url"
                className="form-control" 
                placeholder="https://example.com/very-long-link-address-here"
                value={originalUrl} 
                onChange={(e) => setOriginalUrl(e.target.value)} 
                required 
              />
            </div>
            <div className="col-md-6">
              <label className="form-label text-start d-block">Custom Alias (Optional)</label>
              <input 
                type="text"
                className="form-control" 
                placeholder="e.g. promo-2026"
                value={customAlias} 
                onChange={(e) => setCustomAlias(e.target.value)} 
              />
            </div>
            <div className="col-md-6">
              <label className="form-label text-start d-block">Link Expiration</label>
              <div className="d-flex gap-3 mb-2" style={{ marginTop: '10px', marginBottom: '10px' }}>
                <div className="form-check">
                  <input 
                    type="radio" 
                    id="create-exp-never" 
                    name="createExpirationType" 
                    className="form-check-input"
                    style={{ cursor: 'pointer' }}
                    checked={expirationType === 'never'}
                    onChange={() => { setExpirationType('never'); setExpiresAt(''); }}
                  />
                  <label htmlFor="create-exp-never" className="form-check-label text-secondary small" style={{ cursor: 'pointer' }}>Never Expires</label>
                </div>
                <div className="form-check">
                  <input 
                    type="radio" 
                    id="create-exp-custom" 
                    name="createExpirationType" 
                    className="form-check-input"
                    style={{ cursor: 'pointer' }}
                    checked={expirationType === 'custom'}
                    onChange={() => setExpirationType('custom')}
                  />
                  <label htmlFor="create-exp-custom" className="form-check-label text-secondary small" style={{ cursor: 'pointer' }}>Custom Date</label>
                </div>
              </div>
              {expirationType === 'custom' && (
                <input 
                  type="datetime-local" 
                  className="form-control" 
                  value={expiresAt} 
                  onChange={(e) => setExpiresAt(e.target.value)} 
                  required={expirationType === 'custom'}
                />
              )}
            </div>
            <div className="col-12 mt-4 text-start">
              <button className="btn btn-primary px-4 py-2 d-inline-flex align-items-center justify-content-center" type="submit" disabled={loading}>
                {loading ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    Shortening...
                  </>
                ) : 'Shorten Link'}
              </button>
            </div>
          </div>
        </form>
      </div>

      {toast && (
        <div className="custom-toast-container">
          <div className={`custom-toast custom-toast-${toast.type}`}>
            <div className="custom-toast-content">
              <div className="custom-toast-title">
                {toast.type === 'success' ? 'Success' : toast.type === 'error' ? 'Error' : 'Info'}
              </div>
              <div className="custom-toast-desc">{toast.message}</div>
            </div>
            <button className="custom-toast-close" onClick={() => setToast(null)}>×</button>
          </div>
        </div>
      )}
    </div>
  );
};

export default UrlForm;
