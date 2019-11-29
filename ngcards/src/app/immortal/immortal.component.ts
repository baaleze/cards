import { Component, OnInit } from '@angular/core';
import { Game } from '../model/game';
import { Router } from '@angular/router';
import { SessionService } from '../session.service';
import { WebsocketService } from '../websocket.service';
import { User } from '../model/user';
import { ImmortalGameInfo } from '../model/immortal';
import { Observable, of } from 'rxjs';

@Component({
  selector: 'app-immortal',
  templateUrl: './immortal.component.html',
  styleUrls: ['./immortal.component.scss']
})
export class ImmortalComponent implements OnInit {

    game: Game;
    info: ImmortalGameInfo;
    hand: number[] =  [];
    me: number;
    left: number;
    right: number;
    front: number;
    meIndex: number;
    leftIndex: number;
    rightIndex: number;
    frontIndex: number;

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
  
  refreshGame(data: ImmortalGameInfo) {
    this.info = data;
    const user = this.session.getConnectedUser();
    // set player ids
    if (this.left === undefined) {
      this.me = user.id;
      this.meIndex = this.info.playerInfo.findIndex(p => p.user.id === this.me);
      if (this.info.playerInfo.length === 4) {
        this.leftIndex = (this.meIndex + 1) % 4;
        this.frontIndex = (this.meIndex + 2) % 4;
        this.rightIndex = (this.meIndex + 3) % 4;
        this.left = this.info.playerInfo[this.leftIndex].user.id;
        this.front = this.info.playerInfo[this.frontIndex].user.id;
        this.right = this.info.playerInfo[this.rightIndex].user.id;
      } else {
        // 3
        this.leftIndex = (this.meIndex + 1) % 4;
        this.rightIndex = (this.meIndex + 2) % 4;
        this.left = this.info.playerInfo[this.leftIndex].user.id;
        this.right = this.info.playerInfo[this.rightIndex].user.id;
      }
    }
    // draft hand
    if (this.info.drafting) {
      this.hand = this.info.playerInfo[this.meIndex].hand;
    } else {
      this.hand = [];
    }
  }

  onClick(userId: number, cardId: number) {

  }

  draft(cardId: number) {
    this.getDraftResult(cardId).subscribe(
      params => this.websocket.send({
        type: 'PLAY_CARD', data: {}
      })
    );
  }

  getDraftResult(cardId: number): Observable<string> {
    switch (cardId) {
      default:
        return of('');
    }
  }
}
