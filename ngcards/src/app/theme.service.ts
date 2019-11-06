import { Injectable } from '@angular/core';

export class Theme {
  empty: string;
  text: string;
  shadow: string;
  primary: string;
  backgroundPrimary: string;
  default: string;
  backgroundDefault: string;
  error: string;
  backgroundError: string;
  warn: string;
  backgroundWarn: string;
  accent: string;
  backgroundAccent: string;
  success: string;
  backgroundSuccess: string;
  cancel: string;
  backgroundCancel: string;
}

export const darkTheme: Theme = {
  empty: '#002b36',
  shadow: '#073642',
  text: '#839496',
  primary: '#268bd2',
  backgroundPrimary: '#1f2935bf',
  default: '#93a1a1',
  backgroundDefault: '#073642bf',
  error: '#dc322f',
  backgroundError: '#dc322fbf',
  success: '#859900',
  backgroundSuccess: '#859900bf',
  warn: '#b58900bf',
  backgroundWarn: '#b58900bf',
  accent: '#d33682',
  backgroundAccent: '#d33682bf',
  cancel: '#2aa198',
  backgroundCancel: '#2aa198bf'
};

export const lightTheme = {
  empty: '#fdf6e3',
  shadow: '#eee8d5',
  text: '#657b83',
  primary: '#268bd2',
  backgroundPrimary: '#1f2935bf',
  default: '#586e75',
  backgroundDefault: '#586e75bf',
  error: '#dc322f',
  backgroundError: '#dc322fbf',
  success: '#859900',
  backgroundSuccess: '#859900bf',
  warn: '#b58900',
  backgroundWarn: '#b58900bf',
  accent: '#d33682',
  backgroundAccent: '#d33682bf',
  cancel: '#2aa198',
  backgroundCancel: '#2aa198bf'
};

@Injectable({
  providedIn: 'root'
})
export class ThemeService {

  constructor() { }

  toggleTheme(t: 'DARK' | 'LIGHT') {
    switch (t) {
      case 'DARK':
        this.setTheme(darkTheme);
        break;
      case 'LIGHT':
        this.setTheme(lightTheme);
        break;
    }
  }

  private setTheme(theme: {}) {
    Object.keys(theme).forEach(k =>
      document.documentElement.style.setProperty(`--${k}`, theme[k])
    );
  }
}
