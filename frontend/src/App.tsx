import * as React from 'react';
import './App.css';
import Record from './Record';

import logo from './logo.svg';
import { IRecord } from './model/IRecord';

class App extends React.Component {

  private recordData = {} as IRecord;

  public render() {
this.recordData.id = "test";

    return (
      <div className="App">
        <header className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
          <h1 className="App-title">Welcome to React</h1>
        </header>
        <p className="App-intro">
          To get started, edit <code>src/App.tsx</code> and save to reload.
        </p>
        <Record {...this.recordData}/>
      </div>
    );
  }
}

export default App;
