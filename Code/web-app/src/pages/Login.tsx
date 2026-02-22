import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {useAuth} from "./context/AuthContext.tsx";
import "../pages/styles/Login.css";


function Login() {
    const navigate = useNavigate();
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [showPassword] = useState(false);
    const { setUsernameSession, setRuoloSession } = useAuth();


    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);

        try {
            const res = await fetch("/api/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ username: username, password }),
                credentials: "include"
            });

            if (res.ok) {
                const data = await res.json();
                console.log("Login success", data);

                // ⬇️ Salvo i dati dell'utente nel contesto globale
                setUsernameSession(username);
                setRuoloSession(data.ruolo);

                navigate("/dashboard");
            } else {
                alert("Credenziali errate");
            }
        } catch (err) {
            console.error("Errore nel login", err);
        } finally {
            setIsLoading(false);
        }
    };



    return (
        <div className="login-container">
            <div className="login-card">
                <div className="login-header">
                    <div className="logo">
                        <div className="logo-icon">🛣️</div>
                        <h1>Accesso Operatori</h1>
                        <p className="subtitle">Portale gestione caselli ANAS</p>
                    </div>
                    <p className="subtitle">Accedi al tuo account</p>
                </div>

                <form onSubmit={handleLogin} className="login-form">
                    <div className="form-group">
                        <label htmlFor="username">Username</label>
                        <input
                            id="username"
                            type="text"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            placeholder="Il tuo username"
                            required
                            disabled={isLoading}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Password</label>
                        <div className="password-input-container">
                            <input
                                id="password"
                                type={showPassword ? "text" : "password"}
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="La tua password"
                                required
                                disabled={isLoading}
                            />
                        </div>
                    </div>



                    <button
                        type="submit"
                        className={`login-btn ${isLoading ? 'loading' : ''}`}
                        disabled={isLoading}
                    >
                        {isLoading ? (
                            <>
                                <span className="spinner"></span>
                                Accesso in corso...
                            </>
                        ) : (
                            'Accedi'
                        )}
                    </button>
                </form>
                <div className="divider">
                    <span>oppure</span>
                </div>
            </div>
        </div>
    );
}

export default Login;