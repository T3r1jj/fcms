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
import Payload from "../../model/event/Payload";
import {ClientPayloadType, PayloadType} from "../../model/event/PayloadType";
import Progress from "../../model/event/Progress";
import IBackup from "../../model/IBackup";
import IRecord from "../../model/IRecord";
import Record from "../../model/Record";
import SearchItem from "../../model/SearchItem";
import Configuration from "../Configuration";
import RecordNode from "../RecordNode";
import Upload from "../Upload";

export default class MainPage extends React.Component<IMainPageProps, IMainPageState> {
    private readonly steps = ["Update config with API credentials", "Upload a record to the server", "Wait for the server to finish replication"];

    constructor(props: IMainPageProps) {
        super(props);
        this.state = {configOpen: false, progresses: [], records: [], expand: false};
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
        this.setState({records}, this.handleSearchItemsUpdate);
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
                }, this.handleSearchItemsUpdate);
            }, (i - 99) * 1000)
        }
    }

    public componentWillReceiveProps(nextProps: Readonly<IMainPageProps>, nextContext: any): void {
        if (nextProps.payload !== undefined && nextProps.payload !== this.props.payload) {
            const payload = nextProps.payload;
            let action: string;
            let name: string;
            let clientPayloadType: ClientPayloadType | undefined;
            if (payload.type === PayloadType.SAVE) {
                const record = payload.record!;
                name = record.name;
                const records = [...this.state.records];
                const indexToUpdate = records.findIndex(r => r.id === record.id);
                if (indexToUpdate >= 0) {
                    clientPayloadType = ClientPayloadType.UPDATE;
                    action = "Updated";
                    records.splice(indexToUpdate, 1, record);
                } else {
                    clientPayloadType = ClientPayloadType.ADD;
                    action = "Added";
                    records.push(record);
                }
                this.setState({records}, this.handleSearchItemsUpdate);
                payload.onConsume!(`${action} ${name}`, clientPayloadType);
            } else if (payload.type === PayloadType.DELETE) {
                const record = payload.record!;
                clientPayloadType = ClientPayloadType.DELETE;
                action = "Deleted";
                const records = [...this.state.records];
                const indexToDelete = records.findIndex(r => r.id === record.id);
                if (indexToDelete >= 0) {
                    name = records[indexToDelete].name;
                    records.splice(indexToDelete, 1);
                    this.setState({records}, this.handleSearchItemsUpdate);
                } else { // Should not happen
                    name = record.id;
                }
                payload.onConsume!(`${action} ${name}`, clientPayloadType);
            } else if (payload.type === PayloadType.PROGRESS) {
                const progress = payload.progress!;
                const progresses = [...this.state.progresses];
                const foundProgressIndex = progresses.findIndex(p => p.id === progress.id);
                progress.timeoutId = window.setTimeout(() => {
                    const futureProgresses = [...this.state.progresses];
                    const progressIndexToRemove = futureProgresses.findIndex(p => p.id === progress.id);
                    futureProgresses.splice(progressIndexToRemove, 1);
                    this.setState({progresses: futureProgresses});
                }, 1000 * (progress.bytesWritten >= progress.bytesTotal ? 5 : 60));
                if (foundProgressIndex >= 0) {
                    window.clearTimeout(this.state.progresses[foundProgressIndex].timeoutId);
                    progresses.splice(foundProgressIndex, 1, progress);
                } else {
                    progresses.push(progress);
                }
                this.setState({progresses});
            }
        }
    }

    public componentWillUnmount(): void {
        this.props.onSearchItemsUpdate();
    }

    public render() {
        return (
            <div>
                <Stepper>
                    {this.steps.map((label) =>
                        <Step key={label} active={true} completed={false} disabled={false}>
                            <StepLabel active={true} completed={false} disabled={false}>{label}</StepLabel>
                        </Step>
                    )}
                </Stepper>
                <Upload parentId={this.state.currentParentRecordId}
                        progresses={this.state.progresses}
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

    private handleSearchItemsUpdate = () => {
        this.props.onSearchItemsUpdate(this.getSearchItems(this.state.records))
    };

}

export interface IMainPageProps {
    client: Client;
    payload?: Payload;

    onSearchItemsUpdate(items?: SearchItem[]): void;
}

interface IMainPageState {
    configOpen: boolean;
    records: IRecord[];
    progresses: Progress[];
    expand: boolean;
    currentParentRecordId?: string;
}
