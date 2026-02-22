import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "../styles/VeicoliCaselli.css";

interface Biglietto {
    targa: string;
    // puoi aggiungere altri campi se ti servono
}

export default function VeicoliCaselli() {
    const { cap, id, direzione } = useParams<{ cap: string; id: string; direzione: string }>();
    const navigate = useNavigate();

    const [targhe, setTarghe] = useState<string[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const idNumber = Number(id);
        if (isNaN(idNumber)) {
            setError("id non è un numero valido");
            setLoading(false);
            return;
        }


        if (!cap || !id || !direzione) {
            setError("Parametri mancanti");
            setLoading(false);
            return;
        }

        setLoading(true);
        setError(null);

        fetch(`/api/veicoli/caselli/${cap}/${idNumber}/${direzione}`)
            .then((res) => {
                if (!res.ok) throw new Error(`Errore nella richiesta: ${res.status}`);
                return res.json();
            })
            .then((data: Biglietto[]) => {
                console.log("Dati ricevuti:", data);
                setTarghe(data.map((b) => b.targa));
            })
            .catch((err) => {
                console.error("Errore:", err);
                setError(err.message || "Errore sconosciuto");
            })
            .finally(() => setLoading(false));
    }, [cap, id, direzione]);

    return (
        <div className="veicoli-container">
            <button className="back-btn" onClick={() => navigate(-1)}>
                ⬅ Torna indietro
            </button>
            <h2>🚗 Veicoli in Transito</h2>

            {loading && <p className="loading">Caricamento...</p>}

            {error && <p className="error">❌ {error}</p>}

            {!loading && !error && targhe.length === 0 && <p className="no-data">Nessun veicolo trovato</p>}

            {!loading && !error && targhe.length > 0 && (
                <ul className="targhe-list">
                    {targhe.map((targa, index) => (
                        <li key={index} className="targa-item">
                            {targa}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}
