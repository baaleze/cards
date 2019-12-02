import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Card } from '../model/oriflame';

@Component({
  selector: 'app-oriflamecard',
  templateUrl: './oriflamecard.component.html',
  styleUrls: ['./oriflamecard.component.scss']
})
export class OriflameCardComponent {

    @Input() card: Card;
    @Input() cardIndex: number;
    @Output() clicked: EventEmitter<number> = new EventEmitter<number>();

    constructor() {}

}
