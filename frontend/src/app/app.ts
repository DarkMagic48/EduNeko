import { Component } from '@angular/core';
import { AuthService } from './services/auth.service';
import { LoginRequest } from './models/auth.models';


@Component({
  selector: 'app-root',
  standalone: true,
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {

   constructor(private readonly authService: AuthService) {}
}