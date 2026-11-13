import { useState } from 'react';
import axios from 'axios';
import { UserPlus } from 'lucide-react';

export default function AddUser() {
  const [formData, setFormData] = useState({ name: '', email: '', password: '', role: 'VISITOR' });
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setSuccess('');
    setError('');
    
    try {
      // Connect to User Management Service API
      const response = await axios.post('http://localhost:8081/api/users', formData);
      setSuccess(`User ${response.data.name} successfully registered with role ${response.data.role}!`);
      setFormData({ name: '', email: '', password: '', role: 'VISITOR' });
    } catch (err) {
      if (err.response && err.response.data && err.response.data.message) {
        setError(err.response.data.message);
      } else {
        setError('Failed to create user. Ensure the User Management Service (Port 8081) is running.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="animate-fade-in" style={{ maxWidth: '600px' }}>
      <div className="page-header">
        <h2>Add New User</h2>
        <p>Register a new system user and assign their access role in the database.</p>
      </div>

      {success && <div className="badge success" style={{ display: 'block', padding: '1rem', marginBottom: '2rem', fontSize: '0.875rem' }}>{success}</div>}
      {error && <div className="badge danger" style={{ display: 'block', padding: '1rem', marginBottom: '2rem', fontSize: '0.875rem' }}>{error}</div>}

      <div className="glass-panel" style={{ padding: '2rem' }}>
        <form onSubmit={handleSubmit}>
          <div className="input-group">
            <label>Full Name</label>
            <input type="text" className="input-control" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} placeholder="e.g. Ali Murad" required />
          </div>
          
          <div className="input-group">
            <label>Email Address</label>
            <input type="email" className="input-control" value={formData.email} onChange={e => setFormData({...formData, email: e.target.value})} placeholder="user@bottledrop.de" required />
          </div>

          <div className="input-group">
            <label>Password</label>
            <input type="password" className="input-control" value={formData.password} onChange={e => setFormData({...formData, password: e.target.value})} placeholder="••••••••" required />
          </div>

          <div className="input-group">
            <label>System Role</label>
            <select className="input-control" value={formData.role} onChange={e => setFormData({...formData, role: e.target.value})} style={{ WebkitAppearance: 'none' }}>
              <option value="PLANNER">Planner (Admin)</option>
              <option value="WAREHOUSE_ASSISTANT">Warehouse Assistant</option>
              <option value="BARTENDER">Bartender</option>
              <option value="STOCKIST">Stockist</option>
              <option value="VISITOR">Visitor</option>
            </select>
          </div>

          <button type="submit" className="btn btn-primary" style={{ marginTop: '1rem' }} disabled={loading}>
            <UserPlus size={18} /> {loading ? 'Registering...' : 'Register User'}
          </button>
        </form>
      </div>
    </div>
  );
}
