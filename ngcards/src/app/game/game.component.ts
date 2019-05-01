import { Component, OnInit } from '@angular/core';
import { Game } from '../model/game';
import { Router } from '@angular/router';
import { SessionService } from '../session.service';
import { WebsocketService } from '../websocket.service';
import { CardGameInfo, Board } from '../model/cardgame';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.scss']
})
export class GameComponent implements OnInit {

  game: Game;
  info: CardGameInfo;
  board: Board;
  minus: number;
  plus: number;

  constructor(private websocket: WebsocketService, public session: SessionService,
    private router: Router) { }

  ngOnInit() {
    this.game = this.session.getCurrentGame();
    if (!this.game) {
      // no game selected
      this.router.navigateByUrl('/games');
    }
    // check if I can be here
    const user = this.session.getConnectedUser();
    if (!user || !this.game.players.find(u => u.name === user.name)) {
      // not in the game !
      this.session.setCurrentGame(undefined);
      this.router.navigateByUrl('/games');
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

  startGame() {
    this.websocket.send({type:"START_GAME"});
  }

  pass() {
    this.websocket.send({type:"PASS"});
  }

  playTile() {
    // TODO
    this.websocket.send({type:"PLAY_TILE", data: {
      tileId: 0,
      x: 0,
      y: 0,
      direction: 0
    }});
  }

  useTokens() {
    // TODO
    this.websocket.send({type: "USE_TOKENS", data: {
      nbMinusTokens: this.minus,
      nbPlusTokens: this.plus
    }});
  }

  refreshGame(data: CardGameInfo) {
    this.info = data;
    // resresh current player board
    if (this.info.currentPlayer) {
      this.board = this.info.boards.find(b => b.user.id === this.info.currentPlayer.id);
    }
    if(this.info.state === 'AWAITING_USE_TOKENS') {
      this.plus = 0;
      this.minus = 0;
    }
  }

}
