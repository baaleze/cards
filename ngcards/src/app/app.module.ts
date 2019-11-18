import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { DataTableModule } from 'angular7-data-table';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { GamelistComponent } from './gamelist/gamelist.component';
import { GameComponent } from './game/game.component';
import { UserPipe } from './user.pipe';
import { TileComponent } from './tile/tile.component';
import { SeteditComponent } from './setedit/setedit.component';
import { ImmortalComponent } from './immortal/immortal.component';
import { ImmortalBoardComponent } from './immortal/immortalboard.component';
import { ImmortalCardComponent } from './immortal/immortalcard.component';

@NgModule({
  declarations: [
    AppComponent,
    GamelistComponent,
    GameComponent,
    ImmortalComponent,
    ImmortalBoardComponent,
    ImmortalCardComponent,
    UserPipe,
    TileComponent,
    SeteditComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    AppRoutingModule,
    HttpClientModule,
    DataTableModule.forRoot()
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
