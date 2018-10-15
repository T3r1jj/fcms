import './App.css';

import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogTitle from '@material-ui/core/DialogTitle';
import { createMuiTheme, MuiThemeProvider } from '@material-ui/core/styles';
import * as React from 'react';

import Configuration from './component/Configuration';
import Record from './component/Record';
import Upload from './component/Upload';
import logo from './logo.svg';
import IApiKey from './model/IApiKey';
import IConfiguration from './model/IConfiguration';
import IRecord from './model/IRecord';

class App extends React.Component<{}, IAppProps> {
  private readonly theme = createMuiTheme({
    typography: {
      useNextVariants: true,
    },
  });
  private recordData = {} as IRecord;
  private configData: IConfiguration = {
    apiKeys: [{ name: "Api 1", key: "key 1", primary: true, enabled: true } as IApiKey] as IApiKey[]
  };

  constructor(props: any) {
    super(props);
    this.state = { configOpen: false };
    this.handleConfigClick = this.handleConfigClick.bind(this);
    this.handleConfigClose = this.handleConfigClose.bind(this);
  }

  public render() {
    this.recordData.id = "test";

    return (
      <MuiThemeProvider theme={this.theme}>
        <div className="App">
          <header className="App-header">
            <img src={logo} className="App-logo" alt="logo" />
            <h1 className="App-title">Welcome to React</h1>
          </header>
          <p className="App-intro">
            To get started, edit <code>src/App.tsx</code> and save to reload.
        </p>
          <Record {...this.recordData} />
          <Upload />
          <Button onClick={this.handleConfigClick}>Configuration</Button>
          <Dialog onClose={this.handleConfigClose} aria-labelledby="simple-dialog-title" open={this.state.configOpen}>
            <DialogTitle id="simple-dialog-title">Configuration</DialogTitle>
            <Configuration {...this.configData} />
          </Dialog>
        </div>
      </MuiThemeProvider>
    );
  }

  private handleConfigClick() {
    this.setState({ configOpen: true })
  }
  private handleConfigClose() {
    this.setState({ configOpen: false })
  }
}

interface IAppProps {
  configOpen: boolean;
}

export default App;
