// src/types/TrafficData.ts
import type { Incident } from "./Incident";

export type TrafficData = {
    livelloTraffico: "BASSO" | "MEDIO" | "ALTO" | "BLOCCATO";
    velocitaAttuale: number;
    totaleGuadagni: number;
    lavori: boolean;
    meteo: string;
    ultimoAggiornamento: string;
    incidenti: number;
    listaIncidenti?: Incident[];
};
