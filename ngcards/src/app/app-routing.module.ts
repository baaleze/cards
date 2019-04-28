import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { GamelistComponent } from './gamelist/gamelist.component';

const routes: Routes = [
  { path: 'games', component: GamelistComponent },
  { path: '', redirectTo: 'games', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
