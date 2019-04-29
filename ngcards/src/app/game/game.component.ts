import { Component, OnInit } from '@angular/core';
import { Game } from '../model/game';
import { Router } from '@angular/router';
import { SessionService } from '../session.service';
import { WebsocketService } from '../websocket.service';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.scss']
})
export class GameComponent implements OnInit {

  game: Game;

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
  refreshGame(data: any) {
    throw new Error("Method not implemented.");
  }

}
