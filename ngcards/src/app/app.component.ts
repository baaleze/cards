import { Component, OnInit } from '@angular/core';
import { WebsocketService, Message } from './websocket.service';
import { WebSocketSubject } from 'rxjs/webSocket';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  socket$: WebSocketSubject<Message>;
  chatContent: string[] = [];
  chatInput: string;
  nameInput = '';
  name: string;

  constructor(private websocket: WebsocketService) {}

  ngOnInit() {
  }

  login() {
    this.connect(this.nameInput);
  }

  logout() {
    this.socket$.complete();
    this.name = undefined;
  }

  sendChatMessage() {
    if (this.socket$) {
      this.socket$.next({type: 'CHAT', message: this.chatInput});
      this.chatInput = '';
    } else {
      console.error('NO CONNECTION');
    }
  }

  connect(name: string) {
    // get a socket
    this.socket$ = this.websocket.getConnection$();
    this.socket$.subscribe(
      m => this.handleMessage(m),
      error => this.handleConnectionError(error),
      () => {
        console.warn('connection to server is gone!');
        this.name = undefined;
      }
    )
    this.socket$.next({type: "LOGIN", message: name});
    this.name = name;
  }

  handleMessage(message: Message): void {
    switch(message.type) {
      case 'CHAT':
        this.chatContent.unshift(`\n${message.message}`);
        break;
      case 'ERROR':
        this.handleError(message.errorCode, message.message)
    }
  }

  handleError(code: string, message: string) {
    switch(code) {
      case 'NOT_LOGGED_IN':
        this.name = undefined;
        break;
    }
  }

  handleConnectionError(error: any) {
    this.name = undefined;
    console.error(error);
  }

}
