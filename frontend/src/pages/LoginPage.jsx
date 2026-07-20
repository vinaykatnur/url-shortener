import { useState, useEffect, useRef } from 'react';
import Header from '../components/Header';
import apiClient from '../api/axiosClient';
import { Link, useLocation } from 'react-router-dom';

const LoginPage = () => {
  const location = useLocation();
  const passwordRef = useRef(null);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (location.state?.registrationSuccess) {
      setSuccess('Account created successfully. Please sign in below.');
      if (location.state?.email) {
        setEmail(location.state.email);
        setTimeout(() => {
          passwordRef.current?.focus();
        }, 100);
      }
    }
  }, [location]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);

    // Validate email format strictly (must include TLD, e.g. name@company.com)
    const emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,6}$/;
    if (!emailRegex.test(email)) {
      setError('Please enter a valid email address (e.g. name@company.com).');
      return;
    }

    setLoading(true);

    try {
      const response = await apiClient.post('/auth/login', { email, password });
      localStorage.setItem('accessToken', response.data.data.accessToken);
      localStorage.setItem('refreshToken', response.data.data.refreshToken);
      window.location.href = '/';
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid email or password.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="d-flex flex-column min-vh-100">
      <Header />
      <main className="container flex-grow-1 d-flex align-items-center justify-content-center my-5">
        <div className="card w-100" style={{ maxWidth: '450px' }}>
          <div className="card-header text-center">
            <h3 className="mb-0 text-white font-weight-bold">Welcome Back</h3>
            <p className="text-secondary small mb-0 mt-1">Sign in to manage your short links</p>
          </div>
          <div className="card-body">
            {success && <div className="alert alert-success py-2.5 small mb-3">{success}</div>}
            {error && <div className="alert alert-danger py-2.5 small mb-3">{error}</div>}
            
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label">Email Address</label>
                <input 
                  type="email" 
                  className="form-control" 
                  placeholder="name@company.com"
                  value={email} 
                  onChange={(e) => setEmail(e.target.value)} 
                  required 
                />
              </div>
              <div className="mb-4">
                <label className="form-label">Password</label>
                <input 
                  type="password" 
                  className="form-control" 
                  placeholder="••••••••"
                  value={password} 
                  onChange={(e) => setPassword(e.target.value)} 
                  ref={passwordRef}
                  required 
                />
              </div>
              <button className="btn btn-primary w-100 d-flex align-items-center justify-content-center" type="submit" disabled={loading}>
                {loading ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    Signing in...
                  </>
                ) : 'Sign In'}
              </button>
            </form>
            
            <div className="text-center mt-4">
              <span className="text-secondary small">Don't have an account? </span>
              <Link to="/register" className="small text-white font-weight-bold text-decoration-none">Create account</Link>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default LoginPage;
