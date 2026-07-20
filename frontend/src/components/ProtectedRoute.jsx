import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children, isAdmin = false }) => {
  const accessToken = localStorage.getItem('accessToken');

  if (!accessToken) {
    return <Navigate to="/login" replace />;
  }

  if (isAdmin) {
    try {
      const base64Url = accessToken.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map((c) => {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      const payload = JSON.parse(jsonPayload);
      
      const userIsAdmin = payload.roles && Array.isArray(payload.roles) && 
        payload.roles.some(r => r.authority === 'ROLE_ADMIN');
        
      if (!userIsAdmin) {
        return <Navigate to="/" replace />;
      }
    } catch (e) {
      console.error("JWT decoding failed in ProtectedRoute:", e);
      return <Navigate to="/" replace />;
    }
  }

  return children;
};

export default ProtectedRoute;
