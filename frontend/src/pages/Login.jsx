import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Shield } from 'lucide-react';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await axios.post('http://localhost:8081/api/users/login', {
        email: email,
        password: password
      });

      if (response.data.success) {
        // Save real user data from Spring Boot to localStorage
        localStorage.setItem('user', JSON.stringify({
          id: response.data.userId,
          name: response.data.name,
          role: response.data.role,
          email: email
        }));
        navigate('/');
      } else {
        setError('Login failed: ' + (response.data.message || 'Unknown error'));
      }
    } catch (err) {
      if (err.response && err.response.data && err.response.data.message) {
        setError(err.response.data.message);
      } else {
        setError('Cannot connect to User Management Service (Port 8081). Please ensure it is running.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '1rem' }}>
      <div className="glass-panel animate-fade-in" style={{ width: '100%', maxWidth: '400px', padding: '2.5rem' }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <div style={{ display: 'inline-block', padding: '1rem', background: 'rgba(99, 102, 241, 0.1)', borderRadius: '50%', marginBottom: '1rem' }}>
            <Shield size={32} color="var(--primary)" />
          </div>
          <h1 style={{ fontSize: '1.5rem', marginBottom: '0.5rem' }}>BottleDrop</h1>
          <p style={{ color: 'var(--text-muted)' }}>System Login</p>
        </div>

        {error && <div className="badge danger" style={{ display: 'block', padding: '0.75rem', marginBottom: '1.5rem', textAlign: 'center', borderRadius: '8px' }}>{error}</div>}

        <form onSubmit={handleLogin}>
          <div className="input-group">
            <label>Email Address</label>
            <input 
              type="email" 
              className="input-control" 
              value={email}
              onChange={e => setEmail(e.target.value)}
              placeholder="admin@bottledrop.de"
              required 
            />
          </div>
          <div className="input-group">
            <label>Password</label>
            <input 
              type="password" 
              className="input-control" 
              value={password}
              onChange={e => setPassword(e.target.value)}
              placeholder="••••••••"
              required 
            />
          </div>
          <button type="submit" className="btn btn-primary" style={{ width: '100%', justifyContent: 'center', padding: '0.75rem' }} disabled={loading}>
            {loading ? 'Authenticating...' : 'Sign In'}
          </button>
        </form>
      </div>
    </div>
  );
}
