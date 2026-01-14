import React from 'react';

export default function Feedbacks({ setView, filter, feedbacks = [] }) {
  // normalize lists
  const all = (feedbacks || []).slice().sort((a,b)=> new Date(b.date) - new Date(a.date));

  if (filter === 'mixed') {
    const pos = all.filter(f => f.sentiment === 'pos').slice(0,10);
    const neg = all.filter(f => f.sentiment === 'neg').slice(0,10);
    return (
      <div className="page-container">
        <div className="modal-panel glass">
          <h2 style={{ textAlign: 'center', marginBottom: '1rem' }}>Últimos 20 feedbacks — 10 positivos e 10 negativos</h2>

          <h4 style={{ color: '#60a5fa', marginBottom: '0.5rem' }}>Positivos (últimos 10)</h4>
          <ul style={{ color: '#e6eefc', marginBottom: '0.75rem' }}>
            {pos.map(f => (
              <li key={`pos-${f.id}`} style={{ marginBottom: '0.6rem' }}>{f.text} <span style={{ color:'#9ca3af', marginLeft: '0.5rem' }}>({f.date})</span></li>
            ))}
          </ul>

          <h4 style={{ color: '#ef4444', marginBottom: '0.5rem' }}>Negativos (últimos 10)</h4>
          <ul style={{ color: '#e6eefc' }}>
            {neg.map(f => (
              <li key={`neg-${f.id}`} style={{ marginBottom: '0.6rem' }}>{f.text} <span style={{ color:'#9ca3af', marginLeft: '0.5rem' }}>({f.date})</span></li>
            ))}
          </ul>

          <div style={{ marginTop: '1rem' }}>
            <button className="home-card-btn secondary" onClick={() => setView?.('dashboard')}>Voltar ao Painel</button>
          </div>
        </div>
      </div>
    );
  }

  // single sentiment view (pos or neg)
  const list = all.filter(f => {
    if (!filter) return true;
    return filter === 'pos' ? f.sentiment === 'pos' : f.sentiment === 'neg';
  }).slice(0,10);

  return (
    <div className="page-container">
      <div className="modal-panel glass">
        <h2 style={{ textAlign: 'center', marginBottom: '1rem' }}>{filter === 'pos' ? 'Últimos feedbacks positivos' : 'Últimos feedbacks negativos'}</h2>

        <ul style={{ color: '#e6eefc' }}>
          {list.map(f => (
            <li key={f.id} style={{ marginBottom: '0.6rem' }}>{f.text} <span style={{ color:'#9ca3af', marginLeft: '0.5rem' }}>({f.date})</span></li>
          ))}
        </ul>

        <div style={{ marginTop: '1rem' }}>
          <button className="home-card-btn secondary" onClick={() => setView?.('dashboard')}>Voltar ao Painel</button>
        </div>
      </div>
    </div>
  );
}
