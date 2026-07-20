import { useEffect, useState } from 'react';
import apiClient from '../api/axiosClient';
import { formatAnalyticsDate } from '../utils/dateFormatter';

const UrlAnalyticsModal = ({ urlId, urlShortCode, onClose }) => {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchAnalytics = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await apiClient.get(`/analytics/urls/${urlId}`);
        setAnalytics(response.data.data);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load URL analytics details.');
      } finally {
        setLoading(false);
      }
    };

    if (urlId) {
      fetchAnalytics();
    }
  }, [urlId]);

  const formatDate = (isoString) => {
    return formatAnalyticsDate(isoString);
  };

  const getShortUrl = (code) => {
    return `${window.location.protocol}//${window.location.hostname}:8080/${code}`;
  };

  return (
    <div className="modal-backdrop d-flex align-items-center justify-content-center" style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(5, 8, 16, 0.85)',
      backdropFilter: 'blur(6px)',
      zIndex: 1050
    }}>
      <div className="card w-100 mx-3" style={{ maxWidth: '600px', border: '1px solid var(--border-color)' }}>
        <div className="card-header d-flex justify-content-between align-items-center">
          <div className="d-flex align-items-center">
            <svg className="me-2" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ color: '#3b82f6' }}>
              <line x1="18" y1="20" x2="18" y2="10"></line>
              <line x1="12" y1="20" x2="12" y2="4"></line>
              <line x1="6" y1="20" x2="6" y2="14"></line>
            </svg>
            <span className="text-white font-weight-bold">Analytics for /{urlShortCode}</span>
          </div>
          <button className="btn btn-sm btn-secondary px-2.5 py-1" style={{ border: 'none', background: 'transparent', color: '#94a3b8', fontSize: '1rem' }} onClick={onClose}>
            ✕
          </button>
        </div>
        <div className="card-body">
          {loading && (
            <div className="text-center py-5">
              <span className="spinner-border spinner-border-md text-primary" role="status"></span>
              <p className="text-secondary small mt-3">Analyzing click event records...</p>
            </div>
          )}
          
          {error && (
            <div className="alert alert-danger mb-0">
              {error}
              <div className="mt-3 text-end">
                <button className="btn btn-secondary btn-sm" onClick={onClose}>Close</button>
              </div>
            </div>
          )}

          {analytics && !loading && (
            <div>
              <div className="mb-4 text-start">
                <label className="form-label d-block text-secondary small mb-1">Destination URL</label>
                <a href={analytics.originalUrl} target="_blank" rel="noreferrer" className="text-white text-break font-weight-medium small d-block">
                  {analytics.originalUrl}
                </a>
                
                <label className="form-label d-block text-secondary small mb-1 mt-3">Short URL Link</label>
                <a href={getShortUrl(analytics.customAlias || analytics.shortCode)} target="_blank" rel="noreferrer" className="text-indigo font-weight-medium small d-block" style={{ color: '#818cf8' }}>
                  {getShortUrl(analytics.customAlias || analytics.shortCode)}
                </a>
              </div>

              <div className="row g-3 mb-4">
                <div className="col-6">
                  <div className="stat-card text-center">
                    <span className="stat-label">Total Clicks</span>
                    <div className="stat-value" style={{ color: '#818cf8' }}>{analytics.totalClicks}</div>
                  </div>
                </div>
                <div className="col-6">
                  <div className="stat-card text-center">
                    <span className="stat-label">Clicks Today</span>
                    <div className="stat-value" style={{ color: '#34d399' }}>{analytics.clicksToday}</div>
                  </div>
                </div>
                <div className="col-6">
                  <div className="stat-card text-center">
                    <span className="stat-label">Last 7 Days</span>
                    <div className="stat-value" style={{ color: '#fbbf24' }}>{analytics.clicksLast7Days}</div>
                  </div>
                </div>
                <div className="col-6">
                  <div className="stat-card text-center">
                    <span className="stat-label">Last 30 Days</span>
                    <div className="stat-value" style={{ color: '#38bdf8' }}>{analytics.clicksLast30Days}</div>
                  </div>
                </div>
              </div>

              <div className="border-top border-secondary pt-3">
                <div className="d-flex justify-content-between mb-2 small">
                  <span className="text-secondary">First Access:</span>
                  <span className="text-white">{formatDate(analytics.firstClickDate)}</span>
                </div>
                <div className="d-flex justify-content-between small">
                  <span className="text-secondary">Last Access:</span>
                  <span className="text-white">{formatDate(analytics.lastClickDate)}</span>
                </div>
              </div>

              <div className="text-end mt-4">
                <button className="btn btn-secondary px-4 py-2" onClick={onClose}>Close Panel</button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default UrlAnalyticsModal;
