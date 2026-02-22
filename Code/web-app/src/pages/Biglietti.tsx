// src/pages/Biglietti.tsx
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./styles/Biglietti.css";
import { useAuth } from "./context/AuthContext";
import type { Biglietto } from "./types/Biglietto.ts";
import type { StoricoConPagamento } from "./types/StoricoConPagamento";
import { caricaTrattaDaSessione, getIdTratta } from "./utils/SessionManager.ts";



function Biglietti() {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState<'validi' | 'tutti'>('validi');
    const { username, setUsernameSession } = useAuth();

    // Stati separati per i due tipi di biglietti
    const [bigliettiValidi, setBigliettiValidi] = useState<Biglietto[]>([]);
    const [bigliettiPagati, setBigliettiPagati] = useState<StoricoConPagamento[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState<string>('');

    // Fetch dei biglietti validi (emessi)
    const fetchBigliettiValidi = async (id: string, capIn: string, dirIn: string) => {
        setLoading(true);
        setError(null);

        try {
            const queryParams = new URLSearchParams({
                idCasello: id.toString(),
                capIn: capIn,
                dirIn: dirIn,
            });

            const response = await fetch(`/api/biglietti?${queryParams.toString()}`);

            if (!response.ok) {
                throw new Error(`Errore HTTP: ${response.status} - ${response.statusText}`);
            }

            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
                const text = await response.text();
                throw new Error(`Risposta non JSON ricevuta: ${text.substring(0, 100)}...`);
            }

            const data: Biglietto[] = await response.json();
            setBigliettiValidi(data);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Errore sconosciuto');
            console.error('Errore nel caricamento dei biglietti:', err);
        } finally {
            setLoading(false);
        }
    };

    // (Facoltativo) Fetch dei biglietti pagati
    const fetchBigliettiPagati = async (idIn: string, capIn: string, dirIn: string,
                                        idOut: string, capOut: string, dirOut: string) => {
        setLoading(true);
        setError(null);

        try {
            const queryParams = new URLSearchParams({
                idCaselloIn: idIn.toString(),
                capIn: capIn,
                dirIn: dirIn,
                idCaselloOut: idOut.toString(),
                capOut: capOut,
                dirOut: dirOut,
            });
            const response = await fetch(`/api/biglietti_pagati?${queryParams.toString()}`);

            if (!response.ok) {
                throw new Error(`Errore HTTP: ${response.status} - ${response.statusText}`);
            }

            const data: StoricoConPagamento[] = await response.json();
            setBigliettiPagati(data);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Errore sconosciuto');
            console.error('Errore nel caricamento dei biglietti pagati:', err);
        } finally {
            setLoading(false);
        }
    };

    // Caricamento dei biglietti emessi (validi) da tratta in sessione
    useEffect(() => {
        const trattaId = getIdTratta();
        const tratta = trattaId ? caricaTrattaDaSessione(trattaId) : null;

        if (!tratta) return;

        fetchBigliettiValidi(tratta.idCaselloIn, tratta.capIn, tratta.dirIn);
    }, []);

    // Caricamento sessione utente
    useEffect(() => {
        fetch("/api/sessione", { credentials: "include" })
            .then(res => res.json())
            .then(data => {
                if (data.logged) {
                    setUsernameSession(data.username);
                } else {
                    navigate("/login");
                }
            })
            .catch(err => {
                console.error("Errore nel recupero sessione:", err);
                navigate("/login");
            });
    }, [navigate, setUsernameSession]);

    // Fetch biglietti pagati quando si passa al tab "tutti"
    useEffect(() => {
        if (activeTab === 'tutti' && bigliettiPagati.length === 0) {
            const trattaId = getIdTratta();
            const tratta = trattaId ? caricaTrattaDaSessione(trattaId) : null;

            if (!tratta) return;
            fetchBigliettiPagati(tratta.idCaselloIn, tratta.capIn, tratta.dirIn, tratta.idCaselloOut, tratta.capOut, tratta.dirOut);
        }
    }, [activeTab]);

    /**useEffect(() => {
     const handleBeforeUnload = () => {
     const trattaId = getIdTratta();
     if (trattaId !== null) {
     clearIdTratta();
     cancellaTrattaDaSessione(trattaId);
     }
     };

     window.addEventListener("beforeunload", handleBeforeUnload);

     return () => {
     window.removeEventListener("beforeunload", handleBeforeUnload);
     };
     }, []);**/


    const handleBackToDashboard = () => {
        navigate("/dashboard");
    };

    const handleProfileClick = () => {
        navigate("/profilo");
    };

    const switchTab = (tab: 'validi' | 'tutti') => {
        setActiveTab(tab);
    };

    // Funzioni di filtraggio separate per i due tipi di biglietti
    const filterBigliettiValidi = (biglietti: Biglietto[]) => {
        return biglietti.filter(biglietto => {
            return searchTerm === '' ||
                biglietto.id.toString().includes(searchTerm) ||
                biglietto.targa.toLowerCase().includes(searchTerm.toLowerCase()) ||
                biglietto.capIngresso.toLowerCase().includes(searchTerm.toLowerCase()) ||
                biglietto.idCaselloIngresso.toString().includes(searchTerm);
        });
    };

    const filterBigliettiPagati = (biglietti: StoricoConPagamento[]) => {
        return biglietti.filter(biglietto => {
            return searchTerm === '' ||
                biglietto.id.toString().includes(searchTerm) ||
                biglietto.targa.toLowerCase().includes(searchTerm.toLowerCase()) ||
                biglietto.capUscita.toLowerCase().includes(searchTerm.toLowerCase()) ||
                biglietto.idCaselloUscita.toString().includes(searchTerm);
        });
    };

    // Determina quale dataset usare e applica il filtro appropriato
    const filteredBiglietti = activeTab === 'validi'
        ? filterBigliettiValidi(bigliettiValidi)
        : filterBigliettiPagati(bigliettiPagati);

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('it-IT', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const getStatusClass = (item: Biglietto | StoricoConPagamento) => {
        if (activeTab === 'validi') {
            const biglietto = item as Biglietto;
            return biglietto.valido ? 'status valid' : 'status invalid';
        } else {
            const pagato = item as StoricoConPagamento;
            return pagato.statoPagamento === 1 ? 'status paid' : 'status unpaid';
        }
    };

    const getStatusText = (item: Biglietto | StoricoConPagamento) => {
        if (activeTab === 'validi') {
            const biglietto = item as Biglietto;
            return biglietto.valido ? 'Valido' : 'Non Valido';
        } else {
            const pagato = item as StoricoConPagamento;
            return pagato.statoPagamento === 1 ? 'Pagato' : 'Non Pagato';
        }
    };

    const formatPrice = (price: number) => {
        return new Intl.NumberFormat('it-IT', {
            style: 'currency',
            currency: 'EUR'
        }).format(price);
    };

    const renderBigliettiContent = () => {
        if (loading) {
            return (
                <div className="tab-content loading">
                    <div className="loading-spinner">
                        <div className="spinner"></div>
                        <p>Caricamento biglietti in corso...</p>
                    </div>
                </div>
            );
        }

        if (error) {
            return (
                <div className="tab-content error">
                    <div className="error-message">
                        <h3>⚠️ Errore nel caricamento</h3>
                        <p>{error}</p>
                    </div>
                </div>
            );
        }

        const datiCorrenti = activeTab === 'validi' ? bigliettiValidi : bigliettiPagati;

        if (datiCorrenti.length === 0) {
            return (
                <div className="tab-content empty">
                    <div className="empty-state">
                        <h3>Nessun biglietto disponibile</h3>
                        <p>Non ci sono biglietti da mostrare in questo tab.</p>
                    </div>
                </div>
            );
        }

        return (
            <div className="tab-content">
                <div className="content-header">
                    <h2>{activeTab === 'validi' ? 'Biglietti Emessi' : 'Biglietti Pagati'}</h2>
                    <p>{activeTab === 'validi'
                        ? 'Visualizza tutti i biglietti di ingresso validi'
                        : 'Visualizza tutti i biglietti che sono stati pagati'}</p>
                </div>

                <div className="biglietti-controls">
                    <div className="search-filter">
                        <input
                            type="text"
                            placeholder={activeTab === 'validi'
                                ? "Cerca per ID, targa, CAP ingresso o casello..."
                                : "Cerca per ID, targa, CAP uscita o casello..."
                            }
                            className="search-input"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>
                </div>

                <div className="biglietti-stats">
                    <div className="stat-item">
                        <span className="stat-number">{datiCorrenti.length}</span>
                        <span className="stat-label">Totale Biglietti</span>
                    </div>
                    {activeTab === 'validi' ? (
                        <>
                            <div className="stat-item">
                                <span className="stat-number">{bigliettiValidi.filter(b => b.valido).length}</span>
                                <span className="stat-label">Validi</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-number">{bigliettiValidi.filter(b => !b.valido).length}</span>
                                <span className="stat-label">Non Validi</span>
                            </div>
                        </>
                    ) : (
                        <>
                            <div className="stat-item">
                                <span className="stat-number">{bigliettiPagati.filter(b => b.statoPagamento === 1).length}</span>
                                <span className="stat-label">Pagati</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-number">{bigliettiPagati.filter(b => b.statoPagamento !== 1).length}</span>
                                <span className="stat-label">Non Pagati</span>
                            </div>
                        </>
                    )}
                    <div className="stat-item">
                        <span className="stat-number">{filteredBiglietti.length}</span>
                        <span className="stat-label">Visualizzati</span>
                    </div>
                </div>

                <div className="biglietti-list">
                    {filteredBiglietti.length === 0 ? (
                        <div className="no-results">
                            <p>Nessun biglietto trovato con i criteri di ricerca attuali</p>
                            <button onClick={() => setSearchTerm('')}>
                                Pulisci ricerca
                            </button>
                        </div>
                    ) : (
                        filteredBiglietti.map((biglietto) => (
                            <div key={biglietto.id} className="biglietto-card">
                                <div className="biglietto-header">
                                    <h3>Biglietto #{biglietto.id}</h3>
                                    <span className={getStatusClass(biglietto)}>
                                        {getStatusText(biglietto)}
                                    </span>
                                </div>
                                {activeTab === 'validi' ? (
                                    <div className="biglietto-info">
                                        <div className="biglietto-vehicle">
                                            <p><strong>Targa:</strong> <span className="info-value">{(biglietto as Biglietto).targa}</span></p>
                                            <p><strong>Data Ingresso:</strong> <span className="info-value">{formatDate((biglietto as Biglietto).dataIngresso)}</span></p>
                                        </div>
                                        <div className="biglietto-entrance">
                                            <p><strong>Casello Ingresso:</strong> <span className="info-value">{(biglietto as Biglietto).capIngresso} (ID: {(biglietto as Biglietto).idCaselloIngresso})</span></p>
                                            <p><strong>Direzione:</strong> <span className="info-value">{(biglietto as Biglietto).direzioneIngresso}</span></p>
                                        </div>
                                    </div>
                                ) : (
                                    <div className="biglietto-info">
                                        <div className="biglietto-vehicle">
                                            <p><strong>Targa:</strong> <span className="info-value">{(biglietto as StoricoConPagamento).targa}</span></p>
                                            <p><strong>Data Uscita:</strong> <span className="info-value">{formatDate((biglietto as StoricoConPagamento).dataUscita)}</span></p>
                                        </div>
                                        <div className="biglietto-exit">
                                            <p><strong>Casello Uscita:</strong> <span className="info-value">{(biglietto as StoricoConPagamento).capUscita} (ID: {(biglietto as StoricoConPagamento).idCaselloUscita})</span></p>
                                            <p><strong>Direzione:</strong> <span className="info-value">{(biglietto as StoricoConPagamento).direzioneUscita}</span></p>
                                        </div>
                                        <div className="biglietto-payment">
                                            <p><strong>Prezzo Pagato:</strong> <span className="info-value price">{formatPrice((biglietto as StoricoConPagamento).prezzoPagato)}</span></p>
                                            <p><strong>Metodo Pagamento:</strong> <span className="info-value">{(biglietto as StoricoConPagamento).metodoPagamentoStorico}</span></p>
                                            <p><strong>Data Pagamento:</strong> <span className="info-value">{formatDate((biglietto as StoricoConPagamento).dataPagamento)}</span></p>
                                            <p><strong>Importo:</strong> <span className="info-value price">{formatPrice((biglietto as StoricoConPagamento).importo)}</span></p>
                                        </div>
                                    </div>
                                )}
                            </div>
                        ))
                    )}
                </div>
            </div>
        );
    };

    return (
        <div className="dashboard-wrapper">
            <div className="navbar">
                <div className="logo" onClick={handleBackToDashboard}>
                    🛣️ Autostrade
                </div>

                <div className="profile-container" onClick={handleProfileClick}>
                    <div className="profile-icon-wrapper">
                        <div className="profile-icon">👤</div>
                        <span className="username-label">
                            {username ? username : "Guest"}
                        </span>
                    </div>
                </div>
            </div>

            <div className="container-dash">
                <div className="section-tabs">
                    <button
                        className={`tab-button ${activeTab === 'validi' ? 'active' : ''}`}
                        onClick={() => switchTab('validi')}
                    >
                        Biglietti Emessi
                    </button>
                    <button
                        className={`tab-button ${activeTab === 'tutti' ? 'active' : ''}`}
                        onClick={() => switchTab('tutti')}
                    >
                        Biglietti Pagati
                    </button>
                </div>

                {renderBigliettiContent()}
            </div>

            <footer className="footer">
                &copy; 2025 Autostrade S.p.A. - Fondata da Klejdi Tahiri
            </footer>
        </div>
    );
}

export default Biglietti;