import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { InventarioService } from '../../services/inventario.service';
import { Inventario } from '../../models/inventario.model';

@Component({
  selector: 'app-inventario',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inventario.html',
  styleUrl: './inventario.css'
})
export class InventarioComponent implements OnInit {

  inventarios: Inventario[] = [];

  nuevo: Inventario = {
    nombreProducto: '',
    ubicacion: '',
    stock: 0,
    stockMinimo: 5,
    precio: 0
  };

  editando = false;
  idEditando: number | null = null;
  loading = false;
  cargandoLista = true;
  error = '';

  constructor(private inventarioService: InventarioService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.cargarInventario();
  }

  cargarInventario(): void {
    this.cargandoLista = true;

    this.inventarioService.getAll().subscribe({
      next: data => {
        this.inventarios = data ?? [];
        this.cargandoLista = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.inventarios = [];
        this.error = 'Error al cargar inventario';
        this.cargandoLista = false;
        this.cdr.detectChanges();
      }
    });
  }

  guardar(): void {
    this.loading = true;
    this.error = '';

    const op = this.editando && this.idEditando !== null
      ? this.inventarioService.update(this.idEditando, this.nuevo)
      : this.inventarioService.create(this.nuevo);

    op.pipe(finalize(() => this.loading = false)).subscribe({
      next: () => {
        this.resetFormulario();
        this.cargarInventario();
      },
      error: () => {
        this.error = 'Error al guardar producto';
      }
    });
  }

  editar(item: Inventario): void {
    this.nuevo = { ...item };
    this.editando = true;
    this.idEditando = item.id!;
    this.error = '';
  }

  cancelarEdicion(): void {
    this.resetFormulario();
  }

  eliminar(id: number): void {
    if (!confirm('¿Eliminar este producto del inventario?')) return;
    this.inventarioService.delete(id).subscribe({
      next: () => this.cargarInventario(),
      error: () => this.error = 'Error al eliminar'
    });
  }

  esStockBajo(item: Inventario): boolean {
    return item.stockMinimo !== undefined && item.stock <= item.stockMinimo;
  }

  private resetFormulario(): void {
    this.nuevo = { nombreProducto: '', ubicacion: '', stock: 0, stockMinimo: 5, precio: 0 };
    this.editando = false;
    this.idEditando = null;
  }
}
