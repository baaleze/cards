import { Injectable, EventEmitter } from '@angular/core';

import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { Subject, interval } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {

  socket$: WebSocketSubject<Message>;
  connectionState: EventEmitter<boolean> = new EventEmitter();
  out$: Subject<Message>;

  constructor() {
    // keep alive
    interval(20000).subscribe(
      next => {
        if (this.socket$) {
          this.socket$.next({type: 'PING'});
        }
      }
    );
  }

  getConnection$() {
      return webSocket<Message>(`ws://${window.location.host}/ws`);
      //return webSocket<Message>(`ws://localhost:8080/ws`);
  }

  connect(reconnect = false): Subject<Message> {
    if (reconnect || !this.socket$) {
      // get a socket
      this.socket$ = this.getConnection$();
      this.out$ = new Subject<Message>();
      this.socket$.subscribe(
        m => {
          console.log('WS IN', m);
          if (m.type !== "PONG") {
            this.out$.next(m);
          }
        },
        error => this.handleConnectionError(error),
        () => {
          console.warn('connection to server is gone!');
          this.connectionState.emit(false);
          this.socket$ = undefined;
        }
      );
      this.connectionState.emit(true);
    }
    return this.out$;
  }

  send(message: Message) {
    console.log('WS OUT', message);
    this.socket$.next(message);
  }

  handleConnectionError(error: any) {
    this.connectionState.emit(false);
    console.error(error);
  }

}

export class Message {
  type: string;
  message?: string;
  errorCode?: string;
  data?: any;
}
