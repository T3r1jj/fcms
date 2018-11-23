import * as React from 'react';
import * as ReactDOM from 'react-dom';
import "reflect-metadata"
import App from './App';
import './index.css';
import registerServiceWorker from './registerServiceWorker';

(window.console as any).ignoredYellowBox = ['Warning:'];
ReactDOM.render(
  <App />,
  document.getElementById('root') as HTMLElement
);
registerServiceWorker();
