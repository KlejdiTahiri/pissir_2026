// Interface per tipizzare i dati dei biglietti


export interface Biglietto {
    id: number;
    targa: string;
    capIngresso: string;
    idCaselloIngresso: number;
    direzioneIngresso: string;
    dataIngresso: string;
    valido: boolean;
}
