import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { EnvioService } from '../../services/envio.service';
import { Envio, EstadoEnvio } from '../../models/envio.model';
import { PedidoService } from '../../../pedidos/services/pedido.service';
import { Pedido } from '../../../pedidos/models/pedido.model';

@Component({
  selector: 'app-envio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './envio.html',
  styleUrl: './envio.css'
})
export class EnvioComponent implements OnInit {

  envios: Envio[] = [];
  pedidosDisponibles: Pedido[] = [];

  nuevo: Envio = {
    pedidoId: 0,
    direccionDestino: ''
  };

  loading = false;
  cargandoLista = true;
  error = '';
  success = '';

  constructor(
    private envioService: EnvioService,
    private pedidoService: PedidoService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargandoLista = true;
    this.envioService.getAll().subscribe({
      next: data => {
        this.envios = Array.isArray(data) ? data : [];
        this.cargandoLista = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.envios = [];
        this.cargandoLista = false;
        this.cdr.detectChanges();
      }
    });
    this.pedidoService.getAll().subscribe({
      next: data => {
        this.pedidosDisponibles = Array.isArray(data)
          ? data.filter(p => p.estado === 'CONFIRMADO' || p.estado === 'EN_PREPARACION')
          : [];
        this.cdr.detectChanges();
      },
      error: () => { this.pedidosDisponibles = []; this.cdr.detectChanges(); }
    });
  }

  crearEnvio(): void {
    this.loading = true;
    this.error = '';
    this.success = '';

    this.envioService.create(this.nuevo).pipe(finalize(() => this.loading = false)).subscribe({
      next: envio => {
        this.success = `Envío #${envio.id} creado para pedido #${envio.pedidoId}`;
        this.resetFormulario();
        this.cargarDatos();
      },
      error: e => {
        this.error = e.error || 'Error al crear envío. Verifique el ID del pedido.';
      }
    });
  }

  avanzarEstado(envio: Envio): void {
    const siguiente = this.getSiguienteEstado(envio.estado!);
    if (!siguiente) return;
    this.envioService.cambiarEstado(envio.id!, siguiente).subscribe({
      next: () => {
        this.success = '';
        this.cargarDatos();
      },
      error: () => this.error = 'Error al cambiar estado'
    });
  }

  devolverEnvio(envio: Envio): void {
    if (!confirm('¿Marcar este envío como DEVUELTO?')) return;
    this.envioService.cambiarEstado(envio.id!, 'DEVUELTO').subscribe({
      next: () => this.cargarDatos(),
      error: () => this.error = 'Error al actualizar estado'
    });
  }

  eliminar(id: number): void {
    if (!confirm('¿Eliminar este envío?')) return;
    this.envioService.delete(id).subscribe({
      next: () => this.cargarDatos(),
      error: () => this.error = 'Error al eliminar'
    });
  }

  getSiguienteEstado(estado: EstadoEnvio): EstadoEnvio | null {
    const flujo: Partial<Record<EstadoEnvio, EstadoEnvio>> = {
      'PREPARANDO': 'EN_TRANSITO',
      'EN_TRANSITO': 'ENTREGADO'
    };
    return flujo[estado] ?? null;
  }

  getSiguienteLabel(estado?: EstadoEnvio): string {
    const labels: Partial<Record<EstadoEnvio, string>> = {
      'PREPARANDO': 'En Tránsito',
      'EN_TRANSITO': 'Entregar'
    };
    return estado ? (labels[estado] ?? '') : '';
  }

  puedeAvanzar(estado?: EstadoEnvio): boolean {
    return estado === 'PREPARANDO' || estado === 'EN_TRANSITO';
  }

  puedeDevolver(estado?: EstadoEnvio): boolean {
    return estado === 'EN_TRANSITO';
  }

  getBadgeClass(estado?: EstadoEnvio): string {
    const clases: Partial<Record<EstadoEnvio, string>> = {
      'PREPARANDO': 'badge-orange',
      'EN_TRANSITO': 'badge-info',
      'ENTREGADO': 'badge-success',
      'DEVUELTO': 'badge-danger'
    };
    return estado ? (clases[estado] ?? '') : '';
  }

  private resetFormulario(): void {
    this.nuevo = { pedidoId: 0, direccionDestino: '' };
  }
}
