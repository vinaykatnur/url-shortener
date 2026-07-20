import { NavLink, useNavigate } from 'react-router-dom';

const Header = () => {
  const navigate = useNavigate();
  const token = localStorage.getItem('accessToken');
  const isLoggedIn = Boolean(token);
  
  let isAdmin = false;
  if (token) {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map((c) => {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      const payload = JSON.parse(jsonPayload);
      if (payload.roles && Array.isArray(payload.roles)) {
        isAdmin = payload.roles.some(r => r.authority === 'ROLE_ADMIN');
      }
    } catch (e) {
      console.error("JWT decoding failed in Header:", e);
    }
  }

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    navigate('/login');
  };

  return (
    <nav className="navbar navbar-expand navbar-dark">
      <div className="container">
        <NavLink className="navbar-brand d-flex align-items-center" to="/">
          <svg className="me-2" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ color: '#4f46e5' }}>
            <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"></path>
            <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"></path>
          </svg>
          Horizon
        </NavLink>
        
        <div className="collapse navbar-collapse" id="navbarNav">
          <ul className="navbar-nav ms-auto align-items-center">
            {isLoggedIn && (
              <li className="nav-item me-3">
                <NavLink 
                  className={({ isActive }) => `nav-link ${isActive ? 'active text-white' : ''}`}
                  to="/"
                  end
                >
                  My URLs
                </NavLink>
              </li>
            )}
            {isLoggedIn && isAdmin && (
              <>
                <li className="nav-item me-3">
                  <NavLink 
                    className={({ isActive }) => `nav-link ${isActive ? 'active text-white' : ''}`}
                    to="/dashboard"
                  >
                    Metrics
                  </NavLink>
                </li>
                <li className="nav-item me-3">
                  <NavLink 
                    className={({ isActive }) => `nav-link ${isActive ? 'active text-white' : ''}`}
                    to="/admin"
                  >
                    Admin Control
                  </NavLink>
                </li>
              </>
            )}
            {!isLoggedIn ? (
              <>
                <li className="nav-item me-2">
                  <NavLink className="btn btn-secondary px-3 py-1.5" to="/login">Login</NavLink>
                </li>
                <li className="nav-item">
                  <NavLink className="btn btn-primary px-3 py-1.5" to="/register">Register</NavLink>
                </li>
              </>
            ) : (
              <li className="nav-item">
                <button className="btn btn-secondary px-3 py-1.5" onClick={handleLogout}>
                  Logout
                </button>
              </li>
            )}
          </ul>
        </div>
      </div>
    </nav>
  );
};

export default Header;
