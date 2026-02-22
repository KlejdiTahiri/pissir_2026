// src/pages/Profilo.tsx
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./styles/Profilo.css";
import {useAuth} from "./context/AuthContext.tsx";

interface User {
    username: string;
    ruolo: string;
}

function Profilo() {
    const navigate = useNavigate();
    const { username: contextUsername } = useAuth(); // 👈 preleva lo username dal context

    const [user, setUser] = useState<User>({
        username: contextUsername || "utente123",
        ruolo: "Impiegato"
    });
    const [formData, setFormData] = useState({
        username: "",
        password: ""
    });
    const [message, setMessage] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [isLoadingProfile, setIsLoadingProfile] = useState(true); // 👈 stato per il caricamento iniziale

    const [showCreateNewUser, setShowCreateNewUser] = useState(false);
    const [newUser, setNewUser] = useState({
        username: "",
        password: "",
        ruolo: "Amministratore"
    });
    const [createNewUserMessage, setCreateNewUserMessage] = useState("");
    const [isCreatingNewUser, setIsCreatingNewUser] = useState(false);


    const handleSetNewUserInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setNewUser(prev => ({
            ...prev,
            [name]: value
        }));
    };


    const handleCreateNewUserSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsCreatingNewUser(true);
        setCreateNewUserMessage("");

        try {
            const response = await fetch("/api/crea_utente", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                credentials: "include",
                body: JSON.stringify({
                    username: newUser.username,
                    password: newUser.password,
                    ruolo: newUser.ruolo
                })
            });

            const result = await response.text();

            if (result === "success") {
                setCreateNewUserMessage("Utente creato con successo!");
                // Resetta il form
                setNewUser({
                    username: "",
                    password: "",
                    ruolo: "amministratore"
                });
            } else {
                setCreateNewUserMessage("Errore nella creazione dell'utente.");
            }
        } catch (error) {
            console.error("Errore nella creazione dell'utente:", error);
            setCreateNewUserMessage("Errore di rete durante la creazione.");
        } finally {
            setIsCreatingNewUser(false);
        }
    };


    // 🆕 Funzione per caricare i dati del profilo dal backend
    const caricaProfiloUtente = async () => {
        if (!contextUsername) {
            setMessage("Username non disponibile");
            setIsLoadingProfile(false);
            return;
        }

        try {
            const response = await fetch(`/api/profilo?username=${encodeURIComponent(contextUsername)}`, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json"
                },
                credentials: "include" // 🔑 Per inviare eventuali cookie di autenticazione
            });

            if (response.ok) {
                const userData = await response.json();
                setUser({
                    username: userData.username || contextUsername,
                    ruolo: userData.ruolo || "Impiegato"
                });
                console.log("Dati profilo caricati con successo:", userData);
            } else {
                console.warn("Errore nel caricamento del profilo:", response.status);
                setMessage("Errore nel caricamento del profilo");
                // Mantieni i dati di default dal context
            }
        } catch (error) {
            console.error("Errore di rete nel caricamento profilo:", error);
            setMessage("Errore di connessione");
            // Mantieni i dati di default dal context
        } finally {
            setIsLoadingProfile(false);
        }
    };

    // 🔄 Carica i dati del profilo all'avvio del componente
    useEffect(() => {
        caricaProfiloUtente();
    }, [contextUsername]);

    // Popola i campi del form dopo aver caricato i dati
    useEffect(() => {
        setFormData({
            username: user.username,
            password: ""
        });
    }, [user]);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleLogoClick = () => {
        navigate("/dashboard");
    };

    const handleLogout = async () => {
        try {
            const res = await fetch("/api/logout", {
                method: "GET",
                credentials: "include", // 🔑 Per inviare il cookie
            });

            if (res.ok) {
                console.log("Logout eseguito con successo");
            } else {
                console.warn("Errore durante il logout");
            }
        } catch (err) {
            console.error("Errore di rete nel logout:", err);
        } finally {
            navigate("/login"); // 🔄 Torna alla pagina di login
        }
    };

    const salvaModifiche = async () => {
        setIsLoading(true);
        setMessage("");

        try {
            // Simulazione chiamata API (sostituisci con la tua implementazione)
            const response = await fetch("/api/profilo", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    username: formData.username,
                    password: formData.password
                })
            });

            const result = await response.text();

            if (result === "success") {
                setMessage("Modifiche salvate con successo!");
                // Aggiorna i dati utente localmente
                setUser(prev => ({
                    ...prev,
                    username: formData.username
                }));
                // Pulisci il campo password
                setFormData(prev => ({
                    ...prev,
                    password: ""
                }));
            } else {
                setMessage("Errore nel salvataggio.");
            }
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
        } catch (error) {
            setMessage("Errore di connessione.");
        } finally {
            setIsLoading(false);
        }
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        salvaModifiche();
    };

    // 🔄 Mostra loading durante il caricamento iniziale dei dati
    if (isLoadingProfile) {
        return (
            <div className="profile-wrapper">
                <div className="navbar">
                    <a className="logo" onClick={handleLogoClick}>
                        🛣️ Autostrade
                    </a>
                    <button className="logout-button" onClick={handleLogout} title="Logout">
                        <svg className="logout-icon" width="20" height="20" viewBox="0 0 24 24" fill="none"
                             stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                            <polyline points="16 17 21 12 16 7"></polyline>
                            <line x1="21" y1="12" x2="9" y2="12"></line>
                        </svg>
                    </button>
                </div>
                <div className="container">
                    <div style={{ textAlign: 'center', padding: '2rem' }}>
                        <span className="spinner"></span>
                        <p>Caricamento profilo...</p>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="profile-wrapper">
            {/* Navbar */}
            <div className="navbar">
                <a className="logo" onClick={handleLogoClick}>
                    🛣️ Autostrade
                </a>
                <button className="logout-button" onClick={handleLogout} title="Logout">
                    <svg className="logout-icon" width="20" height="20" viewBox="0 0 24 24" fill="none"
                         stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                        <polyline points="16 17 21 12 16 7"></polyline>
                        <line x1="21" y1="12" x2="9" y2="12"></line>
                    </svg>
                </button>
            </div>

            {/* Main Container */}
            <div className="container">
                <h2>Profilo Utente</h2>

                <form onSubmit={handleSubmit}>
                    <label htmlFor="username">Username</label>
                    <input
                        type="text"
                        id="username"
                        name="username"
                        value={formData.username}
                        onChange={handleInputChange}
                        disabled={isLoading}
                        required
                    />

                    <label htmlFor="password">Nuova Password</label>
                    <input
                        type="password"
                        id="password"
                        name="password"
                        value={formData.password}
                        onChange={handleInputChange}
                        placeholder="Lascia vuoto per non cambiare"
                        disabled={isLoading}
                    />

                    <div className="role-label" id="ruolo">
                        Ruolo: {user.ruolo}
                    </div>

                    <button type="submit" disabled={isLoading}>
                        {isLoading ? (
                            <>
                                <span className="spinner"></span>
                                Salvataggio...
                            </>
                        ) : (
                            "Salva modifiche"
                        )}
                    </button>

                    {message && (
                        <div className={`message ${message.includes("successo") ? "success" : "error"}`}>
                            {message}
                        </div>
                    )}
                </form>

                {/* 🆕 Sezione per Manager - Creazione nuovi utenti */}
                {user.ruolo.toLowerCase() === "manager" && (
                    <div className="manager-section">
                        <div className="section-divider"></div>
                        <h3>Gestione Utenti</h3>

                        {!showCreateNewUser ? (
                            <button
                                className="create-user-button"
                                onClick={() => setShowCreateNewUser(true)}
                                type="button"
                            >
                                ➕ Crea Nuovo Utente
                            </button>
                        ) : (
                            <div className="create-user-form">
                                <h4>Crea Nuovo Utente</h4>
                                <form onSubmit={handleCreateNewUserSubmit}>
                                    <label htmlFor="newUsername">Username</label>
                                    <input
                                        type="text"
                                        id="newUsername"
                                        name="username"
                                        value={newUser.username}
                                        onChange={handleSetNewUserInputChange}
                                        disabled={isCreatingNewUser}
                                        required
                                    />

                                    <label htmlFor="newPassword">Password</label>
                                    <input
                                        type="password"
                                        id="newPassword"
                                        name="password"
                                        value={newUser.password}
                                        onChange={handleSetNewUserInputChange}
                                        disabled={isCreatingNewUser}
                                        required
                                    />

                                    <label htmlFor="newRuolo">Ruolo</label>
                                    <select
                                        id="newRuolo"
                                        name="ruolo"
                                        value={newUser.ruolo}
                                        onChange={handleSetNewUserInputChange}
                                        disabled={isCreatingNewUser}
                                        required
                                    >
                                        <option value="Amministratore">amministratore</option>
                                        <option value="Manager">manager</option>
                                    </select>

                                    <div className="form-buttons">
                                        <button
                                            type="submit"
                                            disabled={isCreatingNewUser}
                                            className="create-button"
                                        >
                                            {isCreatingNewUser ? (
                                                <>
                                                    <span className="spinner"></span>
                                                    Creazione...
                                                </>
                                            ) : (
                                                "Crea Utente"
                                            )}
                                        </button>

                                        <button
                                            type="button"
                                            onClick={() => {
                                                setShowCreateNewUser(false);
                                                setCreateNewUserMessage("");
                                                setNewUser({
                                                    username: "",
                                                    password: "",
                                                    ruolo: "Amministratore"
                                                });
                                            }}
                                            disabled={isCreatingNewUser}
                                            className="cancel-button"
                                        >
                                            Annulla
                                        </button>
                                    </div>

                                    {createNewUserMessage && (
                                        <div className={`message ${createNewUserMessage.includes("successo") ? "success" : "error"}`}>
                                            {createNewUserMessage}
                                        </div>
                                    )}
                                </form>
                            </div>
                        )}
                    </div>
                )}
            </div>

            {/* Footer */}
            <footer className="footer">
                &copy; 2025 Autostrade S.p.A. - Fondata da Klejdi Tahiri
            </footer>
        </div>
    );
}

export default Profilo;