import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-immortalcard',
  templateUrl: './immortalcard.component.html',
  styleUrls: ['./immortalcard.component.scss']
})
export class ImmortalCardComponent {

    @Input() cardId: number;
    @Output() clicked: EventEmitter<number> = new EventEmitter<number>();

    constructor() {}

}
