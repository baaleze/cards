import { Component, OnInit } from '@angular/core';
import { Game } from '../model/game';
import { Router } from '@angular/router';
import { SessionService } from '../session.service';
import { WebsocketService } from '../websocket.service';
import { CardGameInfo, Board, Tile, UserInfo } from '../model/cardgame';
import { User } from '../model/user';

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
  selected: Tile;
  tileOver: [number, number] = [-1, -1];
  chosenSpot: [number, number] = [-1, -1];
  chosenDir = 0;
  winner: User;
  pathUsed: number[] = [];

  constructor(private websocket: WebsocketService, public session: SessionService,
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

  startGame() {
    this.websocket.send({type:"START_GAME"});
  }

  selectPosition(i: number, j: number) {
    if (this.selected
      && i !== this.board.tiles.length / 2 && j !== this.board.tiles.length / 2) {
      this.chosenSpot = [i,j];
    }
  }

  pass() {
    this.websocket.send({type:"PASS"});
  }

  playTile() {
    this.websocket.send({type:"PLAY_TILE", data: {
      tileId: this.selected.id,
      x: this.chosenSpot[0],
      y: this.chosenSpot[1],
      direction: this.selected.direction
    }});
    this.cancel();
  }

  useTokens() {
    this.websocket.send({type: "USE_TOKENS", data: {
      nbMinusTokens: this.minus,
      nbPlusTokens: this.plus
    }});
  }

  selectToBuy(tile: Tile) {
    if (this.canAfford(tile.cost)) {
      this.selected = new Tile();
      this.selected.cost = tile.cost;
      this.selected.direction = 0;
      this.selected.directions = tile.directions;
      this.selected.gold = tile.gold;
      this.selected.minusTokens = tile.minusTokens;
      this.selected.plusTokens = tile.plusTokens;
      this.selected.points = tile.points;
      this.selected.id = tile.id;
    }
  }

  cancel() {
    this.chosenSpot = [-1,-1];
    this.chosenDir = 0;
    this.selected = undefined;
  }

  rotate() {
    if (this.selected) {
      this.selected.direction = (this.selected.direction + 1) % 6;
    }
  }

  canAfford(cost: number): boolean {
    return this.info.currentPlayer.id === this.session.getConnectedUser().id &&
      this.info.players.find(p => p.user.id === this.info.currentPlayer.id).gold >= cost;
  }

  moreToken(which: 'minus' | 'plus') {
    const p = this.info.players.find(p => p.user.id === this.info.currentPlayer.id);
    if (which === 'minus') {
      this.minus = Math.min(p.minusTokens, this.minus+1);
    } else {
      this.plus = Math.min(p.plusTokens, this.plus+1);
    }
  }

  lessToken(which: 'minus' | 'plus') {
    if (which === 'minus') {
      this.minus = Math.max(0, this.minus-1);
    } else {
      this.plus = Math.max(0, this.plus-1);
    }
  }

  refreshGame(data: CardGameInfo) {
    this.info = data;
    // resresh current player board
    if (this.info.currentPlayer) {
      this.board = this.info.boards.find(b => b.user.id === this.session.getConnectedUser().id);
      if (this.info.currentPlayer.id === this.session.getConnectedUser().id) {
        this.pathUsed = this.info.pathUsed;
      } else {
        this.pathUsed = [];
      }
    }
    if(this.info.state === 'AWAITING_USE_TOKENS') {
      this.plus = 0;
      this.minus = 0;
    }
    if (this.info.state === 'ENDED') {
      let win = this.info.players[0];
      this.info.players.forEach(p => {
        if (p.points > win.points) {
          win = p;
        }
      });
      this.winner = win.user;
    }
  }

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

}
