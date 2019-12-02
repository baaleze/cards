import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { GamelistComponent } from './gamelist/gamelist.component';
import { OriflameComponent } from './oriflame/oriflame.component';

const routes: Routes = [
  { path: 'game/:name', component: OriflameComponent},
  { path: 'games', component: GamelistComponent },
  { path: '', redirectTo: 'games', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
