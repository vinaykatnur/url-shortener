import { useState } from 'react';
import Header from '../components/Header';
import apiClient from '../api/axiosClient';
import { Link, useNavigate } from 'react-router-dom';

const RegisterPage = () => {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

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
      await apiClient.post('/auth/register', { name, email, password });
      navigate('/login', { state: { registrationSuccess: true, email: email } });
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Email might already be taken.');
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
            <h3 className="mb-0 text-white font-weight-bold">Create Account</h3>
            <p className="text-secondary small mb-0 mt-1">Get started with Horizon shortener today</p>
          </div>
          <div className="card-body">
            {error && <div className="alert alert-danger py-2.5 small">{error}</div>}
            
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label">Full Name</label>
                <input 
                  type="text" 
                  className="form-control" 
                  placeholder="John Doe"
                  value={name} 
                  onChange={(e) => setName(e.target.value)} 
                  required 
                />
              </div>
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
                  placeholder="Minimum 8 characters"
                  value={password} 
                  onChange={(e) => setPassword(e.target.value)} 
                  required 
                  minLength={8}
                />
              </div>
              <button className="btn btn-primary w-100 d-flex align-items-center justify-content-center" type="submit" disabled={loading}>
                {loading ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    Creating account...
                  </>
                ) : 'Register'}
              </button>
            </form>
            
            <div className="text-center mt-4">
              <span className="text-secondary small">Already have an account? </span>
              <Link to="/login" className="small text-white font-weight-bold text-decoration-none">Sign in</Link>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default RegisterPage;
