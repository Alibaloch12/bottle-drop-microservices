import { NavLink, useNavigate } from 'react-router-dom';
import { LayoutDashboard, Users, Package, LogOut, Beer, Trash2 } from 'lucide-react';

export default function Sidebar() {
  const navigate = useNavigate();
  const userStr = localStorage.getItem('user');
  const user = userStr ? JSON.parse(userStr) : null;
  const role = user?.role || 'VISITOR';

  const handleLogout = () => {
    localStorage.removeItem('user');
    navigate('/login');
  };

  // Define access rules
  const canSeeUsers = role === 'PLANNER';
  const canSeeWarehouse = ['PLANNER', 'WAREHOUSE_ASSISTANT', 'STOCKIST'].includes(role);
  const canSeeBars = ['PLANNER', 'BARTENDER', 'VISITOR'].includes(role);
  const canSeeDropPoints = ['PLANNER', 'STOCKIST', 'VISITOR'].includes(role);

  return (
    <div className="sidebar">
      <div className="sidebar-header">
        <h1>BottleDrop</h1>
      </div>
      
      <div className="sidebar-content">
        <div style={{ marginBottom: '1.5rem' }}>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.1em', marginBottom: '0.5rem', paddingLeft: '1rem' }}>Core</p>
          <ul className="nav-menu">
            <li>
              <NavLink to="/" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`} end>
                <LayoutDashboard /> Dashboard
              </NavLink>
            </li>
            
            {canSeeUsers && (
              <li>
                <NavLink to="/users" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  <Users /> Users Directory
                </NavLink>
              </li>
            )}
          </ul>
        </div>

        <div style={{ marginBottom: '1.5rem', opacity: 0.9 }}>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.1em', marginBottom: '0.5rem', paddingLeft: '1rem' }}>Other Services</p>
          <ul className="nav-menu">
            {canSeeWarehouse && (
              <li>
                <NavLink to="/warehouse" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  <Package /> Warehouse Dashboard
                </NavLink>
              </li>
            )}
            
            {canSeeBars && (
              <li>
                <NavLink to="/bars" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  <Beer /> Bars & POS
                </NavLink>
              </li>
            )}
            
            {canSeeDropPoints && (
              <li>
                <NavLink to="/drop-points" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                  <Trash2 /> Drop Points
                </NavLink>
              </li>
            )}
          </ul>
        </div>
      </div>

      <div className="sidebar-footer">
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem', padding: '0 0.5rem' }}>
          <div style={{ width: '32px', height: '32px', borderRadius: '50%', background: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold' }}>
            {user?.name?.charAt(0) || 'U'}
          </div>
          <div style={{ overflow: 'hidden' }}>
            <div style={{ fontWeight: '500', fontSize: '0.875rem', whiteSpace: 'nowrap', textOverflow: 'ellipsis', overflow: 'hidden' }}>
              {user?.name || 'Unknown User'}
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
              {user?.role || 'No Role'}
            </div>
          </div>
        </div>
        <button onClick={handleLogout} className="logout-btn">
          <LogOut size={18} />
          Logout
        </button>
      </div>
    </div>
  );
}
