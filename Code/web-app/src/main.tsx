import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import "./index.css"; // se hai CSS
import { AuthProvider } from "./pages/context/AuthContext.tsx"; // 👈

ReactDOM.createRoot(document.getElementById("root")!).render(
    <React.StrictMode>
        <AuthProvider>
            <App />
        </AuthProvider>
    </React.StrictMode>
);
