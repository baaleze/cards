import { Component, OnInit } from '@angular/core';
import { Game } from '../model/game';
import { Router } from '@angular/router';
import { SessionService } from '../session.service';
import { WebsocketService } from '../websocket.service';
import { User } from '../model/user';
import { ImmortalGameInfo } from '../model/immortal';

@Component({
  selector: 'app-immortal',
  templateUrl: './immortal.component.html',
  styleUrls: ['./immortal.component.scss']
})
export class ImmortalComponent implements OnInit {

    game: Game;
    info: ImmortalGameInfo;
    me: number;
    left: number;
    right: number;
    front: number;

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
  }

  draft(cardId: number) {
      // TODO
  }
}