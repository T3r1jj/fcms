import IconButton from '@material-ui/core/IconButton';
import AttachmentIcon from '@material-ui/icons/Attachment';
import BackupIcon from '@material-ui/icons/Backup';
import VersionIcon from '@material-ui/icons/FiberNew';
import TextIcon from '@material-ui/icons/TextFormat';
import * as React from 'react';

import IRecord from '../model/IRecord';

export default class Record extends React.Component<IRecord, IRecordState> {
    constructor(props: IRecord) {
        super(props);
        this.state = { selectedRecord: undefined }
        this.onRecordSelected = this.onRecordSelected.bind(this)
    }

    public render() {
        return (
            <div>
                {this.props.name} :: Records: {this.getRecordsCount(this.props)}, Depth: {this.getVersionsDepth(this.props)}, Backups: {this.getBackupsCount(this.props)}
                {this.props.backups.map(b => <IconButton key={b.service} aria-label="Backup"><BackupIcon /></IconButton>)}
                <IconButton aria-label="Text"><TextIcon /></IconButton>
                {this.props.meta.map(m => <IconButton key={m.id} aria-label="Meta" onClick={this.onRecordSelected}><AttachmentIcon /></IconButton>)}
                {this.props.versions.map(m => <IconButton key={m.id} aria-label="Version" onClick={this.onRecordSelected}><VersionIcon /></IconButton>)}
                {this.state.selectedRecord ? <Record {...this.state.selectedRecord} /> : <div>Select child record for more info</div>}
            </div>
        );
    }

    private onRecordSelected(event: React.SyntheticEvent<HTMLElement>) {
        const recordId = (event.target as any)._reactInternalFiber.key
        let record = this.props.meta.find(e => e.id === recordId);
        if (record === undefined) {
            record = this.props.versions.find(v => v.id === recordId);
        }
        this.setState({ selectedRecord: record })
    }

    private getRecordsCount(props: IRecord): number {
        let recordsCount = 1
        props.versions.forEach(v => recordsCount += this.getRecordsCount(v))
        props.meta.forEach(m => recordsCount += this.getRecordsCount(m))
        return recordsCount
    }

    private getVersionsDepth(props: IRecord): number {
        return Math.max(0, Math.max.apply(props.versions.map(v => 1 + this.getVersionsDepth(props))))
    }

    private getBackupsCount(props: IRecord): number {
        let backupsCount = 0
        props.backups.forEach(m => backupsCount += 1)
        props.versions.forEach(v => backupsCount += this.getRecordsCount(v))
        props.meta.forEach(m => backupsCount += this.getRecordsCount(m))
        return backupsCount
    }
}

interface IRecordState {
    selectedRecord?: IRecord
}