import { Injectable } from '@angular/core';

import { webSocket, WebSocketSubject } from 'rxjs/webSocket';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {

  constructor() { }

  getConnection$(): WebSocketSubject<Message> {
    return webSocket<Message>(`ws://${window.location.host}/ws`);
    //return webSocket<Message>(`ws://localhost:7070/ws`);
  }

}

export class Message {
  type: string;
  message?: string;
  errorCode?: string;
  data?: object;
}
