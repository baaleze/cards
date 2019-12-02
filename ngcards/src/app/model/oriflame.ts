import { User } from './user';

export class OriflameGameInfo {
    constructor(
        public currentPlayer: User,
        public state: string,
        public board: Card[][],
        public players: OriflamePlayerInfo[]
        ) {}
}

export class OriflamePlayerInfo {
    constructor(
        public user: User,
        public hand: Card[],
        public discard: Card[],
        public points: number
    ) {}
}

export class Card {
    constructor(
        public index: number,
        public owner: number,
        public revealed: boolean,
        public points: number
    ) {}
}
