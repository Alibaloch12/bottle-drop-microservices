import { useState, useEffect } from 'react';
import axios from 'axios';
import { Trash2, AlertTriangle, Check, ArchiveRestore } from 'lucide-react';

export default function DropPoints() {
  const [dropPoints, setDropPoints] = useState([]);
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      // Assuming drop point service runs on 8085
      const [dpRes, repRes] = await Promise.all([
        axios.get('http://localhost:8085/drop-points'),
        axios.get('http://localhost:8085/drop-points/reports')
      ]);
      setDropPoints(dpRes.data);
      setReports(repRes.data);
    } catch (err) {
      setError('Cannot connect to Bottle Drop Point Service (Port 8085). Please ensure it is running.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleReturn = async (id, quantity) => {
    try {
      await axios.post(`http://localhost:8085/drop-points/${id}/return?quantity=${quantity}`);
      alert('Return successful!');
      fetchData();
    } catch (err) {
      alert("Return failed: " + (err.response?.data?.message || err.message));
    }
  };

  const handleReportFull = async (id) => {
    try {
      await axios.post(`http://localhost:8085/drop-points/${id}/report`);
      alert('Reported as full!');
      fetchData();
    } catch (err) {
      alert("Report failed: " + (err.response?.data?.message || err.message));
    }
  };

  const handleEmpty = async (id) => {
    try {
      await axios.post(`http://localhost:8085/drop-points/${id}/empty`);
      alert('Drop point emptied and bottles transferred!');
      fetchData();
    } catch (err) {
      alert("Emptying failed: " + (err.response?.data?.message || err.message));
    }
  };

  const handleResolveReport = async (id) => {
    try {
      await axios.post(`http://localhost:8085/drop-points/reports/${id}/resolve`);
      fetchData();
    } catch (err) {
      alert("Resolve failed: " + (err.response?.data?.message || err.message));
    }
  };

  if (loading) {
    return <div style={{ padding: '2rem' }}>Loading Drop Points Data...</div>;
  }

  if (error) {
    return (
      <div className="animate-fade-in">
        <div className="page-header">
          <h2>Drop Points Management</h2>
          <p>Monitor drop points, return bottles, and manage full reports.</p>
        </div>
        <div className="badge warning" style={{ padding: '1rem', fontSize: '1rem' }}>{error}</div>
      </div>
    );
  }

  return (
    <div className="animate-fade-in">
      <div className="page-header">
        <h2>Drop Points Management</h2>
        <p>Monitor drop points, return bottles, and manage full reports.</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginBottom: '2.5rem' }}>
        <div className="glass-panel" style={{ padding: '0', overflow: 'hidden' }}>
          <div style={{ padding: '1.5rem', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
            <h3 style={{ margin: 0, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Trash2 size={20} /> Active Drop Points
            </h3>
          </div>
          <table className="data-table">
            <thead>
              <tr>
                <th>Location</th>
                <th>Capacity</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {dropPoints.map(dp => {
                const isFull = dp.bottleCount >= dp.maxCapacity;
                const percentage = Math.round((dp.bottleCount / dp.maxCapacity) * 100);
                return (
                  <tr key={dp.id}>
                    <td>{dp.location}</td>
                    <td>
                      <div style={{ fontSize: '0.875rem', marginBottom: '0.25rem' }}>{dp.bottleCount} / {dp.maxCapacity} ({percentage}%)</div>
                      <div style={{ width: '100%', height: '4px', background: 'rgba(255,255,255,0.1)', borderRadius: '2px' }}>
                        <div style={{ width: `${Math.min(percentage, 100)}%`, height: '100%', background: isFull ? 'var(--danger)' : 'var(--primary)', borderRadius: '2px' }} />
                      </div>
                    </td>
                    <td>
                      <span className={`badge ${isFull ? 'danger' : percentage > 80 ? 'warning' : 'success'}`}>
                        {isFull ? 'Full' : 'Active'}
                      </span>
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                        <button onClick={() => handleReturn(dp.id, 1)} disabled={isFull} className="btn" style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}>
                          Return 1
                        </button>
                        <button onClick={() => handleReportFull(dp.id)} disabled={isFull} className="btn warning" style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }} title="Report Full">
                          <AlertTriangle size={14} />
                        </button>
                        <button onClick={() => handleEmpty(dp.id)} className="btn primary" style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }} title="Empty Bin">
                          <ArchiveRestore size={14} />
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
              {dropPoints.length === 0 && (
                <tr><td colSpan="4" style={{ textAlign: 'center' }}>No drop points configured.</td></tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="glass-panel" style={{ padding: '0', overflow: 'hidden' }}>
          <div style={{ padding: '1.5rem', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
            <h3 style={{ margin: 0, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <AlertTriangle size={20} color="var(--warning)" /> Full Bin Reports
            </h3>
          </div>
          <table className="data-table">
            <thead>
              <tr>
                <th>Drop Point ID</th>
                <th>Reported At</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {reports.map(rep => (
                <tr key={rep.id}>
                  <td>#{rep.dropPointId}</td>
                  <td>{new Date(rep.createdAt).toLocaleString()}</td>
                  <td>
                    <span className={`badge ${rep.resolved ? 'success' : 'warning'}`}>
                      {rep.resolved ? 'Resolved' : 'Pending'}
                    </span>
                  </td>
                  <td>
                    {!rep.resolved && (
                      <button onClick={() => handleResolveReport(rep.id)} className="btn primary" style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                        <Check size={14} /> Resolve
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {reports.length === 0 && (
                <tr><td colSpan="4" style={{ textAlign: 'center' }}>No reports submitted.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
