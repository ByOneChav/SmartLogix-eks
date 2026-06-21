export type EstadoPedido =
  | 'PENDIENTE'
  | 'CONFIRMADO'
  | 'EN_PREPARACION'
  | 'ENVIADO'
  | 'ENTREGADO'
  | 'CANCELADO';

export interface Pedido {
  id?: number;
  clienteNombre: string;
  descripcion: string;
  cantidad: number;
  precio?: number;
  inventarioId: number;
  estado?: EstadoPedido;
  fechaPedido?: string;
}
