import { useEffect, useState } from 'react';
import Header from '../components/Header';
import apiClient from '../api/axiosClient';

const DashboardPage = () => {
  const [stats, setStats] = useState(null);
  const [topUrls, setTopUrls] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadStats = async () => {
      try {
        setLoading(true);
        setError(null);
        const [statsResponse, topUrlsResponse] = await Promise.all([
          apiClient.get('/analytics/dashboard'),
          apiClient.get('/analytics/top-urls')
        ]);
        setStats(statsResponse.data.data);
        setTopUrls(topUrlsResponse.data.data || []);
      } catch (err) {
        setError(err.response?.data?.message || 'Unable to load dashboard metrics.');
      } finally {
        setLoading(false);
      }
    };
    loadStats();
  }, []);

  const getShortUrl = (code) => {
    return `${window.location.protocol}//${window.location.hostname}:8080/${code}`;
  };

  return (
    <div className="d-flex flex-column min-vh-100">
      <Header />
      <main className="container my-5 text-start">
        <div className="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h1 className="h2 text-white font-weight-bold mb-1">Platform Analytics</h1>
            <p className="text-secondary small mb-0">System metrics overview for Admins</p>
          </div>
        </div>

        {error && <div className="alert alert-danger py-2 px-3 small mb-4">{error}</div>}

        {loading ? (
          <div className="card">
            <div className="skeleton-row">
              <div className="skeleton-item" style={{ width: '40%' }}></div>
              <div className="skeleton-item" style={{ height: '80px' }}></div>
              <div className="skeleton-item"></div>
            </div>
          </div>
        ) : (
          <>
            {stats && (
              <div className="row g-3 mb-5">
                <div className="col-md-4 col-sm-6">
                  <div className="stat-card">
                    <div className="d-flex justify-content-between align-items-start">
                      <span className="stat-label">Total Users</span>
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ color: '#60a5fa' }}>
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v-2"></path>
                        <circle cx="12" cy="7" r="4"></circle>
                      </svg>
                    </div>
                    <div className="stat-value">{stats.totalUsers}</div>
                    <div className="small text-secondary mt-2">
                      <span className="text-white font-weight-medium">{stats.usersRegisteredToday}</span> registered today
                    </div>
                  </div>
                </div>
                
                <div className="col-md-4 col-sm-6">
                  <div className="stat-card">
                    <div className="d-flex justify-content-between align-items-start">
                      <span className="stat-label">Shortened Links</span>
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ color: '#818cf8' }}>
                        <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"></path>
                        <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"></path>
                      </svg>
                    </div>
                    <div className="stat-value text-indigo" style={{ color: '#818cf8' }}>
                      {stats.totalUrls}
                    </div>
                    <div className="small text-secondary mt-2">
                      <span className="text-white font-weight-medium">{stats.activeUrls}</span> active | <span className="text-white font-weight-medium">{stats.urlsCreatedToday}</span> created today
                    </div>
                  </div>
                </div>

                <div className="col-md-4 col-sm-12">
                  <div className="stat-card">
                    <div className="d-flex justify-content-between align-items-start">
                      <span className="stat-label">Total Redirects</span>
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ color: '#34d399' }}>
                        <line x1="18" y1="20" x2="18" y2="10"></line>
                        <line x1="12" y1="20" x2="12" y2="4"></line>
                        <line x1="6" y1="20" x2="6" y2="14"></line>
                      </svg>
                    </div>
                    <div className="stat-value text-success" style={{ color: '#34d399' }}>
                      {stats.totalClicks}
                    </div>
                    <div className="small text-secondary mt-2">
                      Accumulated platform redirections
                    </div>
                  </div>
                </div>
              </div>
            )}

            <div className="card">
              <div className="card-header d-flex align-items-center">
                <svg className="me-2 text-warning" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ color: '#fbbf24' }}>
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                </svg>
                <span className="text-white font-weight-bold">Top Performing Links</span>
              </div>
              <div className="card-body p-0">
                <div className="table-responsive">
                  <table className="table mb-0">
                    <thead>
                      <tr>
                        <th>Short link</th>
                        <th>Original Destination</th>
                        <th className="text-center">Total Clicks</th>
                        <th>Today</th>
                        <th>Last 7 Days</th>
                      </tr>
                    </thead>
                    <tbody>
                      {topUrls.length === 0 ? (
                        <tr>
                          <td colSpan="5" className="text-center py-4 text-secondary small">
                            No redirect telemetry records found.
                          </td>
                        </tr>
                      ) : (
                        topUrls.map((url) => (
                          <tr key={url.id}>
                            <td>
                              <a href={getShortUrl(url.customAlias || url.shortCode)} target="_blank" rel="noreferrer" className="text-indigo font-weight-medium">
                                /{url.customAlias || url.shortCode}
                              </a>
                            </td>
                            <td>
                              <div className="text-secondary small text-truncate" style={{ maxWidth: '400px' }}>
                                {url.originalUrl}
                              </div>
                            </td>
                            <td className="text-center font-weight-bold text-indigo" style={{ color: '#818cf8' }}>
                              {url.totalClicks}
                            </td>
                            <td>
                              <span className="badge badge-success" style={{ fontSize: '0.7rem' }}>
                                +{url.clicksToday}
                              </span>
                            </td>
                            <td className="text-secondary small">
                              {url.clicksLast7Days} clicks
                            </td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </>
        )}
      </main>
    </div>
  );
};

export default DashboardPage;
