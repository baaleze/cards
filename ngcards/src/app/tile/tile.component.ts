import { Component, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Tile } from '../model/cardgame';

@Component({
  selector: 'app-tile',
  templateUrl: './tile.component.html',
  styleUrls: ['./tile.component.scss']
})
export class TileComponent implements OnInit {

  @Input() tile: Tile;
  @Input() showCost = false;
  @Input() dir: number;

  actualDirs: number[];

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
    this.actualDirs = this.shifted();
  }

  ngOnChanges(change: SimpleChanges) {
    if (change.dir !== undefined) {
      this.actualDirs = this.shifted();
    }
    if (change.tile) {
      this.actualDirs = this.shifted();
    }
  }

  shifted(): number[] {
    if (this.tile) {
      const d = [];
      this.tile.directions.forEach(n => d.push(n));
      for(let i = 0; i < this.tile.direction; i++) {
          d.unshift(d.pop());
      }
      return d;
    }
}

  numberToText(num: number): any {
    if (num != undefined) {
        return this.a[num];
    } else {
      return '';
    }
  }

}
