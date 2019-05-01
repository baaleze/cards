import { User } from './user';

export class CardGameInfo {
    state: string;
    currentPlayer?: User;
    boards: Board[];
    supply: Tile[];
    players: UserInfo[];
    turn: number;
    diceRoll: number;
}

export class UserInfo {
    user: User;
    gold: number;
    points: number;
    minusTokens: number;
    plusTokens: number;
}

export class Board {
    user: User;
    tiles: Tile[][];
}

export class Tile {
    id: number;
    cost: number;
    direction: number;
    directions: number[];
    gold: number;
    points: number;
    minusTokens: number;
    plusTokens: number;
}