import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { PedidoService } from '../../services/pedido.service';
import { Pedido, EstadoPedido } from '../../models/pedido.model';
import { InventarioService } from '../../../inventario/services/inventario.service';
import { Inventario } from '../../../inventario/models/inventario.model';

@Component({
  selector: 'app-pedidos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pedidos.html',
  styleUrl: './pedidos.css'
})
export class PedidosComponent implements OnInit {

  pedidos: Pedido[] = [];
  inventarios: Inventario[] = [];
  inventarioSeleccionado: Inventario | null = null;

  nuevo: Pedido = {
    clienteNombre: '',
    descripcion: '',
    cantidad: 1,
    inventarioId: 0
  };

  loading = false;
  cargandoLista = true;
  error = '';
  success = '';

  constructor(
    private pedidoService: PedidoService,
    private inventarioService: InventarioService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargandoLista = true;
    this.pedidoService.getAll().subscribe({
      next: data => {
        this.pedidos = Array.isArray(data) ? data : [];
        this.cargandoLista = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.pedidos = [];
        this.cargandoLista = false;
        this.cdr.detectChanges();
      }
    });
    this.inventarioService.getAll().subscribe({
      next: data => {
        this.inventarios = data ?? [];
        this.cdr.detectChanges();
      },
      error: () => { this.inventarios = []; this.cdr.detectChanges(); }
    });
  }

  onInventarioChange(): void {
    this.inventarioSeleccionado = this.inventarios.find(i => i.id === +this.nuevo.inventarioId) || null;
    this.calcularPrecio();
  }

  onCantidadChange(): void {
    this.calcularPrecio();
  }

  calcularPrecio(): void {
    if (this.inventarioSeleccionado && this.nuevo.cantidad > 0) {
      this.nuevo.precio = this.inventarioSeleccionado.precio * this.nuevo.cantidad;
    }
  }

  crearPedido(): void {
    this.loading = true;
    this.error = '';
    this.success = '';

    this.pedidoService.create(this.nuevo).pipe(finalize(() => this.loading = false)).subscribe({
      next: pedido => {
        this.success = `Pedido #${pedido.id} creado. Stock descontado automáticamente.`;
        this.resetFormulario();
        this.cargarDatos();
      },
      error: e => {
        this.error = e.error || 'Error al crear pedido. Verifique el stock disponible.';
      }
    });
  }

  avanzarEstado(pedido: Pedido): void {
    const siguiente = this.getSiguienteEstado(pedido.estado!);
    if (!siguiente) return;
    this.pedidoService.cambiarEstado(pedido.id!, siguiente).subscribe({
      next: () => {
        this.success = '';
        this.cargarDatos();
      },
      error: () => this.error = 'Error al cambiar estado'
    });
  }

  cancelarPedido(pedido: Pedido): void {
    if (!confirm('¿Cancelar este pedido?')) return;
    this.pedidoService.cambiarEstado(pedido.id!, 'CANCELADO').subscribe({
      next: () => this.cargarDatos(),
      error: () => this.error = 'Error al cancelar pedido'
    });
  }

  eliminar(id: number): void {
    if (!confirm('¿Eliminar este pedido?')) return;
    this.pedidoService.delete(id).subscribe({
      next: () => this.cargarDatos(),
      error: () => this.error = 'Error al eliminar'
    });
  }

  getSiguienteEstado(estado: EstadoPedido): EstadoPedido | null {
    const flujo: Partial<Record<EstadoPedido, EstadoPedido>> = {
      'PENDIENTE': 'CONFIRMADO',
      'CONFIRMADO': 'EN_PREPARACION'
    };
    return flujo[estado] ?? null;
  }

  getSiguienteLabel(estado?: EstadoPedido): string {
    const labels: Partial<Record<EstadoPedido, string>> = {
      'PENDIENTE': 'Confirmar',
      'CONFIRMADO': 'Preparar'
    };
    return estado ? (labels[estado] ?? '') : '';
  }

  puedeAvanzar(estado?: EstadoPedido): boolean {
    return estado === 'PENDIENTE' || estado === 'CONFIRMADO';
  }

  puedeCancelar(estado?: EstadoPedido): boolean {
    return !!estado && estado !== 'ENTREGADO' && estado !== 'CANCELADO';
  }

  getBadgeClass(estado?: EstadoPedido): string {
    const clases: Partial<Record<EstadoPedido, string>> = {
      'PENDIENTE': 'badge-warning',
      'CONFIRMADO': 'badge-info',
      'EN_PREPARACION': 'badge-orange',
      'ENVIADO': 'badge-purple',
      'ENTREGADO': 'badge-success',
      'CANCELADO': 'badge-danger'
    };
    return estado ? (clases[estado] ?? '') : '';
  }

  private resetFormulario(): void {
    this.nuevo = { clienteNombre: '', descripcion: '', cantidad: 1, inventarioId: 0 };
    this.inventarioSeleccionado = null;
  }
}
