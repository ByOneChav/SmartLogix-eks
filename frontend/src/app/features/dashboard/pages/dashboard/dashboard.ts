import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { DashboardCard } from '../../components/dashboard-card/dashboard-card';
import { DashboardTable } from '../../components/dashboard-table/dashboard-table';
import { PedidoService } from '../../../pedidos/services/pedido.service';
import { InventarioService } from '../../../inventario/services/inventario.service';
import { EnvioService } from '../../../envio/services/envio.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DashboardCard, DashboardTable],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {

  columnas = ['ID', 'Cliente', 'Descripción', 'Cant.', 'Precio', 'Estado'];
  pedidos: any[] = [];

  totalInventario = '—';
  totalPedidos = '—';
  totalEnvios = '—';

  constructor(
    private pedidoService: PedidoService,
    private inventarioService: InventarioService,
    private envioService: EnvioService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.pedidoService.getAll().subscribe({
      next: data => {
        const lista = Array.isArray(data) ? data : [];
        this.totalPedidos = String(lista.length);
        this.pedidos = lista.map(p => ({
          'ID': p.id,
          'Cliente': p.clienteNombre,
          'Descripción': p.descripcion,
          'Cant.': p.cantidad,
          'Precio': `$${p.precio ?? 0}`,
          'Estado': p.estado
        }));
        this.cdr.detectChanges();
      },
      error: () => {
        this.pedidos = [];
        this.totalPedidos = '0';
        this.cdr.detectChanges();
      }
    });

    this.inventarioService.getAll().subscribe({
      next: data => {
        this.totalInventario = String((data ?? []).length);
        this.cdr.detectChanges();
      },
      error: () => {
        this.totalInventario = '0';
        this.cdr.detectChanges();
      }
    });

    this.envioService.getAll().subscribe({
      next: data => {
        this.totalEnvios = String(data.length);
        this.cdr.detectChanges();
      },
      error: () => {
        this.totalEnvios = '0';
        this.cdr.detectChanges();
      }
    });
  }
}
