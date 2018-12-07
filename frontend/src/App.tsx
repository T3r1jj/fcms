import {Tooltip} from "@material-ui/core";
import LinearProgress from "@material-ui/core/LinearProgress";
import {createMuiTheme, MuiThemeProvider} from '@material-ui/core/styles';
import AddIcon from '@material-ui/icons/AddBox';
import ErrorIcon from '@material-ui/icons/BugReport';
import DeleteIcon from '@material-ui/icons/Delete';
import SuccessIcon from '@material-ui/icons/DoneOutline';
import InfoIcon from '@material-ui/icons/Info';
import UpdateIcon from '@material-ui/icons/Update';
import WarningIcon from '@material-ui/icons/Warning';
import {SnackbarProvider} from "notistack";
import * as React from 'react';
import {Redirect, Route, Switch} from "react-router";
import {BrowserRouter, Link} from "react-router-dom";
import './App.css';
import CodeCallbackPage from "./component/page/CodeCallbackPage";
import HistoryPage from "./component/page/HistoryPage";
import LoginPage from "./component/page/LoginPage";
import MainPage from "./component/page/MainPage";
import PrimarySearchAppBar from "./component/PrimarySearchAppBar";
import Client from "./model/Client";
import Event from "./model/event/Event";
import {EventType} from "./model/event/EventType";
import Payload from "./model/event/Payload";
import {PayloadType} from "./model/event/PayloadType";
import Progress from "./model/event/Progress";
import SearchItem from "./model/SearchItem";
import Notifications from './notification/Notifications';

const iconVariants = {
    add: <AddIcon/>,
    delete: <DeleteIcon/>,
    error: <ErrorIcon/>,
    info: <InfoIcon/>,
    success: <SuccessIcon/>,
    update: <UpdateIcon/>,
    warning: <WarningIcon/>,
};

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

    constructor(props: any) {
        super(props);
        this.state = {
            unreadEventsCount: 0,
        };
        this.renderMainPage = this.renderMainPage.bind(this);
        this.renderHistoryPage = this.renderHistoryPage.bind(this);
        this.renderCodeCallbackPage = this.renderCodeCallbackPage.bind(this);
        this.onEventReceived = this.onEventReceived.bind(this);
        this.onEventDismiss = this.onEventDismiss.bind(this);
    }

    public render() {
        return (
            <BrowserRouter>
                <SnackbarProvider maxSnack={100} anchorOrigin={{horizontal: 'right', vertical: 'bottom'}}
                                  iconVariant={iconVariants as any}
                                  autoHideDuration={1000 * 60 * 15}>
                    {this.state.client ? (<MuiThemeProvider theme={this.theme}>
                            <Notifications {...this.state.client!!} eventToDismiss={this.state.readEvent}
                                           onEventReceived={this.onEventReceived}
                                           onEventDismiss={this.onEventDismiss}/>
                            <div className="App">
                                <PrimarySearchAppBar
                                    onLogout={this.onAuthenticated}
                                    status={this.state.status}
                                    unreadCount={this.state.unreadEventsCount}
                                    searchItems={this.state.searchItems}/>
                                {this.state.replicationProgress && this.state.replicationProgress.done !== this.state.replicationProgress.total &&
                                <Tooltip
                                    title={`REPLICATING ${this.state.replicationProgress.done} of ${this.state.replicationProgress.total}`}>
                                    <LinearProgress variant="determinate" color="secondary"
                                                    value={100 * this.state.replicationProgress.done / this.state.replicationProgress.total}/>
                                </Tooltip>
                                }
                                <Switch>
                                    <Route path="/" exact={true} render={this.renderMainPage}/>
                                    <Route path="/history" exact={true} render={this.renderHistoryPage}/>
                                    <Route path="/code" exact={true} render={this.renderCodeCallbackPage}/>
                                    <Route component={this.NoMatch}/>
                                </Switch>
                            </div>
                        </MuiThemeProvider>) :
                        (<MuiThemeProvider theme={this.theme}>
                            <Switch>
                                <Redirect exact={true} from={"/"} to={{pathname: "/login", state: {referrer: "/"}}}/>
                                <Redirect exact={true} from={"/history"}
                                          to={{pathname: "/login", state: {referrer: "/history"}}}/>
                                <Redirect exact={true} from={"/code"}
                                          to={{pathname: "/login", state: {referrer: "/code"}}}/>
                                <Route path="/login" exact={true} render={this.renderLoginPage}/>
                                <Route component={this.NoMatch}/>
                            </Switch>
                        </MuiThemeProvider>)
                    }
                </SnackbarProvider>
            </BrowserRouter>
        );
    }

    private NoMatch = () => {
        return (
            <div className={"centerScreen"}>
                <h3>
                    404
                    <br/>
                    Not Found
                    <br/>
                    <Link to={"/"}>Go Home, You Are Drunk :]</Link>
                </h3>
            </div>
        );
    };

    private renderLoginPage = () => {
        return <LoginPage onAuthenticated={this.onAuthenticated}/>
    };

    private renderMainPage() {
        return <MainPage
            payload={this.state.payload}
            onSearchItemsUpdate={this.onSearchItemsUpdate}
            onStatusChange={this.onStatusChange}
            client={this.state.client!!}/>
    }

    private renderHistoryPage() {
        return <HistoryPage
            setHistoryAsRead={this.state.client!!.setHistoryAsRead}
            deleteHistory={this.state.client!!.deleteHistory}
            onEventRead={this.onEventRead}
            setEventAsRead={this.state.client!!.setEventAsRead}
            getHistoryPage={this.state.client!!.getHistoryPage}
            getHistory={this.state.client!!.getHistory}
            newEvent={this.state.newOrDismissedEvent}/>;
    }

    private renderCodeCallbackPage() {
        return <CodeCallbackPage getCodeCallback={this.state.client!!.getCodeCallback}
                                 checkCodeCallback={this.state.client!!.checkCodeCallback}
                                 updateCodeCallback={this.state.client!!.updateCodeCallback}/>;
    }

    private onEventReceived(event: Event) {
        if (event.type === EventType.PAYLOAD) {
            const payload = event.payload!;
            if (payload.type === PayloadType.PROGRESS) {
                payload.progress!.id = event.id;
                payload.progress!.action = event.title;
            }
            if (payload.type === PayloadType.REPLICATION_PROGRESS) {
                this.setState({replicationProgress: payload.progress!})
            } else {
                this.setState({payload: event.payload});
            }
        } else {
            this.setState({newOrDismissedEvent: event, unreadEventsCount: this.state.unreadEventsCount + 1});
        }
    }

    private onEventDismiss(event: Event) {
        if (!event.read) {
            this.state.client!!.setEventAsRead(event)
                .then(r => this.setState({
                    newOrDismissedEvent: event,
                    unreadEventsCount: this.state.unreadEventsCount - 1
                }))
        }
    }

    private onEventRead = (event: Event, all?: boolean) => {
        this.setState({
            newOrDismissedEvent: undefined,
            readEvent: event,
            unreadEventsCount: all ? 0 : this.state.unreadEventsCount - 1
        });
    };

    private onSearchItemsUpdate = (searchItems?: SearchItem[]) => {
        this.setState({searchItems});
    };

    private onStatusChange = (status: string) => {
        this.setState({status});
    };

    private onAuthenticated = (client?: Client, redirectCallback?: () => void) => {
        if (client !== undefined) {
            client.countUnreadEvents()
                .then(r => r.ok ? r.text() : new Promise((resolve => resolve("-1"))))
                .then(countText => this.setState({client, unreadEventsCount: +countText}, redirectCallback))
        } else {
            if (this.state.client) {
                this.state.client.logout();
            }
            this.setState({client});
        }
    };
}

interface IAppState {
    readEvent?: Event;
    newOrDismissedEvent?: Event;
    searchItems?: SearchItem[];
    payload?: Payload;
    replicationProgress?: Progress;
    unreadEventsCount: number;
    status?: string;
    client?: Client;
}

export default App;
