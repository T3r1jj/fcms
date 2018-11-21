import {Tooltip} from "@material-ui/core";
import IconButton from "@material-ui/core/IconButton/IconButton";
import List from "@material-ui/core/List/List";
import ListItem from "@material-ui/core/ListItem/ListItem";
import Step from "@material-ui/core/Step/Step";
import StepLabel from "@material-ui/core/StepLabel/StepLabel";
import Stepper from "@material-ui/core/Stepper/Stepper";
import ExpandIcon from '@material-ui/icons/ExpandLess';
import CollapseIcon from '@material-ui/icons/ExpandMore';
import * as React from "react";
import Client from "../../model/Client";
import IBackup from "../../model/IBackup";
import IRecord from "../../model/IRecord";
import Record from "../../model/Record";
import SearchItem from "../../model/SearchItem";
import Configuration from "../Configuration";
import PrimarySearchAppBar from "../PrimarySearchAppBar";
import RecordNode from "../RecordNode";
import Upload from "../Upload";

export default class MainPage extends React.Component<IMainPageProps, IMainPageState> {
    private readonly steps = ["Update config with API credentials", "Upload a record to the server", "Wait for the server to finish replication"];

    constructor(props: IMainPageProps) {
        super(props);
        this.state = {configOpen: false, records: [], expand: false};
        this.updateParentId = this.updateParentId.bind(this);
        this.handleExpand = this.handleExpand.bind(this);
    }

    public componentDidMount() {
        // this.props.client.getRecords()
        //     .then(records => this.setState({records}))
        const records: IRecord[] = [];
        for (let i = 0; i < 100; i++) {
            const record = new Record();
            record.backups = new Map<string, IBackup>();
            record.id = i.toString();
            record.name = i.toString() + " root  That way, it won't cause a rerender every time because ";
            record.versions = [];
            for (let v = 0; v < 3; v++) {
                const vrecord = new Record();
                vrecord.backups = new Map<string, IBackup>();
                vrecord.id = i.toString() + "_" + v.toString();
                vrecord.name = i.toString() + "_" + v.toString();
                vrecord.versions = [];
                if (v === 1) {
                    for (let l = 0; l < 2; l++) {
                        const lrecord = new Record();
                        lrecord.backups = new Map<string, IBackup>();
                        lrecord.id = i.toString() + "_" + v.toString() + "_" + l.toString();
                        lrecord.name = i.toString() + "_" + v.toString() + "_" + l.toString();
                        lrecord.versions = [];
                        vrecord.versions.push(lrecord);
                    }
                }
                record.versions.push(vrecord);
            }
            records.push(record);
        }
        this.setState({records});
        for (let i = 100; i < 110; i++) {
            const record = new Record();
            record.backups = new Map<string, IBackup>();
            record.id = i.toString();
            record.name = i.toString() + " root";
            record.versions = [];
            for (let v = 0; v < 3; v++) {
                const vrecord = new Record();
                vrecord.backups = new Map<string, IBackup>();
                vrecord.id = i.toString() + "_" + v.toString();
                vrecord.name = i.toString() + "_" + v.toString();
                vrecord.versions = [];
                if (v === 1) {
                    for (let l = 0; l < 2; l++) {
                        const lrecord = new Record();
                        lrecord.backups = new Map<string, IBackup>();
                        lrecord.id = i.toString() + "_" + v.toString() + "_" + l.toString();
                        lrecord.name = i.toString() + "_" + v.toString() + "_" + l.toString();
                        lrecord.versions = [];
                        vrecord.versions.push(lrecord);
                    }
                }
                record.versions.push(vrecord);
            }
            setTimeout(() => {
                this.setState({
                    records: [...this.state.records, record]
                });
            }, (i - 99) * 1000)
        }
    }

    public render() {
        return (
            <div>
                <PrimarySearchAppBar
                    searchItems={this.getSearchItems(this.state.records)}/>
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
                <Configuration updateConfiguration={this.props.client.updateConfiguration}
                               getConfiguration={this.props.client.getConfiguration}/>
                <Tooltip
                    placement="bottom"
                    title={this.state.expand ? "Expand records" : "Collapse records"}
                    onClick={this.handleExpand}>
                    <IconButton aria-label={this.state.expand ? "Expand" : "Collapse"}>
                        {this.state.expand ? (
                            <ExpandIcon/>
                        ) : (
                            <CollapseIcon/>
                        )}
                    </IconButton>
                </Tooltip>
                <List className="list records">
                    {this.state.records.map(r =>
                        <ListItem key={r.id}>
                            <RecordNode {...r}
                                        deleteRecords={this.props.client.deleteRecords}
                                        expand={this.state.expand}
                                        forceDeleteRecords={this.props.client.forceDeleteRecords}
                                        lazyLoad={true}
                                        updateParentId={this.updateParentId}
                                        hierarchyTooltipEnabled={this.state.records[0] === r}
                                        updateRecordDescription={this.props.client.updateRecordDescription}
                                        root={true}/>
                        </ListItem>
                    )}
                </List>
            </div>
        );
    }

    private getSearchItems(records: IRecord[], ids?: string[]): SearchItem[] {
        const flatMap: SearchItem[] = records.map(r => new SearchItem(r.name, ids ? [...ids, r.id] : [r.id]));
        for (const record of records) {
            flatMap.push(...this.getSearchItems(record.versions, ids ? [...ids, record.id] : [record.id]));
        }
        return flatMap;
    }

    private updateParentId(parentId: string) {
        this.setState({currentParentRecordId: parentId})
    }

    private handleExpand() {
        this.setState({expand: !this.state.expand})
    }

}

export interface IMainPageProps {
    client: Client;
}

interface IMainPageState {
    configOpen: boolean;
    records: IRecord[];
    expand: boolean;
    currentParentRecordId?: string;
}
