
import {BrowserRouter, Routes, Route, Navigate} from "react-router-dom";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Profilo from "./pages/Profilo";
import DettagliCasello from "./pages/DettagliCasello.tsx";
import VeicoliCaselli from "./pages/functions/VeicoliCaselli.tsx";
import TrattaDettagli from './pages/TrattaDettagli';
import AggiungiCaselloForm from "./pages/functions/AggiungiCaselloForm.tsx";
import Biglietti from "./pages/Biglietti.tsx";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                {/* Rotta di login */}
                <Route path="/login" element={<Login />} />

                {/* Rotta dashboard */}
                <Route path="/dashboard" element={<Dashboard />} />

                {/* Rotta biglietti */}
                <Route path="/biglietti" element={<Biglietti />} />

                {/* Rotta profilo */}
                <Route path="/profilo" element={<Profilo />} />

                {/* Nuova rotta per i dettagli del casello */}
                <Route path="/casello/:cap/:id/:direzione" element={<DettagliCasello />} />

                {/* Rotta di default */}
                <Route path="/" element={<Navigate to="/login" replace />} />

                {/* Rotta per pagine non trovate */}
                <Route path="*" element={<Navigate to="/dashboard" replace />} />

                <Route path="/veicoli/caselli/:cap/:id/:direzione" element={<VeicoliCaselli />} />


                {/* Rotta per i dettagli della tratta */}
                <Route path="/tratta/:id" element={<TrattaDettagli />} />

                <Route path="/aggiungi-casello" element={<AggiungiCaselloForm onClose={function(): void {
                    throw new Error("Function not implemented.");
                } } />} />

            </Routes>
        </BrowserRouter>
    );
}

export default App;
