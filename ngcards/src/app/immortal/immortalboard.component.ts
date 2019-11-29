import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { ImmortalPlayerInfo } from '../model/immortal';

@Component({
  selector: 'app-immortalboard',
  templateUrl: './immortalboard.component.html',
  styleUrls: ['./immortalboard.component.scss']
})
export class ImmortalBoardComponent {

    @Input() info: ImmortalPlayerInfo;
    @Output() cardClicked: EventEmitter<number> = new EventEmitter<number>();

    public tokens = [
        'gold',
        'war',
        'science',
        'chaos',
        'wonder',
        'supremacy',
        'pointTokens',
        'diamonds'
    ];

    constructor() {}

}