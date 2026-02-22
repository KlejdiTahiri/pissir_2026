import React, { createContext, useContext, useState } from "react";

interface AuthContextType {
    username: string | null;
    ruolo: string | null;
    setUsernameSession: (username: string | null) => void;
    setRuoloSession: (ruolo: string | null) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
    const [username, setUsername] = useState<string | null>(null);
    const [ruolo, setRuolo] = useState<string | null>(null);

    const setUsernameSession = (value: string | null) => setUsername(value);
    const setRuoloSession = (value: string | null) => setRuolo(value);

    return (
        <AuthContext.Provider value={{
            username,
            ruolo,
            setUsernameSession,
            setRuoloSession
        }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuth must be used within an AuthProvider");
    }
    return context;
};
