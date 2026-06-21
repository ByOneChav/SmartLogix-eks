import {Component, Input} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: "app-dashboard-table",
  imports: [
    CommonModule
  ],
  templateUrl: "./dashboard-table.html",
  styleUrl: "./dashboard-table.css",
})
export class DashboardTable {
  @Input() titulo = '';
  @Input() columnas: string[] = [];
  @Input() datos: any[] = [];
}
