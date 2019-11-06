import { Component, OnInit } from '@angular/core';
import { WebsocketService, Message } from './websocket.service';
import { Subject, Subscription } from 'rxjs';
import { SessionService } from './session.service';
import { User } from './model/user';
import { ThemeService } from './theme.service';

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
  connected = false;
  connection: Subscription;
  userList: User[] = [];
  currentTheme: 'DARK' | 'LIGHT' = 'LIGHT';

  constructor(private websocket: WebsocketService, public session: SessionService,
    private theme: ThemeService) {}

  ngOnInit() {
    this.websocket.connectionState.subscribe(
      connectionState => {
        if (connectionState && this.nameInput !== '') {
          // connected, do login
          this.websocket.send({type: 'LOGIN', message: this.nameInput});
        } else if (!connectionState) {
          // disconnected
          this.session.setConnectedUser(undefined);
          this.socket$.unsubscribe();
        }
        this.connected = connectionState;
      }
    );
    this.theme.toggleTheme(this.currentTheme);
  }

  toggleTheme() {
    this.theme.toggleTheme(this.currentTheme);
  }

  login() {
    this.socket$ = this.websocket.connect();
    this.connection = this.socket$.subscribe(
      m => this.handleMessage(m)
    );

    // login if already connected
    if (this.connected) {
      this.websocket.send({type: 'LOGIN', message: this.nameInput});
    }
  }

  logout() {
    this.session.setConnectedUser(undefined);
    this.websocket.send({type: 'LOGOUT'});
    this.connection.unsubscribe();
  }

  sendChatMessage() {
    this.websocket.send({type: 'CHAT', message: this.chatInput});
    this.chatInput = '';
  }

  handleMessage(message: Message): void {
    switch (message.type) {
      case 'CHAT':
        this.chatContent.unshift(`\n${message.message}`);
        break;
      case 'ERROR':
        this.handleError(message.errorCode, message.message);
        break;
      case 'USER_LIST':
      const userList = <User[]> message.data;
        if (!this.session.getConnectedUser()) {
          // check if I logged in
          const user = userList.find(u => u.name === this.nameInput && u.ip === window.location.hostname);
          if (user) {
            this.session.setConnectedUser(user);
          }
        }
        // update user list
        this.userList = userList;
    }
  }

  handleError(code: string, message: string) {
    switch (code) {
      case 'NOT_LOGGED_IN':
        this.session.setConnectedUser(undefined);
        break;
    }
  }

}
