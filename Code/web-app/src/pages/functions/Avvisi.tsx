import { useState } from "react";
import type { TrafficData } from "../types/TrafficData";
import "../styles/Avvissi.css";

export default function Avvisi({ traffico }: { traffico: TrafficData }) {
    const [popupOpen, setPopupOpen] = useState(false);

    return (
        <div className="avvisi-section">
            <h3>⚠️ Avvisi</h3>
            <div className="avvisi-list">
                {traffico.listaIncidenti && traffico.listaIncidenti.length > 0 ? (
                    <div
                        className="avviso incident cursor-pointer"
                        onClick={() => setPopupOpen(true)}
                    >
                        <span>🚨</span>
                        <p>
                            {traffico.listaIncidenti.length} incidente
                            {traffico.listaIncidenti.length > 1 ? "i" : ""} segnalato
                            {traffico.listaIncidenti.length > 1 ? "i" : ""}
                        </p>
                    </div>
                ) : traffico.lavori ? (
                    <div className="avviso construction">
                        <span>🚧</span>
                        <p>Lavori in corso sulla tratta</p>
                    </div>
                ) : (
                    <div className="avviso all-clear">
                        <span>✅</span>
                        <p>Nessun problema segnalato</p>
                    </div>
                )}
            </div>

            {popupOpen && traffico.listaIncidenti && (
                <div className="popup-overlay" onClick={() => setPopupOpen(false)}>
                    <div className="popup-content" onClick={(e) => e.stopPropagation()}>
                        <button
                            className="popup-close-btn"
                            onClick={() => setPopupOpen(false)}
                        >
                            ✖️
                        </button>
                        <h4 className="popup-title">Incidenti segnalati</h4>
                        <div className="incidenti-list">
                            {traffico.listaIncidenti.map((inc, i) => (
                                <div key={i} className="incidente-card">
                                    <p><strong>Gravità:</strong> {inc.gravita}</p>
                                    <p><strong>Posizione:</strong> {inc.km ? `${inc.km} Km` : "N/A"}</p>
                                    <p><strong>Descrizione:</strong> {inc.descrizione}</p>
                                    {inc.dettagli && <p><strong>Dettagli:</strong> {inc.dettagli}</p>}
                                    <p><strong>Segnalato il:</strong> {new Date(inc.timestamp * 1000).toLocaleString("it-IT")}</p>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}