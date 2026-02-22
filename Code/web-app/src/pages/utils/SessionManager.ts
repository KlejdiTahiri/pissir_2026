// src/utils/sessionManager.ts
// src/utils/sessionManager.ts
import type {Tratta} from "../types/Tratta.ts";

// INTERFACCIA TRATTA (se vuoi puoi importarla da un file condiviso)

// src/utils/sessionManager.ts

const ID_TRATTA_KEY = "trattaSelezionataId";

export const setIdTratta = (id: number) => {
    localStorage.setItem(ID_TRATTA_KEY, String(id));
};

export const getIdTratta = (): number | null => {
    const stored = localStorage.getItem(ID_TRATTA_KEY);
    return stored ? parseInt(stored, 10) : null;
};

export const clearIdTratta = () => {
    localStorage.removeItem(ID_TRATTA_KEY);
};



// === TRATTA ===
export const salvaTrattaInSessione = (tratta: Tratta) => {
    sessionStorage.setItem(`tratta_${tratta.id}`, JSON.stringify(tratta));
};

export const caricaTrattaDaSessione = (id: number): Tratta | null => {
    const stored = sessionStorage.getItem(`tratta_${id}`);
    return stored ? JSON.parse(stored) : null;
};

export const cancellaTrattaDaSessione = (id: number) => {
    sessionStorage.removeItem(`tratta_${id}`);
};

// === UTENTE ===
/**export const salvaUtenteInSessione = (username: string) => {
    sessionStorage.setItem("utente", username);
};

export const getUtenteDaSessione = (): string | null => {
    return sessionStorage.getItem("utente");
};

export const logoutUtente = () => {
    sessionStorage.removeItem("utente");
    // eventualmente pulisci anche tutte le tratte o altre chiavi
};**/
