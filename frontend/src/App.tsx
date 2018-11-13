import {List, ListItem} from '@material-ui/core';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogTitle from '@material-ui/core/DialogTitle';
import {createMuiTheme, MuiThemeProvider} from '@material-ui/core/styles';
import {SnackbarProvider} from "notistack";
import * as React from 'react';
import './App.css';

import Configuration from './component/Configuration';
import PrimarySearchAppBar from "./component/PrimarySearchAppBar";
import Record, {IRecordProps} from './component/Record';
import Upload from './component/Upload';
import logo from './logo.svg';
import {Client} from "./model/Client";
import IRecord from './model/IRecord';
import Notifications from './notification/Notifications';

class App extends React.Component<{}, IAppProps> {
    private readonly theme = createMuiTheme({
        typography: {
            useNextVariants: true,
        },
    });
    private recordData: IRecord = {
        backups: [],
        description: "test",
        id: "1",
        meta: [],
        name: "test",
        versions: [{name: "test", id: "2", description: "test", tag: "v2", versions: [], meta: [], backups: []}]
    };
    private records = [this.recordData, {...this.recordData, id: "2", name: "Another"}];
    private config = new Client();


    constructor(props: any) {
        super(props);
        this.state = {configOpen: false};
        this.handleConfigClick = this.handleConfigClick.bind(this);
        this.handleConfigClose = this.handleConfigClose.bind(this);
    }

    public render() {
        this.recordData.id = "test";

        return (
            <SnackbarProvider maxSnack={100} anchorOrigin={{horizontal: 'right', vertical: 'bottom'}}
                              autoHideDuration={1000 * 60 * 15}>
                <MuiThemeProvider theme={this.theme}>
                    <Notifications subscribeToNotifications={this.config.subscribeToNotifications}/>
                    <div className="App">
                        <PrimarySearchAppBar/>
                        <header className="App-header">
                            <img src={logo} className="App-logo" alt="logo"/>
                            <h1 className="App-title">FCMS</h1>
                        </header>
                        <p className="App-intro">
                            Upload file or a new version for a backup management
                        </p>
                        <Upload isUploadValid={this.isUploadValid}/>
                        <Button variant="contained" onClick={this.handleConfigClick}>Configuration</Button>
                        <Dialog onClose={this.handleConfigClose} aria-labelledby="simple-dialog-title"
                                open={this.state.configOpen}>
                            <DialogTitle id="simple-dialog-title">Configuration</DialogTitle>
                            <Configuration {...this.config}/>
                        </Dialog>
                        <List className="list">
                            {this.prepareRecordsProps(this.records).map(r =>
                                <ListItem key={r.id}>
                                    <Record {...r} />
                                </ListItem>
                            )}
                        </List>
                    </div>
                </MuiThemeProvider>
            </SnackbarProvider>
        );
    }

    private handleConfigClick() {
        this.setState({configOpen: true})
    }

    private handleConfigClose() {
        this.setState({configOpen: false})
    }

    private prepareRecordsProps(records: IRecord[]): IRecordProps[] {
        return records.map(r => {
            const props: IRecordProps = {
                ...r,
                hierarchyTooltipEnabled: records[0] === r,
                onDescriptionChange: (id, description) => {
                    window.console.log("Changed " + id + " description to " + description)
                }
            }
            return props
        })
    }

    private isUploadValid(file: File, name: string, parent: string, tag: string): boolean {
        return false
    }
}

interface IAppProps {
    configOpen: boolean;
}

export default App;
