import {Tooltip} from '@material-ui/core';
import Button from "@material-ui/core/Button/Button";
import Dialog from "@material-ui/core/Dialog/Dialog";
import DialogActions from "@material-ui/core/DialogActions/DialogActions";
import DialogContent from "@material-ui/core/DialogContent/DialogContent";
import DialogContentText from "@material-ui/core/DialogContentText/DialogContentText";
import DialogTitle from "@material-ui/core/DialogTitle/DialogTitle";
import IconButton from '@material-ui/core/IconButton';
import BackupIcon from '@material-ui/icons/Backup';
// import AttachmentIcon from "@material-ui/core/SvgIcon/SvgIcon";
import NewIcon from '@material-ui/icons/CreateNewFolder';
import DeleteIcon from '@material-ui/icons/Delete';
import ForceDeleteIcon from '@material-ui/icons/DeleteForever';
import VersionIcon from '@material-ui/icons/FiberNew';
import TextIcon from '@material-ui/icons/TextFormat';
import * as React from 'react';
import IRecord from '../model/IRecord';
import Formatter from "../utils/Formatter";
import Description from "./Description";

export default class RecordNode extends React.Component<IRecordProps, IRecordState> {
    constructor(props: IRecordProps) {
        super(props);
        this.state = {
            deletionOpen: false,
            description: this.props.description,
            descriptionOpen: false,
            selectedRecord: undefined
        };
        this.onRecordSelected = this.onRecordSelected.bind(this);
        this.handleDescriptionClose = this.handleDescriptionClose.bind(this);
        this.handleDescriptionOpen = this.handleDescriptionOpen.bind(this);
        this.handleDescriptionChange = this.handleDescriptionChange.bind(this);
        this.handleDescriptionSave = this.handleDescriptionSave.bind(this);
        this.handleDeletionOpen = this.handleDeletionOpen.bind(this);
        this.handleDeletionClose = this.handleDeletionClose.bind(this);
        this.handleDelete = this.handleDelete.bind(this);
        this.handleForceDelete = this.handleForceDelete.bind(this);
        this.handleNewRecord = this.handleNewRecord.bind(this);
    }

    public render() {
        return (
            <div id={this.props.id}>
                <Tooltip title="Create new record" onClick={this.handleNewRecord}>
                    <IconButton aria-label="Create new record"><NewIcon/></IconButton>
                </Tooltip>
                {this.props.name} {this.getSizeText()} :: Records: {this.getRecordsCount(this.props)},
                Depth: {this.getVersionsDepth(this.props)}, Backups: {this.getBackupsCount(this.props)}
                {Array.from(this.props.backups.entries()).map(([service, backup]) =>
                    <Tooltip key={service} title={"Backup " + service}>
                        <IconButton aria-label="Backup"><BackupIcon/></IconButton>
                    </Tooltip>
                )}
                <Tooltip title="Description" onClick={this.handleDescriptionOpen}>
                    <IconButton aria-label="Description"><TextIcon/></IconButton>
                </Tooltip>
                <Tooltip title="Delete" onClick={this.handleDeletionOpen}>
                    <IconButton aria-label="Delete"><DeleteIcon/></IconButton>
                </Tooltip>
                {/*{this.props.meta.map(m =>*/}
                {/*<Tooltip key={m.id} title={"Meta " + m.name}>*/}
                {/*<IconButton id={m.id} aria-label="Meta"*/}
                {/*onClick={this.onRecordSelected}><AttachmentIcon/></IconButton>*/}
                {/*</Tooltip>*/}
                {/*)}*/}
                {this.props.versions.map(v =>
                    <Tooltip key={v.id} title={"Version " + v.tag}>
                        <IconButton id={v.id} aria-label="Version"
                                    onClick={this.onRecordSelected}><VersionIcon/></IconButton>
                    </Tooltip>
                )}
                {this.state.selectedRecord ? <RecordNode {...this.state.selectedRecord} /> :
                    (this.props.hierarchyTooltipEnabled ? <div>Select child record for more info</div> : null)
                }
                <Dialog onClose={this.handleDescriptionClose}
                        aria-labelledby={"description-dialog-title" + this.props.id}
                        open={this.state.descriptionOpen}>
                    <DialogTitle id={"description-dialog-title" + this.props.id}>Description</DialogTitle>
                    <Description rawText={this.textOrEmpty(this.state.description)}
                                 onChange={this.handleDescriptionChange}/>
                    <Button onClick={this.handleDescriptionSave}>Save</Button>
                    <Button onClick={this.handleDescriptionClose}>Cancel</Button>
                </Dialog>
                <Dialog
                    open={this.state.deletionOpen}
                    onClose={this.handleDeletionClose}
                    aria-labelledby="alert-dialog-title"
                    aria-describedby="alert-dialog-description"
                >
                    <DialogTitle id="alert-dialog-title">{"Delete record?"}</DialogTitle>
                    <DialogContent>
                        <DialogContentText id="alert-dialog-description">
                            Are you sure you want to delete this version <b>and all of its children</b>?<br/>
                            The <b><i>deletion</i></b> will stop in case of any error.<br/>
                            <b><i>Forced deletion</i></b> will not stop in case of an error, and it may leave some
                            backups online.
                        </DialogContentText>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={this.handleDelete} color="secondary">
                            <DeleteIcon/>
                            Delete
                        </Button>
                        <Button onClick={this.handleForceDelete} color="secondary">
                            <ForceDeleteIcon/>
                            Force delete
                        </Button>
                        <Button onClick={this.handleDeletionClose} color="primary" variant={"contained"}
                                autoFocus={true}>
                            Cancel
                        </Button>
                    </DialogActions>
                </Dialog>
            </div>
        );
    }

    private textOrEmpty(text: string) {
        return text !== null ? text : "";
    }

    private onRecordSelected(event: React.SyntheticEvent<HTMLElement>) {
        let eventTarget = event.target as any;
        let recordId = eventTarget.getAttribute('id');
        while (recordId == null) {
            eventTarget = eventTarget.parentElement;
            recordId = eventTarget.getAttribute('id');
        }
        const record = this.props.versions.find(v => v.id === recordId);
        const newlySelectedRecord: IRecordProps = {
            ...record!,
            deleteRecords: this.props.deleteRecords,
            forceDeleteRecords: this.props.forceDeleteRecords,
            hierarchyTooltipEnabled: false,
            root: false,
            updateParentId: this.props.updateParentId,
            updateRecordDescription: this.props.updateRecordDescription,
        };
        this.setState({
            selectedRecord: this.state.selectedRecord ?
                (this.state.selectedRecord.id === newlySelectedRecord.id ? undefined : newlySelectedRecord)
                : newlySelectedRecord
        });
    }

    private getRecordsCount(props: IRecord): number {
        let recordsCount = 1;
        props.versions.forEach(v => recordsCount += this.getRecordsCount(v));
        return recordsCount
    }

    private getVersionsDepth(props: IRecord): number {
        return Math.max(0, Math.max.apply(Math, props.versions.map(v => 1 + this.getVersionsDepth(v))))
    }

    private getBackupsCount(props: IRecord): number {
        let backupsCount = props.backups.size;
        props.versions.forEach(v => backupsCount += this.getRecordsCount(v));
        return backupsCount;
    }

    private handleDescriptionClose() {
        this.setState({description: this.props.description, descriptionOpen: false});
    }

    private handleDeletionClose() {
        this.setState({deletionOpen: false});
    }

    private handleDescriptionOpen() {
        this.setState({descriptionOpen: true});
    }

    private handleDeletionOpen() {
        this.setState({deletionOpen: true});
    }

    private handleDescriptionChange(event: React.ChangeEvent<HTMLTextAreaElement>) {
        this.setState({description: event.target.value});
    }

    private handleDescriptionSave() {
        this.props.updateRecordDescription(this.props.id, this.state.description)
            .then(r => this.setState({descriptionOpen: false}));
    }

    private handleDelete() {
        this.props.deleteRecords(this.props.id)
            .then(r => this.setState({deletionOpen: false}))
    }

    private handleForceDelete() {
        this.props.forceDeleteRecords(this.props.id)
            .then(r => this.setState({deletionOpen: false}));
    }

    private getSizeText() {
        if (this.props.root) {
            return "[" + Formatter.formatBytes(this.getSize(this.props)) + "]";
        } else {
            return "";
        }
    }

    private getSize(props: IRecord): number {
        return Math.max.apply(null, [0, ...Array.from(props.backups.values()).map(b => b.size)]);
    }

    private handleNewRecord() {
        this.props.updateParentId(this.props.id);
    }

}

interface IRecordState {
    selectedRecord?: IRecordProps;
    descriptionOpen: boolean;
    deletionOpen: boolean;
    description: string;
}

export interface IRecordProps extends IRecord {
    hierarchyTooltipEnabled: boolean;
    root: boolean;

    updateRecordDescription(id: string, description: string): Promise<Response>;

    deleteRecords(id: string): Promise<Response>;

    forceDeleteRecords(id: string): Promise<Response>;

    updateParentId(id: string): void;

}