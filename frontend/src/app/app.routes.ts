import { Routes } from '@angular/router';

import { authGuard } from './guards/auth.guard';
import { Login } from './pages/login/login';
import { Registro } from './pages/registro/registro';
import { Dashboard } from './pages/dashboard/dashboard';
import { Onboarding } from './pages/onboarding/onboarding';
import { DiagnosticoMatematicas } from './pages/diagnostico-matematicas/diagnostico-matematicas';

export const routes: Routes = [
    {
        path: 'login',
        component: Login
    },
    {
        path: 'registro',
        component: Registro
    },
    {
        path: 'onboarding',
        component: Onboarding,
        canActivate: [authGuard]
    },
    {
    path: 'diagnostico/matematicas',
    component: DiagnosticoMatematicas,
    canActivate: [authGuard]
    },
    {
        path: 'dashboard',
        component: Dashboard,
        canActivate: [authGuard],
    },
    {
        path: '',
        redirectTo: 'login',
        pathMatch: 'full'
    }
];
