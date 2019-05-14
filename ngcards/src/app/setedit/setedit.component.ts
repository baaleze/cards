import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-setedit',
  templateUrl: './setedit.component.html',
  styleUrls: ['./setedit.component.scss']
})
export class SeteditComponent implements OnInit {

  @Input() setName: string;
  @Input() setList: string[][];
  @Output() save = new EventEmitter<string[][]>();
  headers: string[] = [];
  data: object[] = [];

  constructor() {}

  ngOnInit() {
    // build objects
    // get headers first
    this.headers = this.setList[0];
    this.setList.forEach((v, i) => {
      if (i > 0) {
        const o = {};
        this.headers.forEach((h, j) => {
          o[h] = v[j];
        });
        this.data.push(o);
      }
    });
  }

  delete(item: object) {
    this.data.splice(this.data.indexOf(item), 1);
  }

  duplicate(item: object) {
    const o = {};
    this.headers.forEach(h => {
      o[h] = item[h];
    });
    this.data.push(o);
  }

  newLine() {
    const o = {};
    this.headers.forEach(h => {
      o[h] = undefined;
    });
    this.data.push(o);
  }

  saveSet() {
    // rebuild array
    const toSave: string[][] = [];
    toSave.push(this.headers);
    this.data.forEach(row => {
      toSave.push(this.headers.map(h => row[h]));
    });
    this.save.emit(toSave);
  }

}
