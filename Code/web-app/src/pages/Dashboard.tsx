// src/pages/Dashboard.tsx
import { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import "./styles/Dashboard.css";
import AggiungiCaselloForm from "./functions/AggiungiCaselloForm.tsx";
import { useAuth } from "./context/AuthContext";
import type {Tratta} from "./types/Tratta.ts";
import type {Casello} from "./types/Casello.ts";

function Dashboard() {
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();
    const initialTab = searchParams.get('tab') === 'caselli' ? 'caselli' : 'tratte';
    const [activeTab, setActiveTab] = useState<'tratte' | 'caselli'>(initialTab);
    const { username, setUsernameSession } = useAuth();

// dopo login o chiamata a /sessione
// setUsername("operatore1")


    // Stati per la gestione delle tratte
    const [tratte, setTratte] = useState<Tratta[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [searchPartenza, setSearchPartenza] = useState<string>('');
    const [searchArrivo, setSearchArrivo] = useState<string>('');

    // Stati per la gestione dei caselli
    const [caselli, setCaselli] = useState<Casello[]>([]);
    const [caselliLoading, setCaselliLoading] = useState<boolean>(false);
    const [caselliError, setCaselliError] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState<string>('');
    const [filterTratta, setFilterTratta] = useState<string>('');
    const [showFormModal, setShowFormModal] = useState(false);

    // Fetch delle tratte dal backend
    const fetchTratte = async () => {
        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`/api/tratte`);

            if (!response.ok) {
                throw new Error(`Errore HTTP: ${response.status} - ${response.statusText}`);
            }

            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
                const text = await response.text();
                throw new Error(`Risposta non JSON ricevuta: ${text.substring(0, 100)}...`);
            }

            const data: Tratta[] = await response.json();
            setTratte(data);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Errore sconosciuto');
            console.error('Errore nel caricamento delle tratte:', err);
        } finally {
            setLoading(false);
        }
    };

    // Fetch dei caselli dal backend
    const fetchCaselli = async () => {
        setCaselliLoading(true);
        setCaselliError(null);

        try {
            const response = await fetch(`/api/caselli`);

            if (!response.ok) {
                throw new Error(`Errore HTTP: ${response.status} - ${response.statusText}`);
            }

            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
                const text = await response.text();
                throw new Error(`Risposta non JSON ricevuta: ${text.substring(0, 100)}...`);
            }

            const data: Casello[] = await response.json();
            setCaselli(data);
        } catch (err) {
            setCaselliError(err instanceof Error ? err.message : 'Errore sconosciuto');
            console.error('Errore nel caricamento dei caselli:', err);
        } finally {
            setCaselliLoading(false);
        }
    };

    useEffect(() => {
        if (activeTab === 'tratte') {
            fetchTratte();
        } else if (activeTab === 'caselli') {
            fetchCaselli();
        }
    }, [activeTab]);


    useEffect(() => {
        fetch("/api/sessione", { credentials: "include" })
            .then(res => res.json())
            .then(data => {
                if (data.logged) {
                    setUsernameSession(data.username); // 👈 ora lo setti globalmente
                } else {
                    navigate("/login");
                }
            })
            .catch(err => {
                console.error("Errore nel recupero sessione:", err);
                navigate("/login");
            });
    }, []);



    useEffect(() => {
        setSearchParams({ tab: activeTab });
    }, [activeTab, setSearchParams]);


    const handleProfileClick = () => {
        navigate("/profilo");
    };

    const handleLogoClick = () => {
        setActiveTab('tratte');
    };

    const switchTab = (tab: 'tratte' | 'caselli') => {
        setActiveTab(tab);
    };

    const filteredCaselli = caselli.filter(casello => {
        const matchesSearch = casello.cap.toLowerCase().includes(searchTerm.toLowerCase()) ||
            casello.idCasello.toString().includes(searchTerm);

        const matchesFilter = filterTratta === '' || casello.cap.includes(filterTratta);

        return matchesSearch && matchesFilter;
    });

   /** const handleEditCasello = (casello: Casello) => {
        console.log('Modifica casello:', casello);
    };**/

    const handleViewCasello = (casello: Casello) => {
        // Naviga alla pagina dei dettagli del casello
        // Usa encodeURIComponent per gestire caratteri speciali nei parametri URL
        navigate(`/casello/${encodeURIComponent(casello.cap)}/${casello.idCasello}/${encodeURIComponent(casello.direzione)}`);
    };




    const handleAddCasello = () => {
        setShowFormModal(true); // mostra il form
    };


    const getStatusClass = (stato: boolean) => {
        return stato ? 'status active' : 'status maintenance';
    };

    const getStatusText = (stato: boolean) => {
        return stato ? 'Attivo' : 'Inattivo';
    };

    // Modifica: ora naviga alla pagina dei dettagli invece di solo loggare
    const handleSelectTratta = (tratta: Tratta) => {
        console.log('Navigando ai dettagli della tratta:', tratta.id);
        navigate(`/tratta/${tratta.id}`);
    };

    const renderTrattesContent = () => {
        const filteredTratte = tratte.filter(tratta => {
            const matchesPartenza = searchPartenza === '' ||
                tratta.capIn.toLowerCase().includes(searchPartenza.toLowerCase()) ||
                tratta.idCaselloIn.toString().includes(searchPartenza);

            const matchesArrivo = searchArrivo === '' ||
                tratta.capOut.toLowerCase().includes(searchArrivo.toLowerCase()) ||
                tratta.idCaselloOut.toString().includes(searchArrivo);

            return matchesPartenza && matchesArrivo;
        });

        if (loading) {
            return (
                <div className="tab-content loading">
                    <div className="loading-spinner">
                        <div className="spinner"></div>
                        <p>Caricamento tratte in corso...</p>
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
                        <button onClick={fetchTratte} className="retry-btn">
                            Riprova
                        </button>
                    </div>
                </div>
            );
        }

        if (tratte.length === 0) {
            return (
                <div className="tab-content empty">
                    <div className="empty-state">
                        <h3>Nessuna tratta disponibile</h3>
                        <p>Non ci sono tratte configurate nel sistema</p>
                        <button onClick={fetchTratte} className="refresh-btn">
                            Aggiorna
                        </button>
                    </div>
                </div>
            );
        }

        return (
            <div className="tab-content">
                <div className="content-header">
                    <h2>Seleziona Tratta</h2>
                    <p>Clicca su una tratta per selezionarla</p>
                    <button onClick={fetchTratte} className="refresh-btn small">
                        🔄 Aggiorna
                    </button>
                </div>

                <div className="tratte-search">
                    <div className="tratte-search-fields">
                        <input
                            type="text"
                            placeholder="Cerca per partenza..."
                            className="search-input"
                            value={searchPartenza}
                            onChange={(e) => setSearchPartenza(e.target.value)}
                        />
                        <input
                            type="text"
                            placeholder="Cerca per arrivo..."
                            className="search-input"
                            value={searchArrivo}
                            onChange={(e) => setSearchArrivo(e.target.value)}
                        />
                    </div>
                    <div className="clear-search-btn-container">
                        <button
                            onClick={() => {
                                setSearchPartenza('');
                                setSearchArrivo('');
                            }}
                            className="clear-search-btn"
                        >
                            Cancella
                        </button>
                    </div>
                </div>

                <div className="tratte-grid">
                    {filteredTratte.length === 0 ? (
                        <div className="no-results">
                            <p>Nessuna tratta trovata con i criteri di ricerca attuali</p>
                        </div>
                    ) : (
                        filteredTratte.map((tratta) => (
                            <div
                                key={tratta.id}
                                className="tratta-card clickable"
                                onClick={() => handleSelectTratta(tratta)}
                                role="button"
                                tabIndex={0}
                                onKeyDown={(e) => {
                                    if (e.key === 'Enter' || e.key === ' ') {
                                        handleSelectTratta(tratta);
                                    }
                                }}
                            >
                                <div className="tratta-header">
                                    <h3>Tratta {tratta.id}</h3>
                                    <span className="tratta-code">ID: {tratta.id}</span>
                                </div>
                                <div className="tratta-info">
                                    <p><strong>Partenza:</strong> <span
                                        className="info-value">{tratta.capIn} (Casello {tratta.idCaselloIn})</span></p>
                                    <p><strong>Direzione:</strong> <span className="info-value">{tratta.dirIn}</span>
                                    </p>
                                    <p><strong>Arrivo:</strong> <span
                                        className="info-value">{tratta.capOut} (Casello {tratta.idCaselloOut})</span>
                                    </p>
                                    <p><strong>Direzione:</strong> <span className="info-value">{tratta.dirOut}</span>
                                    </p>
                                    <p><strong>Lunghezza:</strong> <span
                                        className="info-value">{tratta.chilometri} km</span></p>
                                    <p><strong>Prezzo:</strong> <span
                                        className="info-value">€{tratta.prezzo.toFixed(2)}</span></p>
                                    {/**<p><strong>Velocità massima:</strong> <span
                                        className="info-value">{tratta.velocitaMedia} km/h</span></p>**/}
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </div>
        );
    };

    const renderCaselliContent = () => {
        if (caselliLoading) {
            return (
                <div className="tab-content loading">
                    <div className="loading-spinner">
                        <div className="spinner"></div>
                        <p>Caricamento caselli in corso...</p>
                    </div>
                </div>
            );
        }

        if (caselliError) {
            return (
                <div className="tab-content error">
                    <div className="error-message">
                        <h3>⚠️ Errore nel caricamento</h3>
                        <p>{caselliError}</p>
                        <button onClick={fetchCaselli} className="retry-btn">
                            Riprova
                        </button>
                    </div>
                </div>
            );
        }

        if (caselli.length === 0) {
            return (
                <div className="tab-content empty">
                    <div className="empty-state">
                        <h3>Nessun casello disponibile</h3>
                        <p>Non ci sono caselli configurati nel sistema</p>
                        <button onClick={fetchCaselli} className="refresh-btn">
                            Aggiorna
                        </button>
                    </div>
                </div>
            );
        }

        return (
            <div className="tab-content">
                <div className="content-header">
                    <h2>Gestisci Caselli</h2>
                    <p>Monitora e gestisci lo stato dei caselli autostradali</p>
                    <button onClick={fetchCaselli} className="refresh-btn small">
                        🔄 Aggiorna
                    </button>
                </div>

                <div className="caselli-controls">
                    <div className="search-filter">
                        <input
                            type="text"
                            placeholder="Cerca casello per nome o codice..."
                            className="search-input"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                        <select
                            className="filter-select"
                            value={filterTratta}
                            onChange={(e) => setFilterTratta(e.target.value)}
                        >
                            <option value="">Tutte le località</option>
                            <option value="MI">Milano</option>
                            <option value="RM">Roma</option>
                            <option value="FI">Firenze</option>
                            <option value="NA">Napoli</option>
                            <option value="TO">Torino</option>
                            <option value="BO">Bologna</option>
                        </select>
                    </div>
                    <button className="add-casello-btn" onClick={handleAddCasello}>
                        + Aggiungi Casello
                    </button>
                </div>

                <div className="caselli-stats">
                    <div className="stat-item">
                        <span className="stat-number">{caselli.length}</span>
                        <span className="stat-label">Totale Caselli</span>
                    </div>
                    <div className="stat-item">
                        <span className="stat-number">{caselli.filter(c => c.stato).length}</span>
                        <span className="stat-label">Attivi</span>
                    </div>
                    <div className="stat-item">
                        <span className="stat-number">{caselli.filter(c => !c.stato).length}</span>
                        <span className="stat-label">Inattivi</span>
                    </div>
                    <div className="stat-item">
                        <span className="stat-number">{filteredCaselli.length}</span>
                        <span className="stat-label">Visualizzati</span>
                    </div>
                </div>

                <div className="caselli-list">
                    {filteredCaselli.length === 0 ? (
                        <div className="no-results">
                            <p>Nessun casello trovato con i criteri di ricerca attuali</p>
                            <button onClick={() => { setSearchTerm(''); setFilterTratta(''); }}>
                                Pulisci filtri
                            </button>
                        </div>
                    ) : (
                        filteredCaselli.map((casello) => (
                            <div key={`${casello.idCasello}-${casello.direzione}`} className="casello-item clickable"
                                 onClick={() => handleViewCasello(casello)}>
                                <div className="casello-info">
                                    <h4>{casello.cap}</h4>
                                    <p>ID: {casello.idCasello} - Dir: {casello.direzione}</p>

                                </div>
                                <span className={getStatusClass(casello.stato)}>
                                        {getStatusText(casello.stato)}
                                    </span>
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
                <a className="logo" onClick={handleLogoClick}>
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


            <div className="container-dash">
                <div className="section-tabs">
                    <button
                        className={`tab-button ${activeTab === 'tratte' ? 'active' : ''}`}
                        onClick={() => switchTab('tratte')}
                    >
                        Seleziona Tratta
                    </button>
                    <button
                        className={`tab-button ${activeTab === 'caselli' ? 'active' : ''}`}
                        onClick={() => switchTab('caselli')}
                    >
                        Gestisci Caselli
                    </button>
                </div>

                {activeTab === 'tratte' && renderTrattesContent()}
                {activeTab === 'caselli' && renderCaselliContent()}
            </div>

            <footer className="footer">
                &copy; 2025 Autostrade S.p.A. - Fondata da Klejdi Tahiri
            </footer>

            {/* MODALE */}
            {showFormModal && (
                <div className="modal-overlay" onClick={() => setShowFormModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <AggiungiCaselloForm onClose={() => setShowFormModal(false)}/>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Dashboard;