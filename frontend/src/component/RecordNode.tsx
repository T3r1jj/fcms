import {StyleRulesCallback, Tooltip, WithStyles} from '@material-ui/core';
import Button from "@material-ui/core/Button/Button";
import CircularProgress from "@material-ui/core/CircularProgress/CircularProgress";
import Dialog from "@material-ui/core/Dialog/Dialog";
import DialogActions from "@material-ui/core/DialogActions/DialogActions";
import DialogContent from "@material-ui/core/DialogContent/DialogContent";
import DialogContentText from "@material-ui/core/DialogContentText/DialogContentText";
import DialogTitle from "@material-ui/core/DialogTitle/DialogTitle";
import IconButton from '@material-ui/core/IconButton';
import withStyles from "@material-ui/core/styles/withStyles";
import TextField from "@material-ui/core/TextField";
import BackupIcon from '@material-ui/icons/Backup';
import NewIcon from '@material-ui/icons/CreateNewFolder';
import DeleteIcon from '@material-ui/icons/Delete';
import ForceDeleteIcon from '@material-ui/icons/DeleteForever';
import FolderClosedIcon from '@material-ui/icons/Folder';
import FolderOpenIcon from '@material-ui/icons/FolderOpen';
import SaveIcon from '@material-ui/icons/Save';
import TitleIcon from '@material-ui/icons/TextFields';
import TextIcon from '@material-ui/icons/TextFormat';
import {ComponentState} from 'react';
import * as React from 'react';
import LazyLoad from "react-lazyload";
import IRecord from '../model/IRecord';
import RecordMeta from "../model/RecordMeta";
import Formatter from "../utils/Formatter";
import Description from "./Description";

const styles: StyleRulesCallback = theme => ({
    descriptionActions: {
        justifyContent: "flex-start"
    },
    leftIcon: {
        marginRight: theme.spacing.unit
    },
    lightTooltip: {
        background: theme.palette.common.white,
        boxShadow: theme.shadows[1],
        color: theme.palette.text.primary,
        fontSize: 11,
    }
});

export class RecordNode extends React.Component<IRecordProps, IRecordState> {
    constructor(props: IRecordProps) {
        super(props);
        this.state = {
            deletionOpen: false,
            description: this.props.meta.description,
            descriptionOpen: false,
            expand: props.expand,
            name: this.props.meta.name,
            nameTagOpen: false,
            selectedRecord: undefined,
            tag: this.props.meta.tag
        };
        this.handleDescriptionClose = this.handleDescriptionClose.bind(this);
        this.handleDescriptionOpen = this.handleDescriptionOpen.bind(this);
        this.handleDescriptionChange = this.handleDescriptionChange.bind(this);
        this.handleDescriptionSave = this.handleDescriptionSave.bind(this);
        this.handleDeletionOpen = this.handleDeletionOpen.bind(this);
        this.handleDeletionClose = this.handleDeletionClose.bind(this);
        this.handleDelete = this.handleDelete.bind(this);
        this.handleForceDelete = this.handleForceDelete.bind(this);
        this.handleNewRecord = this.handleNewRecord.bind(this);
        this.renderRecord = this.renderRecord.bind(this);
    }

    public shouldComponentUpdate(nextProps: Readonly<IRecordProps>, nextState: Readonly<IRecordState>, nextContext: any): boolean {
        if (nextProps.id !== this.props.id || nextProps.expand !== this.props.expand) {
            return true;
        } else if (nextState !== this.state) {
            return true;
        }
        return false;
    }

    public componentWillReceiveProps(nextProps: Readonly<IRecordProps>, nextContext: any): void {
        if (this.props.expand !== nextProps.expand) {
            this.setState({expand: nextProps.expand});
        }
    }

    public onFocus = () => {
        this.setState({expand: true});
    };

    public render() {
        return this.props.lazyLoad ? (
            <LazyLoad debounce={50} height={this.getHeight()}
                      placeholder={<div id={this.props.id} tabIndex={-1} onFocus={this.onFocus} className="tab loading">
                          <CircularProgress/></div>}>
                {this.renderRecord()}
            </LazyLoad>
        ) : this.renderRecord();
    }

    private renderRecord() {
        return <div className="tab">
            <div id={this.props.id} tabIndex={-1} onFocus={this.onFocus}
                 style={{overflow: "auto", width: 0, height: 0}}>{this.props.id}
            </div>
            <Tooltip
                classes={{tooltip: this.props.classes.lightTooltip}}
                placement="top-end"
                title={this.props.meta.tag ? this.props.meta.tag : ""}
                onClick={this.handleExpand}>
                <IconButton>
                    {(this.state.expand || this.props.versions.length === 0) ? (
                        <FolderOpenIcon/>
                    ) : (
                        <FolderClosedIcon/>
                    )}
                </IconButton>
            </Tooltip>
            {this.props.meta.name} {this.getSizeText()} :: Records: {this.getRecordsCount(this.props)},
            Depth: {this.getVersionsDepth(this.props)}, Backups: {this.getBackupsCount(this.props)}
            <Tooltip
                title={"Create new record"}
                onClick={this.handleNewRecord}>
                <IconButton aria-label="Create new record"><NewIcon/></IconButton>
            </Tooltip>
            <Tooltip title="Name and tag" onClick={this.handleNameTagOpen}>
                <IconButton aria-label="Name and tag"><TitleIcon/></IconButton>
            </Tooltip>
            <Tooltip title="Description" onClick={this.handleDescriptionOpen}>
                <IconButton aria-label="Description"><TextIcon/></IconButton>
            </Tooltip>
            <Tooltip title="Delete" onClick={this.handleDeletionOpen}>
                <IconButton aria-label="Delete"><DeleteIcon/></IconButton>
            </Tooltip>
            {Array.from(this.props.backups.entries()).map(([service, backup]) =>
                <Tooltip key={service} title={"Backup " + service}>
                    <IconButton aria-label="Backup"><BackupIcon/></IconButton>
                </Tooltip>
            )}
            {this.state.expand && this.props.versions.map(v =>
                <RecordNode {...v} deleteRecords={this.props.deleteRecords}
                            key={v.id}
                            lazyLoad={this.props.lazyLoad}
                            classes={this.props.classes}
                            forceDeleteRecords={this.props.forceDeleteRecords}
                            hierarchyTooltipEnabled={false}
                            expand={this.props.expand}
                            root={false}
                            updateParentId={this.props.updateParentId}
                            updateRecordMeta={this.props.updateRecordMeta}
                />
            )}
            <Dialog
                fullWidth={true}
                open={this.state.nameTagOpen}
                onClose={this.handleDeletionClose}
                aria-labelledby="alert-dialog-title"
                aria-describedby="alert-dialog-description"
            >
                <DialogTitle id="alert-dialog-title">{"Meta"}</DialogTitle>
                <DialogContent>
                    <TextField
                        label="Name"
                        fullWidth={true}
                        value={this.state.name}
                        onChange={this.handleStateChange('name')}
                        variant="outlined"
                        margin="normal"
                    />
                    <br/>
                    <TextField
                        label="Tag"
                        fullWidth={true}
                        value={this.state.tag ? this.state.tag : ""}
                        onChange={this.handleStateChange('tag')}
                        variant="outlined"
                        margin="normal"
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={this.handleNameTagSave} variant={"contained"}>
                        <SaveIcon className={this.props.classes.leftIcon}/>
                        Save
                    </Button>
                    <Button onClick={this.handleTitleTagClose} color="primary" variant={"contained"}>Cancel</Button>
                </DialogActions>
            </Dialog>
            <Dialog onClose={this.handleDescriptionClose}
                    fullScreen={true}
                    aria-labelledby={"description-dialog-title" + this.props.id}
                    open={this.state.descriptionOpen}>
                <DialogTitle id={"description-dialog-title" + this.props.id}>Description</DialogTitle>
                <DialogContent style={{flexBasis: 0}}>
                    <Description rawText={this.textOrEmpty(this.state.description)}
                                 onChange={this.handleDescriptionChange}/>
                </DialogContent>
                <DialogActions className={this.props.classes.descriptionActions}>
                    <Button onClick={this.handleDescriptionSave} variant={"contained"}>
                        <SaveIcon className={this.props.classes.leftIcon}/>
                        Save
                    </Button>
                    <Button onClick={this.handleDescriptionClose} color="primary" variant={"contained"}>Cancel</Button>
                </DialogActions>
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
                        <DeleteIcon className={this.props.classes.leftIcon}/>
                        Delete
                    </Button>
                    <Button onClick={this.handleForceDelete} color="secondary">
                        <ForceDeleteIcon className={this.props.classes.leftIcon}/>
                        Force delete
                    </Button>
                    <Button onClick={this.handleDeletionClose} color="primary" variant={"contained"}
                            autoFocus={true}>
                        Cancel
                    </Button>
                </DialogActions>
            </Dialog>
        </div>;
    }

    private textOrEmpty(text: string) {
        return text !== null ? text : "";
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
        props.versions.forEach(v => backupsCount += this.getBackupsCount(v));
        return backupsCount;
    }

    private handleDescriptionClose() {
        this.setState({description: this.props.meta.description, descriptionOpen: false});
    }

    private handleTitleTagClose = () => {
        this.setState({name: this.props.meta.name, tag: this.props.meta.tag, nameTagOpen: false});
    }

    private handleDeletionClose() {
        this.setState({deletionOpen: false});
    }

    private handleDescriptionOpen() {
        this.setState({descriptionOpen: true});
    }

    private handleNameTagOpen = () =>  {
        this.setState({nameTagOpen: true});
    }

    private handleDeletionOpen() {
        this.setState({deletionOpen: true});
    }

    private handleDescriptionChange(event: React.ChangeEvent<HTMLTextAreaElement>) {
        this.setState({description: event.target.value});
    }

    private handleDescriptionSave() {
        this.props.updateRecordMeta({...this.props.meta, description: this.state.description})
            .then(r => this.setState({descriptionOpen: false}));
    }

    private handleNameTagSave = () => {
        this.props.updateRecordMeta({...this.props.meta, name: this.state.name, tag: this.state.tag})
            .then(r => this.setState({nameTagOpen: false}));
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

    private getHeight = () => {
        let count: number;
        if (this.props.expand) {
            count = this.getRecordsCount(this.props);
        } else if (this.state.expand) {
            count = this.props.versions.length + 1;
        } else {
            count = 1;
        }
        return count * 48;
    };

    private handleExpand = () => {
        this.setState({
            expand: !this.state.expand
        })
    };

    private handleStateChange = (field: string) => (event: React.ChangeEvent<HTMLInputElement>) => {
        this.setState({[field]: event.target.value} as ComponentState)
    };
}

interface IRecordState {
    selectedRecord?: IRecordProps;
    nameTagOpen: boolean;
    descriptionOpen: boolean;
    deletionOpen: boolean;
    description: string;
    expand: boolean;
    name: string;
    tag?: string;
}

export interface IRecordProps extends IRecord, WithStyles<typeof styles> {
    hierarchyTooltipEnabled: boolean;
    root: boolean;
    expand: boolean;
    lazyLoad: boolean;

    updateRecordMeta(meta: RecordMeta): Promise<Response>;

    deleteRecords(id: string): Promise<Response>;

    forceDeleteRecords(id: string): Promise<Response>;

    updateParentId(id: string): void;

}

export default withStyles(styles)(RecordNode)