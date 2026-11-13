import { useState, useEffect } from 'react';
import axios from 'axios';
import { Beer, MapPin, ShoppingCart } from 'lucide-react';

export default function Bars() {
  const [bars, setBars] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchBars = async () => {
    setLoading(true);
    setError(null);
    try {
      // Assuming bar-management runs on 8083 and uses UUID for X-User-Id
      const userId = localStorage.getItem('userId') || '00000000-0000-0000-0000-000000000000';
      const response = await axios.get('http://localhost:8083/api/v1/bars', {
        headers: { 'X-User-Id': userId }
      });
      setBars(response.data);
    } catch (err) {
      setError('Cannot connect to Bar Management Service (Port 8083). Please ensure it is running.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBars();
  }, []);

  const handlePurchase = async (barId, beverageId) => {
    try {
      const userIdStr = localStorage.getItem('userId') || '1';
      const visitorId = parseInt(userIdStr, 10) || 1;
      
      // Call customer-service (port 8086)
      await axios.post('http://localhost:8086/api/v1/customer/orders', {
        visitorId: visitorId,
        barId: barId,
        items: [{
          beverageTypeId: beverageId,
          quantity: 1
        }]
      });
      alert('Purchase successful!');
      fetchBars(); // refresh stock
    } catch (err) {
      alert("Purchase failed: " + (err.response?.data?.message || err.message));
    }
  };

  if (loading) {
    return <div style={{ padding: '2rem' }}>Loading Bars Data...</div>;
  }

  if (error) {
    return (
      <div className="animate-fade-in">
        <div className="page-header">
          <h2>Bar Management & POS</h2>
          <p>Local bar inventories and point of sale.</p>
        </div>
        <div className="badge warning" style={{ padding: '1rem', fontSize: '1rem' }}>{error}</div>
      </div>
    );
  }

  return (
    <div className="animate-fade-in">
      <div className="page-header">
        <h2>Bar Management & POS</h2>
        <p>Local bar inventories and point of sale.</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(350px, 1fr))', gap: '2rem' }}>
        {bars.map(bar => (
          <div key={bar.id} className="glass-panel" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <div style={{ padding: '0.75rem', background: 'rgba(99, 102, 241, 0.1)', borderRadius: '12px' }}>
                  <Beer color="var(--primary)" size={20} />
                </div>
                <div>
                  <h3 style={{ margin: 0, fontSize: '1.25rem' }}>{bar.name}</h3>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', color: 'var(--text-muted)', fontSize: '0.875rem', marginTop: '0.25rem' }}>
                    <MapPin size={14} />
                    <span>{bar.location || 'Location Not Set'}</span>
                  </div>
                </div>
              </div>
              <span className={`badge ${bar.active ? 'success' : 'danger'}`}>
                {bar.active ? 'Open' : 'Closed'}
              </span>
            </div>

            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
              {bar.description || 'No description provided.'}
            </p>

            <h4 style={{ marginBottom: '0.75rem', color: 'var(--text-main)', fontSize: '0.875rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Local Stock & POS
            </h4>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', flex: 1 }}>
              {bar.stocks && bar.stocks.length > 0 ? (
                bar.stocks.map(stock => (
                  <div key={stock.beverageTypeId} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem', background: 'rgba(255,255,255,0.03)', borderRadius: '8px' }}>
                    <div>
                      <span style={{ fontWeight: '500', display: 'block' }}>BevID: {stock.beverageTypeId}</span>
                      <span style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>Stock: {stock.quantity} / {stock.maxCapacity}</span>
                    </div>
                    <button 
                      onClick={() => handlePurchase(bar.id, stock.beverageTypeId)}
                      disabled={!bar.active || stock.quantity <= 0}
                      className="btn primary" 
                      style={{ padding: '0.5rem 1rem', display: 'flex', alignItems: 'center', gap: '0.5rem', opacity: (!bar.active || stock.quantity <= 0) ? 0.5 : 1 }}
                    >
                      <ShoppingCart size={16} />
                      Sell 1
                    </button>
                  </div>
                ))
              ) : (
                <div style={{ padding: '1rem', textAlign: 'center', color: 'var(--text-muted)', background: 'rgba(255,255,255,0.02)', borderRadius: '8px' }}>
                  No stock assigned.
                </div>
              )}
            </div>
          </div>
        ))}
        {bars.length === 0 && (
          <div style={{ gridColumn: '1 / -1', padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            No bars found. Planners need to configure bars first.
          </div>
        )}
      </div>
    </div>
  );
}
