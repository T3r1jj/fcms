import Button from "@material-ui/core/Button/Button";
import Dialog from "@material-ui/core/Dialog/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle/DialogTitle";
import List from "@material-ui/core/List/List";
import ListItem from "@material-ui/core/ListItem/ListItem";
import Step from "@material-ui/core/Step/Step";
import StepLabel from "@material-ui/core/StepLabel/StepLabel";
import Stepper from "@material-ui/core/Stepper/Stepper";
import * as React from "react";
import Client from "../../model/Client";
import IRecord from "../../model/IRecord";
import SearchItem from "../../model/SearchItem";
import Configuration from "../Configuration";
import PrimarySearchAppBar from "../PrimarySearchAppBar";
import RecordNode from "../RecordNode";
import Upload from "../Upload";

export default class MainPage extends React.Component<IMainPageProps, IMainPageState> {
    private readonly steps = ["Update config with API credentials", "Upload a record to the server", "Wait for the server to finish replication"];

    constructor(props: IMainPageProps) {
        super(props);
        this.state = {configOpen: false, records: []};
        this.handleConfigClick = this.handleConfigClick.bind(this);
        this.handleConfigClose = this.handleConfigClose.bind(this);
        this.updateParentId = this.updateParentId.bind(this);
    }

    public componentDidMount() {
        this.props.client.getRecords()
            .then(records => this.setState({records}))
    }

    public render() {
        return (
            <div>
                <PrimarySearchAppBar
                    searchItems={this.getRootsAndChildren(this.state.records).map(r => new SearchItem(r.name, r.id))}/>
                <Stepper>
                    {this.steps.map((label) =>
                        <Step key={label} active={true} completed={false} disabled={false}>
                            <StepLabel active={true} completed={false} disabled={false}>{label}</StepLabel>
                        </Step>
                    )}
                </Stepper>
                <Upload parentId={this.state.currentParentRecordId}
                        isUploadValid={this.props.client.isUploadValid}
                        upload={this.props.client.upload}/>
                <Button variant="contained" onClick={this.handleConfigClick}>Configuration</Button>
                <Dialog onClose={this.handleConfigClose} aria-labelledby="simple-dialog-title"
                        open={this.state.configOpen}>
                    <DialogTitle id="simple-dialog-title">Configuration</DialogTitle>
                    <Configuration updateConfiguration={this.props.client.updateConfiguration}
                                   getConfiguration={this.props.client.getConfiguration}/>
                </Dialog>
                <List className="list records">
                    {this.state.records.map(r =>
                        <ListItem key={r.id}>
                            <RecordNode {...r}
                                        deleteRecords={this.props.client.deleteRecords}
                                        forceDeleteRecords={this.props.client.forceDeleteRecords}
                                        updateParentId={this.updateParentId}
                                        hierarchyTooltipEnabled={this.state.records[0] === r}
                                        updateRecordDescription={this.props.client.updateRecordDescription}
                                        root={true}
                            />
                        </ListItem>
                    )}
                </List>
            </div>
        );
    }

    private getRootsAndChildren(records: IRecord[]): IRecord[] {
        const flatMap: IRecord[] = [...records];
        for (const record of records) {
            flatMap.push(...record.versions);
        }
        return flatMap;
    }

    private handleConfigClick() {
        this.setState({configOpen: true})
    }

    private handleConfigClose() {
        this.setState({configOpen: false})
    }

    private updateParentId(parentId: string) {
        this.setState({currentParentRecordId: parentId})
    }

}

export interface IMainPageProps {
    client: Client;
}

interface IMainPageState {
    configOpen: boolean;
    records: IRecord[];
    currentParentRecordId?: string;
}
