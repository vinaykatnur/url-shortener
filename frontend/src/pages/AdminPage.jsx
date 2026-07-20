import { useEffect, useState } from 'react';
import Header from '../components/Header';
import apiClient from '../api/axiosClient';

const AdminPage = () => {
  const [users, setUsers] = useState([]);
  const [urls, setUrls] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('users'); // 'users' or 'urls'
  const [searchTerm, setSearchTerm] = useState('');
  const [toast, setToast] = useState(null);

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const loadAdminData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const [usersResponse, urlsResponse] = await Promise.all([
        apiClient.get('/admin/users', { params: { size: 100 } }),
        apiClient.get('/admin/urls', { params: { size: 100, search: searchTerm } })
      ]);
      
      setUsers(usersResponse.data.data.content || []);
      setUrls(urlsResponse.data.data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to load admin administration panel telemetry.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAdminData();
  }, [searchTerm]);

  const updateUserStatus = async (userId, currentEnabled) => {
    try {
      setError(null);
      const action = currentEnabled ? 'disable' : 'enable';
      const response = await apiClient.put(`/admin/users/${userId}/${action}`);
      setUsers((current) => current.map((user) => (user.id === userId ? response.data.data : user)));
      showToast(`User status updated to ${currentEnabled ? 'Disabled' : 'Enabled'}.`, 'info');
    } catch {
      setError('Unable to update user enable state.');
    }
  };

  const updateUrlStatus = async (urlId, currentActive) => {
    try {
      setError(null);
      const action = currentActive ? 'disable' : 'enable';
      const response = await apiClient.put(`/admin/urls/${urlId}/${action}`);
      setUrls((current) => current.map((url) => (url.id === urlId ? response.data.data : url)));
      showToast(`Short link status updated to ${currentActive ? 'Disabled' : 'Enabled'}.`, 'info');
    } catch {
      setError('Unable to update short link enable state.');
    }
  };

  const getShortUrl = (code) => {
    return `${window.location.protocol}//${window.location.hostname}:8080/${code}`;
  };

  return (
    <div className="d-flex flex-column min-vh-100">
      <Header />
      <main className="container my-5 text-start">
        <div className="mb-4">
          <h1 className="h2 text-white font-weight-bold mb-1">Administrative Center</h1>
          <p className="text-secondary small mb-0">Platform governance panel</p>
        </div>

        {error && <div className="alert alert-danger py-2 px-3 small mb-4">{error}</div>}

        <div className="d-flex border-bottom border-secondary mb-4 gap-2">
          <button 
            className={`btn py-2.5 px-3 border-0 d-inline-flex align-items-center ${activeTab === 'users' ? 'text-white border-bottom border-indigo font-weight-bold' : 'text-secondary'}`}
            style={{ background: 'transparent', borderRadius: 0, borderBottom: activeTab === 'users' ? '2px solid #4f46e5 !important' : 'none' }}
            onClick={() => { setActiveTab('users'); setSearchTerm(''); }}
          >
            <svg className="me-2" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
            </svg>
            User Governance ({users.length})
          </button>
          <button 
            className={`btn py-2.5 px-3 border-0 d-inline-flex align-items-center ${activeTab === 'urls' ? 'text-white border-bottom border-indigo font-weight-bold' : 'text-secondary'}`}
            style={{ background: 'transparent', borderRadius: 0, borderBottom: activeTab === 'urls' ? '2px solid #4f46e5 !important' : 'none' }}
            onClick={() => { setActiveTab('urls'); setSearchTerm(''); }}
          >
            <svg className="me-2" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"></path>
              <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"></path>
            </svg>
            Shortcode Governance ({urls.length})
          </button>
        </div>

        {activeTab === 'urls' && (
          <div className="card mb-4">
            <div className="card-body py-3">
              <input 
                type="text" 
                className="form-control" 
                placeholder="Search across all shortened destination URLs, codes, and aliases..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
          </div>
        )}

        {loading ? (
          <div className="card">
            <div className="skeleton-row">
              <div className="skeleton-item" style={{ width: '60%' }}></div>
              <div className="skeleton-item" style={{ height: '120px' }}></div>
            </div>
          </div>
        ) : (
          <>
            {activeTab === 'users' && (
              <div className="card">
                <div className="card-body p-0">
                  <div className="table-responsive">
                    <table className="table mb-0">
                      <thead>
                        <tr>
                          <th>Account Holder</th>
                          <th>Email Address</th>
                          <th>Privilege Tier</th>
                          <th>Status</th>
                          <th className="text-end">Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {users.map((user) => (
                          <tr key={user.id}>
                            <td className="text-white font-weight-medium">{user.name}</td>
                            <td className="text-secondary small">{user.email}</td>
                            <td>
                              {user.roles.map((role) => (
                                <span key={role} className="badge bg-secondary me-1" style={{ fontSize: '0.65rem' }}>
                                  {role.replace('ROLE_', '')}
                                </span>
                              ))}
                            </td>
                            <td>
                              <span className={`badge ${user.enabled ? 'badge-success' : 'badge-danger'}`}>
                                {user.enabled ? 'Enabled' : 'Disabled'}
                              </span>
                            </td>
                            <td className="text-end">
                              {user.email !== 'admin@example.com' && (
                                <button 
                                  className={`btn btn-sm ${user.enabled ? 'btn-secondary' : 'btn-primary'} py-1 px-3`} 
                                  style={{ fontSize: '0.78rem' }}
                                  onClick={() => updateUserStatus(user.id, user.enabled)}
                                >
                                  {user.enabled ? 'Pause Access' : 'Restore Access'}
                                </button>
                              )}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'urls' && (
              <div className="card">
                <div className="card-body p-0">
                  <div className="table-responsive">
                    <table className="table mb-0">
                      <thead>
                        <tr>
                          <th>Short Code</th>
                          <th>Target Destination</th>
                          <th className="text-center">Clicks</th>
                          <th>Status</th>
                          <th className="text-end">Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {urls.length === 0 ? (
                          <tr>
                            <td colSpan="5" className="text-center py-4 text-secondary small">
                              No shortcode records found matching search filters.
                            </td>
                          </tr>
                        ) : (
                          urls.map((url) => (
                            <tr key={url.id}>
                              <td>
                                <a href={getShortUrl(url.customAlias || url.shortCode)} target="_blank" rel="noreferrer" className="text-indigo font-weight-medium">
                                  /{url.customAlias || url.shortCode}
                                </a>
                              </td>
                              <td>
                                <div className="text-secondary small text-truncate text-start" style={{ maxWidth: '350px' }}>
                                  {url.originalUrl}
                                </div>
                              </td>
                              <td className="text-center font-weight-medium text-white">{url.clickCount}</td>
                              <td>
                                <span className={`badge ${url.active ? 'badge-success' : 'badge-danger'}`}>
                                  {url.active ? 'Active' : 'Disabled'}
                                </span>
                              </td>
                              <td className="text-end">
                                <button 
                                  className={`btn btn-sm ${url.active ? 'btn-secondary' : 'btn-primary'} py-1 px-3`} 
                                  style={{ fontSize: '0.78rem' }}
                                  onClick={() => updateUrlStatus(url.id, url.active)}
                                >
                                  {url.active ? 'Disable Link' : 'Enable Link'}
                                </button>
                              </td>
                            </tr>
                          ))
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            )}
          </>
        )}
      </main>

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

export default AdminPage;
