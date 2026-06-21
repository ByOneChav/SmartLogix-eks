export type EstadoEnvio =
  | 'PREPARANDO'
  | 'EN_TRANSITO'
  | 'ENTREGADO'
  | 'DEVUELTO';

export interface Envio {
  id?: number;
  pedidoId: number;
  direccionDestino: string;
  estado?: EstadoEnvio;
  fechaEnvio?: string;
}
