import { useState } from 'react';
import apiClient from '../api/axiosClient';
import UrlAnalyticsModal from './UrlAnalyticsModal';
import { formatExpirationDate } from '../utils/dateFormatter';

const UrlList = ({ urls, onActionComplete }) => {
  const [editingId, setEditingId] = useState(null);
  const [editOriginalUrl, setEditOriginalUrl] = useState('');
  const [editCustomAlias, setEditCustomAlias] = useState('');
  const [editExpiresAt, setEditExpiresAt] = useState('');
  const [editExpirationType, setEditExpirationType] = useState('never'); // 'never' or 'custom'
  const [error, setError] = useState(null);
  const [selectedAnalyticsUrl, setSelectedAnalyticsUrl] = useState(null);
  
  // Custom states for deletion & copy confirmation
  const [deletingId, setDeletingId] = useState(null);
  const [toast, setToast] = useState(null);

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const getShortUrl = (code) => {
    return `${window.location.protocol}//${window.location.hostname}:8080/${code}`;
  };

  const handleCopy = (code) => {
    const link = getShortUrl(code);
    navigator.clipboard.writeText(link);
    showToast('Copied short URL to clipboard!', 'success');
  };

  const handleToggleActive = async (id, currentActive) => {
    try {
      setError(null);
      await apiClient.put(`/urls/${id}/${currentActive ? 'disable' : 'enable'}`);
      showToast(`Link ${currentActive ? 'disabled' : 'enabled'} successfully!`, 'info');
      onActionComplete();
    } catch (err) {
      setError('Failed to update URL status.');
    }
  };

  const handleDelete = async (id) => {
    if (deletingId !== id) {
      setDeletingId(id);
      // Auto-reset confirmation state after 4 seconds if no action is taken
      setTimeout(() => setDeletingId((current) => (current === id ? null : current)), 4000);
      return;
    }

    try {
      setError(null);
      await apiClient.delete(`/urls/${id}`);
      showToast('Short URL deleted successfully!', 'success');
      setDeletingId(null);
      onActionComplete();
    } catch (err) {
      setError('Failed to delete URL.');
      setDeletingId(null);
    }
  };

  const startEdit = (url) => {
    setEditingId(url.id);
    setEditOriginalUrl(url.originalUrl);
    setEditCustomAlias(url.customAlias || '');
    setEditExpiresAt(url.expiresAt ? url.expiresAt.substring(0, 16) : '');
    setEditExpirationType(url.expiresAt ? 'custom' : 'never');
  };

  const handleSaveEdit = async (id) => {
    if (editExpirationType === 'custom' && editExpiresAt) {
      const selectedDate = new Date(editExpiresAt);
      if (selectedDate <= new Date()) {
        setError('Expiration date must be in the future.');
        return;
      }
    }

    try {
      setError(null);
      const payload = {
        originalUrl: editOriginalUrl,
        customAlias: editCustomAlias.trim() || null,
        expiresAt: editExpirationType === 'custom' && editExpiresAt ? new Date(editExpiresAt).toISOString() : null
      };
      await apiClient.put(`/urls/${id}`, payload);
      setEditingId(null);
      showToast('Link configurations updated!', 'success');
      onActionComplete();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update URL settings.');
    }
  };

  const formatDate = (isoString) => {
    return formatExpirationDate(isoString);
  };

  return (
    <div className="card">
      <div className="card-header d-flex justify-content-between align-items-center">
        <div className="d-flex align-items-center">
          <svg className="me-2" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ color: '#60a5fa' }}>
            <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"></path>
            <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"></path>
          </svg>
          <span className="text-white font-weight-bold">Link Directory</span>
        </div>
      </div>
      <div className="card-body p-0">
        {error && <div className="alert alert-danger m-3 py-2 px-3 small">{error}</div>}
        
        <div className="table-responsive">
          <table className="table mb-0">
            <thead>
              <tr>
                <th style={{ width: '25%' }}>Short Link</th>
                <th style={{ width: '35%' }}>Destination</th>
                <th style={{ width: '10%' }} className="text-center">Clicks</th>
                <th style={{ width: '15%' }}>Expires</th>
                <th style={{ width: '15%' }} className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {urls.length === 0 ? (
                <tr>
                  <td colSpan="5" className="text-center py-5 text-secondary">
                    <svg className="mb-2" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ color: '#475569' }}>
                      <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
                    </svg>
                    <div className="small mt-1 text-secondary">No short links found. Create one to get started!</div>
                  </td>
                </tr>
              ) : (
                urls.map((url) => {
                  const isEditing = editingId === url.id;
                  const displayAlias = url.customAlias || url.shortCode;

                  return (
                    <tr key={url.id}>
                      <td>
                        {isEditing ? (
                          <div className="mb-2 text-start">
                            <label className="small text-secondary mb-1">Alias</label>
                            <input 
                              type="text" 
                              className="form-control form-control-sm" 
                              value={editCustomAlias}
                              onChange={(e) => setEditCustomAlias(e.target.value)}
                              placeholder="custom-alias"
                            />
                          </div>
                        ) : (
                          <div className="d-flex align-items-center">
                            <a href={getShortUrl(displayAlias)} target="_blank" rel="noreferrer" className="text-indigo font-weight-bold text-decoration-none">
                              /{displayAlias}
                            </a>
                            <span 
                              className={`ms-2 badge ${url.active ? 'badge-success' : 'badge-danger'}`}
                              style={{ fontSize: '0.65rem', padding: '2px 5px' }}
                            >
                              {url.active ? 'Active' : 'Disabled'}
                            </span>
                          </div>
                        )}
                      </td>
                      
                      <td>
                        {isEditing ? (
                          <div className="mb-2 text-start">
                            <label className="small text-secondary mb-1">Destination URL</label>
                            <input 
                              type="url" 
                              className="form-control form-control-sm" 
                              value={editOriginalUrl}
                              onChange={(e) => setEditOriginalUrl(e.target.value)}
                              required
                            />
                          </div>
                        ) : (
                          <div className="text-secondary text-break small text-truncate text-start" style={{ maxWidth: '300px' }}>
                            {url.originalUrl}
                          </div>
                        )}
                      </td>
                      
                      <td className="text-center font-weight-bold text-white">
                        {url.clickCount}
                      </td>
                      
                      <td>
                        {isEditing ? (
                          <div className="mb-2 text-start" style={{ minWidth: '160px' }}>
                            <label className="small text-secondary mb-1">Expiry</label>
                            <select 
                              className="form-select form-select-sm mb-2"
                              value={editExpirationType}
                              onChange={(e) => {
                                setEditExpirationType(e.target.value);
                                if (e.target.value === 'never') setEditExpiresAt('');
                              }}
                            >
                              <option value="never">Never Expires</option>
                              <option value="custom">Custom Date</option>
                            </select>
                            {editExpirationType === 'custom' && (
                              <input 
                                type="datetime-local" 
                                className="form-control form-control-sm" 
                                value={editExpiresAt}
                                onChange={(e) => setEditExpiresAt(e.target.value)}
                                required={editExpirationType === 'custom'}
                              />
                            )}
                          </div>
                        ) : (
                          <span className="text-secondary small">{formatDate(url.expiresAt)}</span>
                        )}
                      </td>
                      
                      <td className="text-end">
                        {isEditing ? (
                          <div className="d-flex justify-content-end gap-2">
                            <button className="btn btn-sm btn-primary py-1 px-2.5" onClick={() => handleSaveEdit(url.id)}>
                              Save
                            </button>
                            <button className="btn btn-sm btn-secondary py-1 px-2.5" onClick={() => setEditingId(null)}>
                              Cancel
                            </button>
                          </div>
                        ) : (
                          <div className="d-flex justify-content-end gap-2">
                            <button className="btn btn-sm btn-secondary py-1 px-2.5" title="Copy URL" onClick={() => handleCopy(displayAlias)}>
                              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                              </svg>
                            </button>
                            <button className="btn btn-sm btn-secondary py-1 px-2.5" title="Edit" onClick={() => startEdit(url)}>
                              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                <path d="M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                              </svg>
                            </button>
                            <button className="btn btn-sm btn-secondary py-1 px-2.5" title="View Analytics" onClick={() => setSelectedAnalyticsUrl({ id: url.id, code: displayAlias })}>
                              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                                <line x1="18" y1="20" x2="18" y2="10"></line>
                                <line x1="12" y1="20" x2="12" y2="4"></line>
                                <line x1="6" y1="20" x2="6" y2="14"></line>
                              </svg>
                            </button>
                            <button 
                              className={`btn btn-sm ${url.active ? 'btn-secondary' : 'btn-primary'} py-1 px-2.5`} 
                              title={url.active ? 'Disable' : 'Enable'} 
                              onClick={() => handleToggleActive(url.id, url.active)}
                            >
                              {url.active ? (
                                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                                  <rect x="6" y="4" width="4" height="16"></rect>
                                  <rect x="14" y="4" width="4" height="16"></rect>
                                </svg>
                              ) : (
                                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                                  <polygon points="5 3 19 12 5 21 5 3"></polygon>
                                </svg>
                              )}
                            </button>
                            <button 
                              className={`btn btn-sm ${deletingId === url.id ? 'btn-danger text-white' : 'btn-secondary'} py-1 px-2.5`} 
                              title="Delete" 
                              onClick={() => handleDelete(url.id)}
                              style={{ transition: 'all 0.2s ease', minWidth: deletingId === url.id ? '85px' : 'auto' }}
                            >
                              {deletingId === url.id ? (
                                <span style={{ fontSize: '0.75rem', fontWeight: 600 }}>Confirm?</span>
                              ) : (
                                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                                  <polyline points="3 6 5 6 21 6"></polyline>
                                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                                </svg>
                              )}
                            </button>
                          </div>
                        )}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {selectedAnalyticsUrl && (
        <UrlAnalyticsModal 
          urlId={selectedAnalyticsUrl.id}
          urlShortCode={selectedAnalyticsUrl.code}
          onClose={() => setSelectedAnalyticsUrl(null)}
        />
      )}

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

export default UrlList;
