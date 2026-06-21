export interface Inventario {
  id?: number;
  nombreProducto: string;
  ubicacion: string;
  stock: number;
  precio: number;
  stockMinimo?: number;
}
