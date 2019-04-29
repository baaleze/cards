import { Injectable } from '@angular/core';
import { User } from './model/user';
import { Game } from './model/game';

@Injectable({
  providedIn: 'root'
})
export class SessionService {

  connectedAs: User;
  currentGame: Game;

  constructor() { }

  setConnectedUser(u: User) {
    this.connectedAs = u;
  }

  getConnectedUser(): User {
    return this.connectedAs;
  }

  setCurrentGame(game: Game) {
    this.currentGame = game;
  }

  getCurrentGame(): Game {
    return this.currentGame;
  }
}
