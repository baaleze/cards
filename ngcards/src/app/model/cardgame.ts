import { User } from './user';

export class CardGameInfo {
    state: string;
    currentPlayer?: User;
    boards: Board[];
    supply: Tile[];
    players: User[];
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