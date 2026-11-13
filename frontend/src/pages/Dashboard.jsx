import { useState, useEffect } from 'react';
import axios from 'axios';
import { ShieldCheck, Users, Activity, Key } from 'lucide-react';

export default function Dashboard() {
  const [user, setUser] = useState(null);
  const [stats, setStats] = useState({ totalUsers: 0, activeUsers: 0 });

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) setUser(JSON.parse(userData));

    // Fetch real stats from backend
    axios.get('http://localhost:8081/api/users')
      .then(res => {
        const users = res.data.content || [];
        setStats({
          totalUsers: users.length,
          activeUsers: users.filter(u => u.active !== false).length
        });
      })
      .catch(err => console.error("Could not fetch user stats"));
  }, []);

  return (
    <div className="animate-fade-in">
      <div className="page-header">
        <h2>Welcome, {user?.name || 'Admin'}</h2>
        <p>User Management Dashboard - Manage roles and access control.</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1.5rem', marginBottom: '2.5rem' }}>
        
        <div className="glass-panel" style={{ padding: '1.5rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ padding: '1rem', background: 'rgba(99, 102, 241, 0.1)', borderRadius: '12px' }}>
            <Users color="var(--primary)" size={24} />
          </div>
          <div>
            <h3 style={{ fontSize: '1.5rem', marginBottom: '0.25rem' }}>{stats.totalUsers}</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Total Users</p>
          </div>
        </div>

        <div className="glass-panel" style={{ padding: '1.5rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ padding: '1rem', background: 'rgba(16, 185, 129, 0.1)', borderRadius: '12px' }}>
            <Activity color="var(--success)" size={24} />
          </div>
          <div>
            <h3 style={{ fontSize: '1.5rem', marginBottom: '0.25rem' }}>{stats.activeUsers}</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Active Accounts</p>
          </div>
        </div>

        <div className="glass-panel" style={{ padding: '1.5rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ padding: '1rem', background: 'rgba(245, 158, 11, 0.1)', borderRadius: '12px' }}>
            <Key color="var(--warning)" size={24} />
          </div>
          <div>
            <h3 style={{ fontSize: '1.5rem', marginBottom: '0.25rem' }}>Secured</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Auth Interceptor</p>
          </div>
        </div>

      </div>

      <div className="glass-panel" style={{ padding: '2rem' }}>
        <h3 style={{ marginBottom: '1.5rem' }}>Registered Users Overview</h3>
        <table className="data-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>Ali Murad</td>
              <td>ali@bottledrop.de</td>
              <td><span className="badge warning">PLANNER</span></td>
              <td><span className="badge success">Active</span></td>
            </tr>
            <tr>
              <td>Emma</td>
              <td>emma@bottledrop.de</td>
              <td><span className="badge success">WAREHOUSE_ASSISTANT</span></td>
              <td><span className="badge success">Active</span></td>
            </tr>
            <tr>
              <td>David</td>
              <td>david@bottledrop.de</td>
              <td><span className="badge danger">BARTENDER</span></td>
              <td><span className="badge success">Active</span></td>
            </tr>
            <tr>
              <td>Sophia Green</td>
              <td>sophia@bottledrop.de</td>
              <td><span className="badge" style={{background: 'rgba(99, 102, 241, 0.1)', color: 'var(--primary)', border: '1px solid var(--primary)'}}>STOCKIST</span></td>
              <td><span className="badge success">Active</span></td>
            </tr>
            <tr>
              <td>John Doe</td>
              <td>john@event.org</td>
              <td><span className="badge" style={{background: 'rgba(148, 163, 184, 0.1)', color: 'var(--text-muted)', border: '1px solid var(--text-muted)'}}>VISITOR</span></td>
              <td><span className="badge success">Active</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
}
