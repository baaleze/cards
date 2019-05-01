import { Component, OnInit, Input } from '@angular/core';
import { Tile } from '../model/cardgame';

@Component({
  selector: 'app-tile',
  templateUrl: './tile.component.html',
  styleUrls: ['./tile.component.scss']
})
export class TileComponent implements OnInit {

  @Input() tile: Tile;

  a = [
    'zero',
    'one ',
    'two ',
    'three ',
    'four ',
    'five ',
    'six ',
    'seven ',
    'eight ',
    'nine ',
    'ten ',
    'eleven ',
    'twelve ',
    'thirteen ',
    'fourteen ',
    'fifteen ',
    'sixteen ',
    'seventeen ',
    'eighteen ',
    'nineteen '];

  

  constructor() { }

  ngOnInit() {
  }

  numberToText(num: number): any {
    if (num != undefined) {
        return this.a[num];
    } else {
      return '';
    }
  }

}
