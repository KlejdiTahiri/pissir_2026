// src/types/Incident.ts
export type Incident = {
    idTratta: number;
    timestamp: number; // UNIX timestamp
    descrizione: string;
    gravita: "BASSA" | "MEDIA" | "ALTA";
    km: number;      // opzionale
    dettagli?: string;   // opzionale
};
