// src/pages/TrattaDettagli.tsx
import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "./styles/TrattaDettagli.css";
import { useAuth } from "./context/AuthContext";
import {salvaTrattaInSessione, setIdTratta} from "./utils/SessionManager.ts";
import type {Tratta} from "./types/Tratta.ts";
import type {TrafficData} from "./types/TrafficData.ts";
import Avvisi from "./functions/Avvisi";

// Tipo per una singola infrazione velocità
interface InfrazioneVelocita {
    id: number;
    idBiglietto: number;
    targa: string;
    cap: string;
    idCasello: number;
    direzione: string;
    idTratta: number;
    kmRilevazione: number;
    velocitaRilevata: number;
    velocitaMassima: number;
    importo: number;
    timestamp: string;
}

function TrattaDettagli() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    const [tratta, setTratta] = useState<Tratta | null>(null);
    const [traffico, setTraffico] = useState<TrafficData | null>(null);
    const [infrazioni, setInfrazioni] = useState<InfrazioneVelocita[]>([]);
    const [infrazioniLoading, setInfrazioniLoading] = useState<boolean>(false);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [/**refreshing*/, setRefreshing] = useState<boolean>(false);
    const { username, ruolo } = useAuth();

    const [showModalePrezzo, setShowModalePrezzo] = useState(false);
    const [nuovoPrezzo, setNuovoPrezzo] = useState<number | null>(null);
    const [modificaLoading, setModificaLoading] = useState(false);
    const [modificaMessaggio, setModificaMessaggio] = useState("");

    const [showModaleVelocita, setShowModaleVelocita] = useState(false);
    const [nuovaVelocita, setNuovaVelocita] = useState<number | null>(null);
    const [/*modificaVelocitaLoading*/, setModificaVelocitaLoading] = useState(false);
    const [velocitaMessaggio, setVelocitaMessaggio] = useState("");

    const [showModaleInfrazioni, setShowModaleInfrazioni] = useState(false);

    // ── FETCH TRATTA ──
    const fetchTrattaDettagli = async () => {
        if (!id) return;
        setLoading(true);
        setError(null);
        try {
            const response = await fetch(`/api/tratta/${id}`);
            if (!response.ok) throw new Error(`Errore HTTP: ${response.status} - ${response.statusText}`);
            const data: Tratta = await response.json();
            setTratta(data);
            setIdTratta(data.id);
            salvaTrattaInSessione(data);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Errore nel caricamento della tratta');
            console.error('Errore nel caricamento della tratta:', err);
        } finally {
            setLoading(false);
        }
    };

    // ── FETCH INFRAZIONI DAL BACKEND ──
    // Usa GET /infrazione/velocita/tratta/:idTratta (route aggregata per tratta).
    // Se il backend non ha ancora questa route, aggiungila lato Java oppure
    // chiama per singola targa con GET /infrazione/velocita/:targa.
    const fetchInfrazioni = async (currentTratta: Tratta) => {
        setInfrazioniLoading(true);
        try {
            const res = await fetch(`/infrazione/velocita/tratta/${currentTratta.id}`);
            if (res.ok) {
                const data: InfrazioneVelocita[] = await res.json();
                setInfrazioni(Array.isArray(data) ? data : []);
            } else if (res.status === 404) {
                setInfrazioni([]);
            } else {
                console.warn("Risposta inattesa infrazioni:", res.status);
                setInfrazioni([]);
            }
        } catch (err) {
            console.error("Errore fetch infrazioni:", err);
            setInfrazioni([]);
        } finally {
            setInfrazioniLoading(false);
        }
    };

    // ── FETCH TRAFFICO ──
    const fetchTrafficData = async () => {
        if (!id) return;
        setRefreshing(true);
        try {
            const response = await fetch(`/api/incidente/${id}`);
            if (!response.ok) throw new Error(`Errore HTTP: ${response.status} - ${response.statusText}`);
            const data = await response.json();
            let trafficData: TrafficData;

            if (data.idTratta && data.timestamp) {
                let velocitaRidotta = tratta ? tratta.velocitaMax : 130;
                let livelloTraffico: "BASSO" | "MEDIO" | "ALTO" | "BLOCCATO" = 'ALTO';
                switch (data.gravita) {
                    case 'BASSA': velocitaRidotta = tratta ? tratta.velocitaMax * 0.8 : 104; livelloTraffico = 'MEDIO'; break;
                    case 'MEDIA': velocitaRidotta = tratta ? tratta.velocitaMax * 0.6 : 78;  livelloTraffico = 'ALTO';   break;
                    case 'ALTA':  velocitaRidotta = tratta ? tratta.velocitaMax * 0.3 : 39;  livelloTraffico = 'BLOCCATO'; break;
                }
                trafficData = {
                    livelloTraffico,
                    velocitaAttuale: Math.round(velocitaRidotta),
                    totaleGuadagni: tratta ? Math.round((tratta.chilometri / velocitaRidotta) * 60) : 50,
                    incidenti: 1,
                    lavori: false,
                    meteo: 'Sereno',
                    ultimoAggiornamento: new Date().toLocaleString('it-IT'),
                    listaIncidenti: [data]
                };
            } else {
                trafficData = data;
                if (data.incidente && !data.listaIncidenti) trafficData.listaIncidenti = [data.incidente];
            }
            setTraffico(trafficData);
        } catch (err) {
            console.error('Errore nel caricamento traffico:', err);
            const velocitaNormale = tratta ? tratta.velocitaMax * 0.9 : 117;
            setTraffico({
                livelloTraffico: 'BASSO',
                velocitaAttuale: Math.round(velocitaNormale),
                totaleGuadagni: tratta ? Math.round((tratta.chilometri / velocitaNormale) * 60) : 45,
                incidenti: 0,
                lavori: false,
                meteo: 'Sereno',
                ultimoAggiornamento: new Date().toLocaleString('it-IT')
            });
        } finally {
            setRefreshing(false);
        }
    };

    useEffect(() => {
        fetchTrattaDettagli();
        fetchTrafficData();
    }, [id]);

    // Dopo aver caricato la tratta, carica le infrazioni
    useEffect(() => {
        if (tratta) fetchInfrazioni(tratta);
    }, [tratta]);

    // Auto-refresh ogni 30s
    useEffect(() => {
        const interval = setInterval(() => {
            fetchTrafficData();
            if (tratta) fetchInfrazioni(tratta);
        }, 30000);
        return () => clearInterval(interval);
    }, [id, tratta]);

    const handleBack = () => navigate(-1);
    const handleProfileClick = () => navigate("/profilo");
    const handleBackToDashboard = () => navigate("/dashboard");
    const handleBiglietti = () => navigate("/biglietti");

    const handleApriModalePrezzo = () => {
        if (ruolo !== "manager") { alert("❌ Solo i manager possono fare modifiche"); return; }
        if (tratta) { setNuovoPrezzo(tratta.prezzo); setShowModalePrezzo(true); }
    };

    const handleSalvaPrezzo = async () => {
        if (!tratta || nuovoPrezzo === null) return;
        setModificaLoading(true);
        setModificaMessaggio("");
        try {
            const response = await fetch(`/api/tratta/${tratta.id}/modificaPrezzo`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ prezzo: nuovoPrezzo }),
            });
            if (!response.ok) throw new Error(`Errore HTTP: ${response.status}`);
            setTratta({ ...tratta, prezzo: nuovoPrezzo });
            setModificaMessaggio("✅ Prezzo aggiornato con successo!");
            setTimeout(() => { setShowModalePrezzo(false); setModificaMessaggio(""); }, 1500);
        } catch (err) {
            setModificaMessaggio("❌ Errore nel salvataggio");
            console.error(err);
        } finally {
            setModificaLoading(false);
        }
    };

    const handleApriModaleVelocita = () => {
        if (ruolo !== "manager") { alert("❌ Solo i manager possono fare modifiche."); return; }
        if (tratta) { setNuovaVelocita(tratta.velocitaMax); setShowModaleVelocita(true); }
    };

    const handleSalvaVelocita = async () => {
        if (!tratta || nuovaVelocita === null) return;
        setModificaVelocitaLoading(true);
        setVelocitaMessaggio("");
        try {
            const response = await fetch(`/api/tratta/${tratta.id}/modificaVelocita`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ velocitaMax: nuovaVelocita }),
            });
            if (!response.ok) throw new Error(`Errore HTTP: ${response.status}`);
            const trattaAggiornata = { ...tratta, velocitaMax: nuovaVelocita };
            setTratta(trattaAggiornata);
            if (traffico) {
                const velocitaNormale = nuovaVelocita * 0.9;
                if (traffico.listaIncidenti && traffico.listaIncidenti.length > 0) {
                    const inc = traffico.listaIncidenti[0];
                    let vr = nuovaVelocita;
                    let lv: "BASSO" | "MEDIO" | "ALTO" | "BLOCCATO" = 'ALTO';
                    switch (inc.gravita) {
                        case 'BASSA': vr = nuovaVelocita * 0.8; lv = 'MEDIO'; break;
                        case 'MEDIA': vr = nuovaVelocita * 0.6; lv = 'ALTO';   break;
                        case 'ALTA':  vr = nuovaVelocita * 0.3; lv = 'BLOCCATO'; break;
                    }
                    setTraffico({ ...traffico, livelloTraffico: lv, velocitaAttuale: Math.round(vr), totaleGuadagni: Math.round((trattaAggiornata.chilometri / vr) * 60) });
                } else {
                    setTraffico({ ...traffico, velocitaAttuale: Math.round(velocitaNormale), totaleGuadagni: Math.round((trattaAggiornata.chilometri / velocitaNormale) * 60) });
                }
            }
            setVelocitaMessaggio("✅ Velocità massima aggiornata!");
            setTimeout(() => { setShowModaleVelocita(false); setVelocitaMessaggio(""); }, 1500);
        } catch (err) {
            setVelocitaMessaggio("❌ Errore nel salvataggio");
            console.error(err);
        } finally {
            setModificaVelocitaLoading(false);
        }
    };

    // ── HELPERS INFRAZIONI ──
    const getInfrazioniColor = (count: number): string => {
        if (count === 0) return '#22c55e';
        if (count <= 5) return '#f59e0b';
        return '#ef4444';
    };

    const getInfrazioniLabel = (count: number): string => {
        if (count === 0) return 'Nessuna infrazione registrata';
        if (count <= 5) return 'Livello di rischio moderato';
        return 'Alto numero di infrazioni';
    };

    const getEccessoLabel = (rilevata: number, massima: number): string => {
        const delta = rilevata - massima;
        if (delta <= 10) return 'LIEVE';
        if (delta <= 30) return 'MEDIA';
        return 'GRAVE';
    };

    const getEccessoColor = (rilevata: number, massima: number): string => {
        const delta = rilevata - massima;
        if (delta <= 10) return '#f59e0b';
        if (delta <= 30) return '#ef4444';
        return '#7c2d12';
    };

    const getTotaleMulte = (): number =>
        infrazioni.reduce((acc, inf) => acc + inf.importo, 0);

    // ── HELPERS TRAFFICO ──
    const getTrafficoColor = (livello: string) => {
        switch (livello) {
            case 'BASSO': return '#22c55e';
            case 'MEDIO': return '#f59e0b';
            case 'ALTO': return '#ef4444';
            case 'BLOCCATO': return '#7c2d12';
            default: return '#6b7280';
        }
    };

    const getTrafficoIcon = (livello: string) => {
        switch (livello) {
            case 'BASSO': return '🟢';
            case 'MEDIO': return '🟡';
            case 'ALTO': return '🔴';
            case 'BLOCCATO': return '⛔';
            default: return '⚪';
        }
    };

    if (loading) {
        return (
            <div className="tratta-dettagli-wrapper">
                <div className="loading-container">
                    <div className="loading-spinner">
                        <div className="spinner"></div>
                        <p>Caricamento dettagli tratta...</p>
                    </div>
                </div>
            </div>
        );
    }

    if (error || !tratta) {
        return (
            <div className="tratta-dettagli-wrapper">
                <div className="error-container">
                    <div className="error-message">
                        <h3>⚠️ Errore nel caricamento</h3>
                        <p>{error || 'Tratta non trovata'}</p>
                        <button onClick={handleBack} className="back-btn">← Torna indietro</button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="tratta-dettagli-wrapper">

            <div className="navbar">
                <a className="logo" onClick={handleBackToDashboard}>🛣️ Autostrade</a>
                <div className="profile-container" onClick={handleProfileClick}>
                    <div className="profile-icon-wrapper">
                        <div className="profile-icon">👤</div>
                        <span className="username-label">{username ?? "Guest"}</span>
                    </div>
                </div>
            </div>

            <div className="content">

                {/* ── INFO TRATTA ── */}
                <div className="tratta-info-section">
                    <h2>Informazioni Tratta {tratta.id}</h2>
                    <div className="info-grid">
                        <div className="info-card clickable" onClick={handleBiglietti}>
                            <h2>🚗 Auto in circolazione</h2>
                            <h3>📍 Partenza</h3>
                            <p><strong>{tratta.capIn}</strong></p>
                            <p>Casello: {tratta.idCaselloIn}</p>
                            <p>Direzione: {tratta.dirIn}</p>
                            <h3>🏁 Arrivo</h3>
                            <p><strong>{tratta.capOut}</strong></p>
                            <p>Casello: {tratta.idCaselloOut}</p>
                            <p>Direzione: {tratta.dirOut}</p>
                        </div>
                        <div className="info-card clickable" onClick={handleApriModalePrezzo}>
                            <h2>📊 Statistiche tratta</h2>
                            <h3>€ Modifica prezzo</h3>
                            <p>Lunghezza: <strong>{tratta.chilometri} km</strong></p>
                            <p>Prezzo: <strong>€{tratta.prezzo.toFixed(2)}</strong></p>
                            <p>Velocità max: <strong>{tratta.velocitaMax} km/h</strong></p>
                        </div>
                    </div>
                </div>

                {/* ── TRAFFICO ── */}
                {traffico && (
                    <div className="traffico-section">
                        <h2>🚗 Traffico in Tempo Reale</h2>
                        <div className="traffico-overview">
                            <div className="traffico-level" style={{ backgroundColor: getTrafficoColor(traffico.livelloTraffico) }}>
                                <span className="traffico-icon">{getTrafficoIcon(traffico.livelloTraffico)}</span>
                                <div className="traffico-text">
                                    <h3>Traffico {traffico.livelloTraffico}</h3>
                                    <p>Ultimo aggiornamento: {traffico.ultimoAggiornamento}</p>
                                </div>
                            </div>
                        </div>

                        <div className="traffico-details">
                            <div className="detail-card clickable" onClick={handleApriModaleVelocita}>
                                <h4>⛔ Limite velocità</h4>
                                <p className="big-number">{tratta.velocitaMax} km/h</p>
                                <p className="detail-subtitle">
                                    {Math.round((traffico.velocitaAttuale / tratta.velocitaMax) * 100)}% della velocità massima
                                </p>
                            </div>

                            <div className="detail-card">
                                <h4>Totale guadagni</h4>
                                <p className="big-number">{traffico.totaleGuadagni} €</p>
                                <p className="detail-subtitle">Ultimo mese</p>
                            </div>

                            {/* ── CARD INFRAZIONI VELOCITÀ ── */}
                            <div
                                className="detail-card infrazioni-card clickable"
                                onClick={() => infrazioni.length > 0 && setShowModaleInfrazioni(true)}
                                title={infrazioni.length > 0 ? "Clicca per vedere il dettaglio" : ""}
                            >
                                <h4>🚨 Infrazioni Velocità</h4>

                                {infrazioniLoading ? (
                                    <p className="detail-subtitle">Caricamento...</p>
                                ) : (
                                    <>
                                        <p
                                            className="big-number"
                                            style={{ color: getInfrazioniColor(infrazioni.length) }}
                                        >
                                            {infrazioni.length}
                                        </p>
                                        <p className="detail-subtitle">
                                            {getInfrazioniLabel(infrazioni.length)}
                                        </p>

                                        {/* Barra di severità */}
                                        <div className="infrazioni-bar-wrapper">
                                            <div
                                                className="infrazioni-bar-fill"
                                                style={{
                                                    width: `${Math.min((infrazioni.length / 20) * 100, 100)}%`,
                                                    backgroundColor: getInfrazioniColor(infrazioni.length),
                                                }}
                                            />
                                        </div>

                                        <p className="detail-subtitle" style={{ marginTop: "6px" }}>
                                            Multe totali: <strong>€{getTotaleMulte().toFixed(2)}</strong>
                                        </p>

                                        {infrazioni.length > 0 && (
                                            <p className="detail-subtitle infrazioni-hint">
                                                👆 Clicca per i dettagli
                                            </p>
                                        )}
                                    </>
                                )}
                            </div>
                            {/* ── FINE CARD INFRAZIONI ── */}
                        </div>

                        <Avvisi traffico={traffico} />
                    </div>
                )}
            </div>

            {/* ── MODALE PREZZO ── */}
            {showModalePrezzo && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <h2>Modifica Prezzo Tratta</h2>
                        <label>Nuovo prezzo (€):</label>
                        <input
                            type="number" step="0.01" min="0"
                            value={nuovoPrezzo ?? ""}
                            onChange={(e) => setNuovoPrezzo(parseFloat(e.target.value))}
                        />
                        <p className={modificaMessaggio.includes("✅") ? "message-success" : "message-error"}>
                            {modificaMessaggio}
                        </p>
                        <div className="modal-actions">
                            <button onClick={() => setShowModalePrezzo(false)} disabled={modificaLoading}>❌ Annulla</button>
                            <button onClick={handleSalvaPrezzo} disabled={modificaLoading || nuovoPrezzo === null}>
                                {modificaLoading ? "Salvataggio..." : "💾 Salva"}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* ── MODALE VELOCITÀ ── */}
            {showModaleVelocita && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <h2>Modifica Velocità Massima</h2>
                        <label>Nuova velocità (km/h):</label>
                        <input
                            type="number" step="1" min="30" max="200"
                            value={nuovaVelocita ?? ""}
                            onChange={(e) => {
                                const v = e.target.value;
                                setNuovaVelocita(v === "" ? null : parseInt(v));
                            }}
                        />
                        <p className={velocitaMessaggio.includes("✅") ? "message-success" : "message-error"}>
                            {velocitaMessaggio}
                        </p>
                        <div className="modal-actions">
                            <button onClick={() => setShowModaleVelocita(false)}>❌ Annulla</button>
                            <button onClick={handleSalvaVelocita} disabled={nuovaVelocita === null}>💾 Salva</button>
                        </div>
                    </div>
                </div>
            )}

            {/* ── MODALE DETTAGLIO INFRAZIONI ── */}
            {showModaleInfrazioni && (
                <div className="modal-overlay" onClick={() => setShowModaleInfrazioni(false)}>
                    <div
                        className="modal-content modal-infrazioni"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <div className="modal-infrazioni-header">
                            <h2>🚨 Infrazioni Velocità — Tratta {tratta.id}</h2>
                            <div className="infrazioni-summary">
                                <span className="summary-chip">
                                    Totale: <strong>{infrazioni.length}</strong>
                                </span>
                                <span className="summary-chip summary-chip-money">
                                    Multe: <strong>€{getTotaleMulte().toFixed(2)}</strong>
                                </span>
                            </div>
                        </div>

                        {infrazioni.length === 0 ? (
                            <div className="infrazioni-empty">
                                <span>✅</span>
                                <p>Nessuna infrazione registrata per questa tratta.</p>
                            </div>
                        ) : (
                            <div className="infrazioni-table-wrapper">
                                <table className="infrazioni-table">
                                    <thead>
                                        <tr>
                                            <th>Targa</th>
                                            <th>Km</th>
                                            <th>Rilevata</th>
                                            <th>Limite</th>
                                            <th>Eccesso</th>
                                            <th>Gravità</th>
                                            <th>Multa</th>
                                            <th>Data/Ora</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {infrazioni.map((inf) => {
                                            const eccesso = inf.velocitaRilevata - inf.velocitaMassima;
                                            const label = getEccessoLabel(inf.velocitaRilevata, inf.velocitaMassima);
                                            const color = getEccessoColor(inf.velocitaRilevata, inf.velocitaMassima);
                                            return (
                                                <tr key={inf.id}>
                                                    <td className="targa-cell">{inf.targa}</td>
                                                    <td>{inf.kmRilevazione} km</td>
                                                    <td style={{ color: '#ef4444', fontWeight: 600 }}>
                                                        {inf.velocitaRilevata} km/h
                                                    </td>
                                                    <td>{inf.velocitaMassima} km/h</td>
                                                    <td style={{ color: '#ef4444', fontWeight: 600 }}>
                                                        +{eccesso} km/h
                                                    </td>
                                                    <td>
                                                        <span
                                                            className="gravita-badge"
                                                            style={{ backgroundColor: color }}
                                                        >
                                                            {label}
                                                        </span>
                                                    </td>
                                                    <td className="multa-cell">€{inf.importo.toFixed(2)}</td>
                                                    <td className="timestamp-cell">
                                                        {new Date(inf.timestamp).toLocaleString('it-IT')}
                                                    </td>
                                                </tr>
                                            );
                                        })}
                                    </tbody>
                                </table>
                            </div>
                        )}

                        <div className="modal-actions">
                            <button onClick={() => setShowModaleInfrazioni(false)}>✖ Chiudi</button>
                        </div>
                    </div>
                </div>
            )}

            <footer className="footer">
                &copy; 2025 Autostrade S.p.A. - Fondata da Klejdi Tahiri
            </footer>
        </div>
    );
}

export default TrattaDettagli;