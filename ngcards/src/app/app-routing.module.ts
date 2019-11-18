import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { GamelistComponent } from './gamelist/gamelist.component';
import { ImmortalComponent } from './immortal/immortal.component';

const routes: Routes = [
  { path: 'game/:name', component: ImmortalComponent},
  { path: 'games', component: GamelistComponent },
  { path: '', redirectTo: 'games', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
