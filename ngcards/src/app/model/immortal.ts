import { User } from "./user";
import { ImmortalComponent } from "../immortal/immortal.component";

export class ImmortalGameInfo {
    state: string;
    currentPlayer?: User;
    playerInfo: ImmortalPlayerInfo[];
    allCards: number[];
    discard: number[];
    commerce: string[];
    discardCommerce: string[];
    round: number;
    currentWonderUse: number;
    drafting: boolean;
    cylakTopCard: number;
    chaosPortalCards: number[];
    commerceChoice: string[];
    narashimaCulture: number;
}

export class ImmortalPlayerInfo {
    user: User;
    gold: number;
    hand: number[];
    buildings: number[];
    heroes: number[];
    chaos: number;
    science: number;
    war: number;
    wonder: number;
    supremacy: number;
    pointTokens: number;
    diamonds: number;
}