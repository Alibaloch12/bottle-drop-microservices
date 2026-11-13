import { useState, useEffect } from 'react';
import axios from 'axios';
import { Package, Activity, Inbox, Check, X } from 'lucide-react';

export default function Warehouse() {
  const [beverages, setBeverages] = useState([]);
  const [empties, setEmpties] = useState([]);
  const [requests, setRequests] = useState([]);
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchWarehouseData = async () => {
    setLoading(true);
    setError(null);
    try {
      // Assuming warehouse service runs on 8082
      const statusRes = await axios.get('http://localhost:8082/api/warehouse');
      const bevRes = await axios.get('http://localhost:8082/api/warehouse/beverages');
      const empRes = await axios.get('http://localhost:8082/api/warehouse/empties');
      const reqRes = await axios.get('http://localhost:8082/api/replenishment-requests');

      setStatus(statusRes.data);
      setBeverages(bevRes.data);
      setEmpties(empRes.data);
      setRequests(reqRes.data);
    } catch (err) {
      setError('Cannot connect to Warehouse Service (Port 8082). Please ensure it is running.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWarehouseData();
  }, []);

  const handleFulfill = async (id) => {
    try {
      await axios.patch(`http://localhost:8082/api/replenishment-requests/${id}/fulfill`);
      fetchWarehouseData();
    } catch (err) {
      alert("Failed to fulfill request: " + (err.response?.data?.message || err.message));
    }
  };

  const handleReject = async (id) => {
    try {
      await axios.patch(`http://localhost:8082/api/replenishment-requests/${id}/reject`);
      fetchWarehouseData();
    } catch (err) {
      alert("Failed to reject request: " + (err.response?.data?.message || err.message));
    }
  };

  if (loading) {
    return <div style={{ padding: '2rem' }}>Loading Warehouse Data...</div>;
  }

  if (error) {
    return (
      <div className="animate-fade-in">
        <div className="page-header">
          <h2>Warehouse Dashboard</h2>
          <p>Global inventory and replenishment management.</p>
        </div>
        <div className="badge warning" style={{ padding: '1rem', fontSize: '1rem' }}>{error}</div>
      </div>
    );
  }

  return (
    <div className="animate-fade-in">
      <div className="page-header">
        <h2>Warehouse Dashboard</h2>
        <p>Global inventory and replenishment management.</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1.5rem', marginBottom: '2.5rem' }}>
        <div className="glass-panel" style={{ padding: '1.5rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ padding: '1rem', background: 'rgba(99, 102, 241, 0.1)', borderRadius: '12px' }}>
            <Package color="var(--primary)" size={24} />
          </div>
          <div>
            <h3 style={{ fontSize: '1.5rem', marginBottom: '0.25rem' }}>{status?.totalBeverageCount || 0}</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Total Beverages</p>
          </div>
        </div>
        <div className="glass-panel" style={{ padding: '1.5rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ padding: '1rem', background: 'rgba(239, 68, 68, 0.1)', borderRadius: '12px' }}>
            <Inbox color="var(--danger)" size={24} />
          </div>
          <div>
            <h3 style={{ fontSize: '1.5rem', marginBottom: '0.25rem' }}>{status?.totalEmptyCount || 0}</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Empty Bottles</p>
          </div>
        </div>
        <div className="glass-panel" style={{ padding: '1.5rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ padding: '1rem', background: 'rgba(245, 158, 11, 0.1)', borderRadius: '12px' }}>
            <Activity color="var(--warning)" size={24} />
          </div>
          <div>
            <h3 style={{ fontSize: '1.5rem', marginBottom: '0.25rem' }}>{requests.filter(r => r.status === 'PENDING').length}</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Pending Restocks</p>
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginBottom: '2rem' }}>
        <div className="glass-panel" style={{ padding: '0', overflow: 'hidden' }}>
          <div style={{ padding: '1.5rem', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
            <h3 style={{ margin: 0 }}>Beverage Inventory</h3>
          </div>
          <table className="data-table">
            <thead>
              <tr>
                <th>Beverage Type</th>
                <th>Quantity</th>
              </tr>
            </thead>
            <tbody>
              {beverages.map((item, idx) => (
                <tr key={idx}>
                  <td>{item.beverageType}</td>
                  <td>{item.quantity} units</td>
                </tr>
              ))}
              {beverages.length === 0 && (
                <tr><td colSpan="2" style={{ textAlign: 'center' }}>No stock data available.</td></tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="glass-panel" style={{ padding: '0', overflow: 'hidden' }}>
          <div style={{ padding: '1.5rem', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
            <h3 style={{ margin: 0 }}>Empty Bottle Inventory</h3>
          </div>
          <table className="data-table">
            <thead>
              <tr>
                <th>Beverage Type</th>
                <th>Quantity</th>
              </tr>
            </thead>
            <tbody>
              {empties.map((item, idx) => (
                <tr key={idx}>
                  <td>{item.beverageType}</td>
                  <td>{item.quantity} units</td>
                </tr>
              ))}
              {empties.length === 0 && (
                <tr><td colSpan="2" style={{ textAlign: 'center' }}>No empty bottles.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="glass-panel" style={{ padding: '0', overflow: 'hidden' }}>
        <div style={{ padding: '1.5rem', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
          <h3 style={{ margin: 0 }}>Bar Replenishment Requests</h3>
        </div>
        <table className="data-table">
          <thead>
            <tr>
              <th>Request ID</th>
              <th>Bar Name</th>
              <th>Beverage Type</th>
              <th>Quantity</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {requests.map(req => (
              <tr key={req.id}>
                <td>#{req.id}</td>
                <td>{req.barName}</td>
                <td>{req.beverageType}</td>
                <td>{req.requestedQuantity}</td>
                <td>
                  <span className={`badge ${req.status === 'PENDING' ? 'warning' : req.status === 'FULFILLED' ? 'success' : 'danger'}`}>
                    {req.status}
                  </span>
                </td>
                <td>
                  {req.status === 'PENDING' && (
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button onClick={() => handleFulfill(req.id)} className="btn" style={{ background: 'transparent', padding: '0.25rem', color: 'var(--success)' }} title="Fulfill">
                        <Check size={18} />
                      </button>
                      <button onClick={() => handleReject(req.id)} className="btn" style={{ background: 'transparent', padding: '0.25rem', color: 'var(--danger)' }} title="Reject">
                        <X size={18} />
                      </button>
                    </div>
                  )}
                </td>
              </tr>
            ))}
            {requests.length === 0 && (
              <tr><td colSpan="6" style={{ textAlign: 'center' }}>No requests pending.</td></tr>
            )}
          </tbody>
        </table>
      </div>

    </div>
  );
}
