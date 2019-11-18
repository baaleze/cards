import { Component, OnInit } from '@angular/core';
import { User } from '../model/user';
import { ImmortalPlayerInfo } from '../model/immortal';

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