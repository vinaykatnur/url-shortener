import { useEffect, useState } from 'react';
import Header from '../components/Header';
import UrlForm from '../components/UrlForm';
import UrlList from '../components/UrlList';
import apiClient from '../api/axiosClient';

const HomePage = () => {
  const [urls, setUrls] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Search and filter states
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all'); // 'all', 'active', 'disabled'

  const loadUrls = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const params = {};
      if (searchTerm.trim()) {
        params.search = searchTerm.trim();
      }
      if (statusFilter === 'active') {
        params.active = true;
      } else if (statusFilter === 'disabled') {
        params.active = false;
      }
      
      const response = await apiClient.get('/urls', { params });
      setUrls(response.data.data.content || []);
    } catch (err) {
      setError('Unable to load URL link directory.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUrls();
  }, [searchTerm, statusFilter]);

  const handleCreate = (newUrl) => {
    loadUrls();
  };

  return (
    <div className="d-flex flex-column min-vh-100">
      <Header />
      <main className="container my-5">
        <div className="text-center mb-5">
          <h1 className="display-5 text-white font-weight-bold mb-2" style={{ letterSpacing: '-1px' }}>
            Horizon Link Platform
          </h1>
          <p className="text-secondary lead mx-auto" style={{ maxWidth: '600px', fontSize: '1rem' }}>
            A high-performance URL shortening and link management dashboard built for developers.
          </p>
        </div>

        <UrlForm onCreate={handleCreate} />

        <div className="card mb-4 mt-5">
          <div className="card-body">
            <div className="row gy-3">
              <div className="col-md-8">
                <label className="form-label text-start d-block">Search Links</label>
                <input 
                  type="text" 
                  className="form-control" 
                  placeholder="Search by destination URL, short code, or alias..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              <div className="col-md-4">
                <label className="form-label text-start d-block">Filter Status</label>
                <select 
                  className="form-select" 
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                >
                  <option value="all">All Links</option>
                  <option value="active">Active Only</option>
                  <option value="disabled">Disabled Only</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        {error && <div className="alert alert-danger py-2.5 small mb-4">{error}</div>}
        
        {loading ? (
          <div className="card">
            <div className="skeleton-row">
              <div className="skeleton-item" style={{ width: '30%' }}></div>
              <div className="skeleton-item" style={{ height: '80px' }}></div>
              <div className="skeleton-item" style={{ width: '70%' }}></div>
            </div>
          </div>
        ) : (
          <UrlList urls={urls} onActionComplete={loadUrls} />
        )}
      </main>
    </div>
  );
};

export default HomePage;
