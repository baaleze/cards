import { Component, OnInit } from '@angular/core';
import { WebsocketService, Message } from './websocket.service';
import { Subject, Subscription } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  socket$: Subject<Message>;
  chatContent: string[] = [];
  chatInput: string;
  nameInput = '';
  name: string;
  connected = false;
  connection: Subscription;

  constructor(private websocket: WebsocketService) {}

  ngOnInit() {
    this.websocket.connectionState.subscribe(
      connectionState => {
        if (connectionState && this.nameInput !== '') {
          // connected, do login
          this.websocket.send({type: "LOGIN", message: this.nameInput});
          this.name = this.nameInput;
        } else if (!connectionState) {
          // disconnected
          this.name = undefined;
          this.socket$.unsubscribe();
        }
        this.connected = connectionState;
      }
    )
  }

  login() {
    this.socket$ = this.websocket.connect();
    this.connection = this.socket$.subscribe(
      m => this.handleMessage(m)
    );
    
    // login if already connected
    if (this.connected) {
      this.websocket.send({type: "LOGIN", message: this.nameInput});
          this.name = this.nameInput;
    }
    
  }

  logout() {
    this.name = undefined;
    this.websocket.send({type: 'LOGOUT'});
    this.connection.unsubscribe();
  }

  sendChatMessage() {
    this.websocket.send({type: 'CHAT', message: this.chatInput});
    this.chatInput = '';
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

}
