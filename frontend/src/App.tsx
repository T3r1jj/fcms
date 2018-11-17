import {createMuiTheme, MuiThemeProvider} from '@material-ui/core/styles';
import {SnackbarProvider} from "notistack";
import * as React from 'react';
import {Route} from "react-router";
import {BrowserRouter} from "react-router-dom";
import './App.css';
import CodeCallbackPage from "./component/page/CodeCallbackPage";
import HistoryPage from "./component/page/HistoryPage";
import MainPage from "./component/page/MainPage";
import Client from "./model/Client";
import Event from "./model/event/Event";
import Notifications from './notification/Notifications';

class App extends React.Component<{}, IAppState> {
    private readonly theme = createMuiTheme({
        palette: {
            primary: {
                contrastText: '#fff',
                dark: '#002884',
                light: '#757ce8',
                main: '#3f50b5',
            },
            secondary: {
                contrastText: '#fff',
                dark: '#ba000d',
                light: '#ff7961',
                main: '#f44336',
            },
        },
        typography: {
            useNextVariants: true,
        },
    });

    private client = new Client();

    constructor(props: any) {
        super(props);
        this.state = {
            newEvent: undefined
        };
        this.renderMainPage = this.renderMainPage.bind(this);
        this.renderHistoryPage = this.renderHistoryPage.bind(this);
        this.renderCodeCallbackPage = this.renderCodeCallbackPage.bind(this);
        this.onEventReceived = this.onEventReceived.bind(this);
    }

    public render() {
        return (
            <BrowserRouter>
                <SnackbarProvider maxSnack={100} anchorOrigin={{horizontal: 'right', vertical: 'bottom'}}
                                  autoHideDuration={1000 * 60 * 15}>
                    <MuiThemeProvider theme={this.theme}>
                        <Notifications {...this.client} onEventReceived={this.onEventReceived}/>
                        <div className="App">
                            <Route path="/" exact={true} render={this.renderMainPage}/>
                            <Route path="/history" render={this.renderHistoryPage}/>
                            <Route path="/code" render={this.renderCodeCallbackPage}/>
                        </div>
                    </MuiThemeProvider>
                </SnackbarProvider>
            </BrowserRouter>
        );
    }

    private renderMainPage() {
        return <MainPage client={this.client}/>
    }

    private renderHistoryPage() {
        return <HistoryPage getHistoryPage={this.client.getHistoryPage}
                            getHistory={this.client.getHistory}
                            newEvent={this.state.newEvent}/>;
    }

    private renderCodeCallbackPage() {
        return <CodeCallbackPage getCodeCallback={this.client.getCodeCallback}
                                 checkCodeCallback={this.client.checkCodeCallback}
                                 updateCodeCallback={this.client.updateCodeCallback}/>;
    }

    private onEventReceived(event: Event) {
        this.setState({newEvent: event});
    }
}

interface IAppState {
    newEvent?: Event;
}

export default App;
