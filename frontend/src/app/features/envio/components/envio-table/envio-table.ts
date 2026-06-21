import {Component, Input} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: "app-envio-table",
  imports: [CommonModule],
  templateUrl: "./envio-table.html",
  styleUrl: "./envio-table.css",
})
export class EnvioTable {
  @Input() titulo = '';
  @Input() columnas: string[] = [];
  @Input() datos: any[] = [];
}
