import { Component, OnInit } from '@angular/core';
import { Game } from '../model/game';
import { WebsocketService } from '../websocket.service';
import { SessionService } from '../session.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-gamelist',
  templateUrl: './gamelist.component.html',
  styleUrls: ['./gamelist.component.scss']
})
export class GamelistComponent implements OnInit {

  games: Game[];
  gameTitle = '';
  joining: string;

  constructor(private websocket: WebsocketService, public session: SessionService,
    private router: Router) { }

  ngOnInit() {
    this.websocket.connect().subscribe(
      m => {
        const username = this.session.getConnectedUser() ? this.session.getConnectedUser().name : '';
        if (m.type === 'GAME_LIST') {
          this.games = m.data;
          // check if joined
          if (this.joining) {
            const game = this.games.find(g => g.name === this.joining);
            if (game && username !== '' && game.players.find(u => username === u.name)) {
              // ok I could join go into the game
              this.playGame(game);
            }
          }
        } else if (m.type === 'ERROR' && m.errorCode === 'GAME_FULL') {
          alert('Game is full!!');
          this.joining = undefined;
        }
      }
    );
  }

  createGame() {
    if (this.gameTitle !== '') {
      this.websocket.send({type: 'CREATE_GAME', message: this.gameTitle});
      this.gameTitle = '';
    }
  }

  playerInGame(game: Game): boolean {
    const user = this.session.getConnectedUser();
    if (user) {
      return game.players.find(u => u.id === user.id) !== undefined;
    }
    return false;
  }

  joinGame(game: Game) {
    this.websocket.send({type: 'JOIN_GAME', message: game.name});
    this.joining = game.name;
  }

  playGame(game: Game) {
    // go to game page!
    this.session.setCurrentGame(game);
    this.router.navigateByUrl(`/game/${game.name}`);
  }

}
