import {createMuiTheme, MuiThemeProvider} from '@material-ui/core/styles';
import {SnackbarProvider} from "notistack";
import * as React from 'react';
import {Route} from "react-router";
import {BrowserRouter} from "react-router-dom";
import './App.css';
import HistoryPage from "./component/pages/HistoryPage";
import MainPage from "./component/pages/MainPage";
import PrimarySearchAppBar from "./component/PrimarySearchAppBar";
import {Client} from "./model/Client";
import Notifications from './notification/Notifications';

class App extends React.Component<{}, {}> {
    private readonly theme = createMuiTheme({
        palette: {
            primary: {
                contrastText: '#fff',
                dark: '#002884',
                light: '#757ce8',
                main: '#3f50b5',
            },
            secondary: {
                contrastText: '#000',
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
        this.renderMainPage = this.renderMainPage.bind(this);
    }

    public render() {
        return (
            <BrowserRouter>
                <SnackbarProvider maxSnack={100} anchorOrigin={{horizontal: 'right', vertical: 'bottom'}}
                                  autoHideDuration={1000 * 60 * 15}>
                    <MuiThemeProvider theme={this.theme}>
                        <Notifications subscribeToNotifications={this.client.subscribeToNotifications}/>
                        <div className="App">
                            <PrimarySearchAppBar/>

                            <Route path="/" exact={true} render={this.renderMainPage}/>
                            <Route path="/history" component={HistoryPage}/>
                        </div>
                    </MuiThemeProvider>
                </SnackbarProvider>
            </BrowserRouter>
        );
    }

    private renderMainPage() {
        return <MainPage client={this.client}/>
    }
}


export default App;
