// src/pages/AggiungiCaselloForm.tsx
import { useState } from 'react';
import '../styles/AggiungiCaselloForm.css';

interface AggiungiCaselloFormProps {
    onClose: () => void;
}

const capsItalia = ['MI', 'RM', 'FI', 'NA', 'TO', 'BO'];
const direzioni = ['ingresso', 'uscita'];

function AggiungiCaselloForm({ onClose }: AggiungiCaselloFormProps) {
    const [cap, setCap] = useState<string>('MI');
    const [direzione, setDirezione] = useState<string>('ingresso');
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<boolean>(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setSuccess(false);

        const payload = {
            cap,
            direzione,
        };

        try {
            const response = await fetch('/api/aggiungiCasello', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                throw new Error(`Errore HTTP: ${response.status}`);
            }

            setSuccess(true);

            setTimeout(() => {
                setSuccess(false);
                onClose(); // chiude il modale
            }, 1500);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Errore durante l’invio');
        } finally {
            setLoading(false);
        }
    };


    return (
        <div className="form-wrapper">
            <h2>Aggiungi Casello</h2>
            <form onSubmit={handleSubmit} className="casello-form">
                <label>
                    CAP
                    <select value={cap} onChange={(e) => setCap(e.target.value)}>
                        {capsItalia.map((c) => (
                            <option key={c} value={c}>{c}</option>
                        ))}
                    </select>
                </label>

                <label>
                    Direzione
                    <select value={direzione} onChange={(e) => setDirezione(e.target.value)}>
                        {direzioni.map((d) => (
                            <option key={d} value={d}>{d}</option>
                        ))}
                    </select>
                </label>

                <button type="submit" disabled={loading}>
                    {loading ? 'Invio in corso...' : 'Aggiungi Casello'}
                </button>

                {loading && <div className="spinner" />}
                {error && <p className="error-msg">❌ {error}</p>}
                {success && <p className="success-msg">✅ Casello aggiunto con successo!</p>}
            </form>
        </div>
    );
}

export default AggiungiCaselloForm;
