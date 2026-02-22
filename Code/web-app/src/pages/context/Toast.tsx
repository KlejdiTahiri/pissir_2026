// src/components/Toast.tsx
import "../styles/Toast.css";

interface ToastProps {
    message: string;
    type?: "success" | "error";
    onClose?: () => void;
}

const Toast = ({ message, type = "success"}: ToastProps) => {
    return (
        <div className={`toast ${type}`}>
            <span>{message}</span>
        </div>
    );
};

export default Toast;
