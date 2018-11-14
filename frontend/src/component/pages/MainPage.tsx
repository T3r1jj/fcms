import Button from "@material-ui/core/Button/Button";
import Dialog from "@material-ui/core/Dialog/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle/DialogTitle";
import List from "@material-ui/core/List/List";
import ListItem from "@material-ui/core/ListItem/ListItem";
import * as React from "react";
import {Client} from "../../model/Client";
import IRecord from "../../model/IRecord";
import Configuration from "../Configuration";
import Record, {IRecordProps} from "../Record";
import Upload from "../Upload";

export default class MainPage extends React.Component<IMainPageProps, IMainPageState> {
    private recordData: IRecord = {
        backups: [],
        description: "test",
        id: "1",
        meta: [],
        name: "test",
        versions: [{name: "test", id: "2", description: "test", tag: "v2", versions: [], meta: [], backups: []}]
    };
    private records = [this.recordData, {...this.recordData, id: "2", name: "Another"}];

    constructor(props: IMainPageProps) {
        super(props);
        this.state = {configOpen: false};
        this.handleConfigClick = this.handleConfigClick.bind(this);
        this.handleConfigClose = this.handleConfigClose.bind(this);
        this.recordData.id = "test";
    }

    public render() {
        return (
            <div>
                <p className="App-intro">
                    Upload file or a new version for a backup management
                </p>
                <Upload isUploadValid={this.isUploadValid}/>
                <Button variant="contained" onClick={this.handleConfigClick}>Configuration</Button>
                <Dialog onClose={this.handleConfigClose} aria-labelledby="simple-dialog-title"
                        open={this.state.configOpen}>
                    <DialogTitle id="simple-dialog-title">Configuration</DialogTitle>
                    <Configuration updateConfiguration={this.props.client.updateConfiguration}
                                   getConfiguration={this.props.client.getConfiguration}/>
                </Dialog>
                <List className="list">
                    {this.prepareRecordsProps(this.records).map(r =>
                        <ListItem key={r.id}>
                            <Record {...r} />
                        </ListItem>
                    )}
                </List>
            </div>
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
            return {
                ...r,
                hierarchyTooltipEnabled: records[0] === r,
                onDescriptionChange: (id, description) => {
                    window.console.log("Changed " + id + " description to " + description)
                }
            } as IRecordProps
        })
    }

    private isUploadValid(file: File, name: string, parent: string, tag: string): boolean {
        return false
    }
}

interface IMainPageProps {
    client: Client;
}

interface IMainPageState {
    configOpen: boolean;
}
