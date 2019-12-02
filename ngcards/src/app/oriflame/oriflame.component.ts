import { Component, OnInit } from '@angular/core';
import { Game } from '../model/game';
import { Router } from '@angular/router';
import { SessionService } from '../session.service';
import { WebsocketService } from '../websocket.service';
import { User } from '../model/user';
import { Observable, of } from 'rxjs';
import { OriflameGameInfo, Card } from '../model/oriflame';

@Component({
  selector: 'app-oriflame',
  templateUrl: './oriflame.component.html',
  styleUrls: ['./oriflame.component.scss']
})
export class OriflameComponent implements OnInit {

  allColors = [ 'red', 'blue', 'green', 'orange', 'black' ];

    game: Game;
    info: OriflameGameInfo;
    hand: Card[] =  [];
    board: Card[][];

    colors = new Map<number, string>();

    constructor(
        private websocket: WebsocketService,
        public session: SessionService,
        private router: Router) { }

      ngOnInit() {
        this.game = this.session.getCurrentGame();
        if (!this.game) {
          // no game selected
          this.router.navigateByUrl('/');
        }
        // check if I can be here
        const user = this.session.getConnectedUser();
        if (!user || !this.game.players.find(u => u.name === user.name)) {
          // not in the game !
          this.session.setCurrentGame(undefined);
          this.router.navigateByUrl('/');
        } else {
          // subscribe to socket
          this.subscribe();
          // get game state
          this.websocket.send({type: 'GET_GAME_INFO', message: this.game.name});
        }
      }

  subscribe() {
    this.websocket.connect().subscribe(
      m => {
        switch (m.type) {
          case 'GAME_INFO':
            this.refreshGame(m.data);
        }
      }
    );
  }

  refreshGame(data: OriflameGameInfo) {
    if (this.colors.values.length !== this.game.players.length && this.game.started) {
      this.game.players.forEach(p => this.colors.set(p.id, ))
    }
    this.info = data;
    const user = this.session.getConnectedUser();
    const userInfo = this.info.players.find(p => p.user.id === user.id);
    this.hand = userInfo.hand;
    this.board = this.info.board;
  }

  boardCardClicked(userId: number, cardId: number) {

  }
  handCardClicked(userId: number, cardId: number) {

  }
}
