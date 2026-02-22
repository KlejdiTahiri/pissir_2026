// src/pages/DettagliCasello.tsx
import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "./styles/DettagliCasello.css";
import {useAuth} from "./context/AuthContext.tsx";
import Toast from "./context/Toast.tsx";
import type {Casello} from "./types/Casello.ts";


function DettagliCasello() {
    const navigate = useNavigate();
    const { cap, id, direzione } = useParams<{
        cap: string;
        id: string;
        direzione: string;
    }>();

    const [casello, setCasello] = useState<Casello | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [isEditing, setIsEditing] = useState<boolean>(false);
    const [editData, setEditData] = useState<{stato: boolean}>({stato: false});
    const [saveLoading, setSaveLoading] = useState<boolean>(false);
    const [saveMessage, setSaveMessage] = useState<string>("");
    const { username } = useAuth();

    // Fetch dei dettagli del casello
    const fetchCaselloDetails = async () => {
        if (!cap || !id || !direzione) {
            setError("Parametri mancanti");
            setLoading(false);
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`/api/caselli/${encodeURIComponent(cap)}/${id}/${encodeURIComponent(direzione)}`);

            if (!response.ok) {
                if (response.status === 404) {
                    throw new Error("Casello non trovato");
                }
                throw new Error(`Errore HTTP: ${response.status} - ${response.statusText}`);
            }

            const data: Casello = await response.json();
            setCasello(data);
            setEditData({stato: data.stato});
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Errore sconosciuto');
            console.error('Errore nel caricamento del casello:', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCaselloDetails();
    }, [cap, id, direzione]);

    const handleBackToDashboard = () => {
        navigate("/dashboard");
    };

    const handleEditToggle = () => {
        if (isEditing) {
            // Annulla modifiche
            if (casello) {
                setEditData({stato: casello.stato});
            }
        }
        setIsEditing(!isEditing);
        setSaveMessage("");
    };

    const handleVisualizzaVeicoli = () => {
        if (cap && id && direzione) {
            navigate(`/veicoli/caselli/${cap}/${id}/${direzione}`);
        }
    };

    const handleEliminaCasello = async () => {
        const messaggio = "Sicuro di voler eliminare il casello ?";

        const conferma = window.confirm(messaggio);

        if (conferma) {
            try {
                const response = await fetch(`/api/caselli/${cap}/${id}/${direzione}/elimina`, {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json',
                    }
                });

                if (!response.ok) {
                    throw new Error(`Errore nell'eliminazione: ${response.status}`);
                }

                // Mostra messaggio per 1.5 secondi e poi naviga
                setSaveMessage("✅ Casello eliminato con successo. Ritorno alla dashboard...");
                setTimeout(() => {
                    setSaveMessage("");  // Pulisce eventuali messaggi futuri
                    navigate("/dashboard");
                }, 1500);

            } catch (error) {
                console.error("Errore nell'eliminazione del casello:", error);
                setSaveMessage("❌ Errore nell'eliminazione del casello");
            }

        } else {
            console.log("Operazione annullata");
        }
    }


    const handleSospendiCasello = async () => {
        const messaggio = casello?.stato
            ? "Le tratte con questo casello verranno chiuse. Sei sicuro di voler sospendere il casello?"
            : "Il casello e le relative tratte verranno riaperte. Sei sicuro di voler procedere?";

        const conferma = window.confirm(messaggio);


        if (conferma) {
            try {
                const response = await fetch(`/api/caselli/${cap}/${id}/${direzione}/stato`, {
                    method: 'PATCH',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        stato: editData.stato
                    }),
                });

                if (!response.ok) {
                    throw new Error(`Errore nel salvataggio: ${response.status}`);
                }

                // Aggiorna dati locali con lo stato invertito, perché il backend lo inverte
                const nuovoStato = !editData.stato;

                setCasello(prev => prev ? {...prev, stato: nuovoStato} : null);
                setEditData({stato: nuovoStato});
                setIsEditing(false);
                setSaveMessage("Modifiche salvate con successo!");

                setTimeout(() => setSaveMessage(""), 3000);

            } catch (error) {
                console.error("Errore nel sospendere il casello:", error);
                setSaveMessage("Errore nel salvataggio delle modifiche");
            }
        } else {
            console.log("Operazione annullata");
        }
    };



    const handleSaveChanges = async () => {
        if (!casello) return;

        setSaveLoading(true);
        setSaveMessage("");

        try {
            const response = await fetch(`/api/caselli/${encodeURIComponent(casello.cap)}/${casello.idCasello}/${encodeURIComponent(casello.direzione)}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    stato: editData.stato
                })
            });

            if (!response.ok) {
                throw new Error(`Errore nel salvataggio: ${response.status}`);
            }

            // Aggiorna i dati locali
            setCasello(prev => prev ? {...prev, stato: editData.stato} : null);
            setIsEditing(false);
            setSaveMessage("Modifiche salvate con successo!");

            // Rimuovi il messaggio dopo 3 secondi
            setTimeout(() => setSaveMessage(""), 3000);

        } catch (err) {
            setSaveMessage("Errore nel salvataggio delle modifiche");
            console.error('Errore nel salvataggio:', err);
        } finally {
            setSaveLoading(false);
        }
    };

    const getStatusInfo = (stato: boolean) => {
        return stato
            ? { text: 'Attivo', class: 'active', icon: '✅' }
            : { text: 'Inattivo', class: 'inactive', icon: '🔴' };
    };

    const formatCaselloTitle = (cap: string, id: number, direzione: string) => {
        return `${cap} - Casello ${id} (${direzione})`;
    };

    const handleProfileClick = () => {
        navigate("/profilo");
    };


    if (loading) {
        return (
            <div className="casello-details-wrapper">
                <div className="navbar">
                    <a className="logo" onClick={handleBackToDashboard}>
                        🛣️ Autostrade
                    </a>
                    <div className="profile-container" onClick={handleProfileClick}>
                        <div className="profile-icon-wrapper">
                            <div className="profile-icon">👤</div>
                            <span className="username-label">
                            {username ? username : "Guest"}
                        </span>
                        </div>
                    </div>
                </div>
                <div className="loading-container">
                    <div className="loading-spinner">
                        <div className="spinner"></div>
                        <p>Caricamento dettagli casello...</p>
                    </div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="casello-details-wrapper">
                <div className="navbar">
                    <a className="logo" onClick={handleBackToDashboard}>
                        🛣️ Autostrade
                    </a>
                    <div className="profile-container" onClick={handleProfileClick}>
                        <div className="profile-icon-wrapper">
                            <div className="profile-icon">👤</div>
                            <span className="username-label">
                            {username ? username : "Guest"}
                        </span>
                        </div>
                    </div>
                </div>
                <div className="error-container">
                    <div className="error-message">
                        <h3>⚠️ Errore</h3>
                        <p>{error}</p>
                        <button onClick={fetchCaselloDetails} className="retry-btn">
                            Riprova
                        </button>
                        <button onClick={handleBackToDashboard} className="back-to-dashboard-btn">
                            Torna alla Dashboard
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    if (!casello) {
        return (
            <div className="casello-details-wrapper">
                <div className="navbar">
                    <a className="logo" onClick={handleBackToDashboard}>
                        🛣️ Autostrade
                    </a>
                    <div className="profile-container" onClick={handleProfileClick}>
                        <div className="profile-icon-wrapper">
                            <div className="profile-icon">👤</div>
                            <span className="username-label">
                            {username ? username : "Guest"}
                        </span>
                        </div>
                    </div>
                </div>
                <div className="not-found-container">
                    <h3>Casello non trovato</h3>
                    <button onClick={handleBackToDashboard} className="back-to-dashboard-btn">
                        Torna alla Dashboard
                    </button>
                </div>
            </div>
        );
    }

    const statusInfo = getStatusInfo(isEditing ? editData.stato : casello.stato);

    return (
        <div className="casello-details-wrapper">
            <div className="navbar">
                <a className="logo" onClick={handleBackToDashboard}>
                    🛣️ Autostrade
                </a>
                <div className="profile-container" onClick={handleProfileClick}>
                    <div className="profile-icon-wrapper">
                        <div className="profile-icon">👤</div>
                        <span className="username-label">
                            {username ? username : "Guest"}
                        </span>
                    </div>
                </div>
            </div>

            <div className="container">
                <div className="details-header">
                    <h1>{formatCaselloTitle(casello.cap, casello.idCasello, casello.direzione)}</h1>
                    <div className="header-actions">
                        <button
                            onClick={handleEditToggle}
                            className={`edit-btn ${isEditing ? 'cancel' : 'edit'}`}
                            disabled={saveLoading}
                        >
                            {isEditing ? '❌ Annulla' : '✏️ Modifica'}
                        </button>
                        {isEditing && (
                            <button
                                onClick={handleSaveChanges}
                                className="save-btn"
                                disabled={saveLoading}
                            >
                                {saveLoading ? (
                                    <>
                                        <span className="spinner small"></span>
                                        Salvataggio...
                                    </>
                                ) : (
                                    '💾 Salva'
                                )}
                            </button>
                        )}
                    </div>
                </div>

                {saveMessage && (
                    <Toast
                        message={saveMessage}
                        type={saveMessage.includes("errore") || saveMessage.includes("Errore") ? "error" : "success"}
                        onClose={() => setSaveMessage("")}
                    />
                )}


                <div className="details-card">
                <div className="detail-row">
                        <label>Codice Postale (CAP)</label>
                        <span className="detail-value">{casello.cap}</span>
                    </div>

                    <div className="detail-row">
                        <label>ID Casello</label>
                        <span className="detail-value">{casello.idCasello}</span>
                    </div>

                    <div className="detail-row">
                        <label>Direzione</label>
                        <span className="detail-value">{casello.direzione}</span>
                    </div>

                    <div className="detail-row">
                        <label>Stato</label>
                        {isEditing ? (
                            <div className="status-edit">
                                <label className="radio-option">
                                    <input
                                        type="radio"
                                        name="stato"
                                        value="true"
                                        checked={editData.stato}
                                        onChange={() => setEditData({stato: true})}
                                    />
                                    <span className="radio-label active">✅ Attivo</span>
                                </label>
                                <label className="radio-option">
                                    <input
                                        type="radio"
                                        name="stato"
                                        value="false"
                                        checked={!editData.stato}
                                        onChange={() => setEditData({stato: false})}
                                    />
                                    <span className="radio-label inactive">🔴 Inattivo</span>
                                </label>
                            </div>
                        ) : (
                            <span className={`status-badge ${statusInfo.class}`}>
                                {statusInfo.icon} {statusInfo.text}
                            </span>
                        )}
                    </div>
                </div>

                <div className="actions-section">
                    <h3>Azioni Disponibili</h3>
                    <div className="action-buttons">

                        {casello.stato ? (
                            <button className="action-button maintenance" onClick={handleSospendiCasello}>
                                🔧 Sospendi per manutenzione
                            </button>
                        ) : (
                            <button className="action-button reopen" onClick={handleSospendiCasello}>
                                ✅ Riapri casello
                            </button>
                        )}

                        <button className="action-button elimina" onClick={handleEliminaCasello}>
                            ❌ Elimina Casello
                        </button>
                        <button
                            className="action-button refresh"
                            onClick={fetchCaselloDetails}
                        >
                            🔄 Aggiorna Dati
                        </button>
                    </div>
                </div>
            </div>

            <footer className="footer">
                &copy; 2025 Autostrade S.p.A. - Fondata Klejdi Tahiri
            </footer>
        </div>
    );
}

export default DettagliCasello;