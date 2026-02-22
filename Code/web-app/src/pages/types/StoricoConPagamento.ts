export interface StoricoConPagamento {
    id: number;
    targa: string;
    capUscita: string;
    idCaselloUscita: number;
    direzioneUscita: string;
    dataUscita: string; // LocalDateTime serializzato come stringa ISO
    prezzoPagato: number;
    metodoPagamentoStorico: string;

    // Campi da tabella pagamenti
    dataPagamento: string; // anche questa come stringa ISO
    importo: number;
    metodoPagamento: string;
    statoPagamento: number; // 1 = pagato, 0 = non pagato (o altro stato)
}
