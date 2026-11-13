import { useState, useEffect } from 'react';
import axios from 'axios';
import { UserCheck, UserX, AlertTriangle, Edit2, X } from 'lucide-react';

export default function UsersDirectory() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Edit Modal State
  const [editingUser, setEditingUser] = useState(null);
  const [editForm, setEditForm] = useState({ name: '', email: '', role: '', password: '' });
  const [editSaving, setEditSaving] = useState(false);

  const fetchUsers = async () => {
    try {
      const response = await axios.get('http://localhost:8081/api/users');
      setUsers(response.data.content || []);
    } catch (err) {
      setError('Cannot connect to User Management Service (Port 8081). Please ensure it is running.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const toggleUserStatus = async (user) => {
    try {
      const endpoint = user.active !== false ? 'deactivate' : 'activate';
      await axios.post(`http://localhost:8081/api/users/${user.id}/${endpoint}`);
      fetchUsers();
    } catch (err) {
      alert("Failed to change user status. Make sure you restarted the User Management Service.");
    }
  };

  const openEditModal = (user) => {
    setEditingUser(user);
    setEditForm({ name: user.name, email: user.email, role: user.role, password: '' });
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setEditSaving(true);
    try {
      const payload = {
        name: editForm.name,
        email: editForm.email,
        role: editForm.role,
      };
      if (editForm.password.trim() !== '') {
        payload.password = editForm.password;
      }
      
      await axios.put(`http://localhost:8081/api/users/${editingUser.id}`, payload);
      setEditingUser(null);
      fetchUsers();
    } catch (err) {
      alert("Failed to update user. " + (err.response?.data?.message || err.message));
    } finally {
      setEditSaving(false);
    }
  };

  return (
    <div className="animate-fade-in" style={{ position: 'relative' }}>
      <div className="page-header">
        <h2>Users Directory</h2>
        <p>List of all registered users fetched live from the database.</p>
      </div>

      {error && <div className="badge warning" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem', padding: '0.75rem 1rem', borderRadius: '8px' }}><AlertTriangle size={16} /> {error}</div>}

      <div className="glass-panel" style={{ padding: '0', overflow: 'hidden' }}>
        <table className="data-table">
          <thead style={{ background: 'rgba(0,0,0,0.2)' }}>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Email</th>
              <th>System Role</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan="6" style={{ textAlign: 'center', padding: '2rem' }}>Loading users from database...</td></tr>
            ) : users.length === 0 && !error ? (
              <tr><td colSpan="6" style={{ textAlign: 'center', padding: '2rem' }}>No users found.</td></tr>
            ) : users.map(u => (
              <tr key={u.id}>
                <td>#{u.id}</td>
                <td><strong style={{ opacity: u.active !== false ? 1 : 0.5 }}>{u.name}</strong></td>
                <td style={{ opacity: u.active !== false ? 1 : 0.5 }}>{u.email}</td>
                <td>
                  {u.role === 'PLANNER' && <span className="badge warning">PLANNER</span>}
                  {u.role === 'WAREHOUSE_ASSISTANT' && <span className="badge success">WAREHOUSE_ASSISTANT</span>}
                  {u.role === 'BARTENDER' && <span className="badge danger">BARTENDER</span>}
                  {u.role === 'STOCKIST' && <span className="badge" style={{background: 'rgba(99, 102, 241, 0.1)', color: 'var(--primary)', border: '1px solid var(--primary)'}}>STOCKIST</span>}
                  {u.role === 'VISITOR' && <span className="badge" style={{background: 'rgba(148, 163, 184, 0.1)', color: 'var(--text-muted)', border: '1px solid var(--text-muted)'}}>VISITOR</span>}
                </td>
                <td>
                  {u.active !== false ? <span className="badge success">Active</span> : <span className="badge danger">Inactive</span>}
                </td>
                <td>
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button onClick={() => openEditModal(u)} className="btn" style={{ background: 'transparent', padding: '0.25rem', color: 'var(--primary)' }} title="Edit User">
                      <Edit2 size={18} />
                    </button>
                    <button onClick={() => toggleUserStatus(u)} className="btn" style={{ background: 'transparent', padding: '0.25rem', color: u.active !== false ? 'var(--danger)' : 'var(--success)' }} title={u.active !== false ? "Deactivate User" : "Activate User"}>
                      {u.active !== false ? <UserX size={18} /> : <UserCheck size={18} />}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Edit User Modal */}
      {editingUser && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '1rem' }}>
          <div className="glass-panel animate-fade-in" style={{ width: '100%', maxWidth: '500px', padding: '2rem', background: 'var(--bg-dark)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h3 style={{ margin: 0 }}>Edit User #{editingUser.id}</h3>
              <button className="btn" onClick={() => setEditingUser(null)} style={{ background: 'transparent', padding: '0.25rem' }}>
                <X size={20} color="var(--text-muted)" />
              </button>
            </div>
            
            <form onSubmit={handleEditSubmit}>
              <div className="input-group">
                <label>Full Name</label>
                <input type="text" className="input-control" value={editForm.name} onChange={e => setEditForm({...editForm, name: e.target.value})} required />
              </div>
              
              <div className="input-group">
                <label>Email Address</label>
                <input type="email" className="input-control" value={editForm.email} onChange={e => setEditForm({...editForm, email: e.target.value})} required />
              </div>

              <div className="input-group">
                <label>System Role</label>
                <select className="input-control" value={editForm.role} onChange={e => setEditForm({...editForm, role: e.target.value})} style={{ WebkitAppearance: 'none' }}>
                  <option value="PLANNER">Planner (Admin)</option>
                  <option value="WAREHOUSE_ASSISTANT">Warehouse Assistant</option>
                  <option value="BARTENDER">Bartender</option>
                  <option value="STOCKIST">Stockist</option>
                  <option value="VISITOR">Visitor</option>
                </select>
              </div>

              <div className="input-group">
                <label>New Password (Optional)</label>
                <input type="password" className="input-control" value={editForm.password} onChange={e => setEditForm({...editForm, password: e.target.value})} placeholder="Leave blank to keep unchanged" />
              </div>

              <div style={{ display: 'flex', gap: '1rem', marginTop: '2rem' }}>
                <button type="button" className="btn" onClick={() => setEditingUser(null)} style={{ flex: 1, justifyContent: 'center', background: 'rgba(255,255,255,0.05)' }}>Cancel</button>
                <button type="submit" className="btn btn-primary" style={{ flex: 1, justifyContent: 'center' }} disabled={editSaving}>
                  {editSaving ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
