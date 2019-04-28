import { Component, OnInit } from '@angular/core';
import { Game } from '../model/game';
import { WebsocketService } from '../websocket.service';

@Component({
  selector: 'app-gamelist',
  templateUrl: './gamelist.component.html',
  styleUrls: ['./gamelist.component.scss']
})
export class GamelistComponent implements OnInit {

  games: Game[];
  gameTitle = '';

  constructor(private websocket: WebsocketService) { }

  ngOnInit() {
    this.websocket.connect().subscribe(
      m => {
        if (m.type === 'GAME_LIST') {
          this.games = m.data;
        }
      }
    )
  }

  createGame() {
    if (this.gameTitle !== '') {
      this.websocket.send({type: 'CREATE_GAME', message: this.gameTitle});
      this.gameTitle = '';
    }
  }

}
