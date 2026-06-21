import { ComponentFixture, TestBed } from "@angular/core/testing";

import { Envio } from "./envio";

describe("Envio", () => {
  let component: Envio;
  let fixture: ComponentFixture<Envio>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Envio],
    }).compileComponents();

    fixture = TestBed.createComponent(Envio);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
