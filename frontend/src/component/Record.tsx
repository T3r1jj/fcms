import IconButton from '@material-ui/core/IconButton';
import AttachmentIcon from '@material-ui/icons/Attachment';
import BackupIcon from '@material-ui/icons/Backup';
import VersionIcon from '@material-ui/icons/FiberNew';
import TextIcon from '@material-ui/icons/TextFormat';
import * as React from 'react';

import { Tooltip } from '@material-ui/core';
import IRecord from '../model/IRecord';

export default class Record extends React.Component<IRecordProps, IRecordState> {
    constructor(props: IRecordProps) {
        super(props);
        this.state = { selectedRecord: undefined }
        this.onRecordSelected = this.onRecordSelected.bind(this)
    }

    public render() {
        return (
            <div>
                {this.props.name} :: Records: {this.getRecordsCount(this.props)}, Depth: {this.getVersionsDepth(this.props)}, Backups: {this.getBackupsCount(this.props)}
                {this.props.backups.map(b =>
                    <Tooltip key={b.service} title={"Backup " + b.service}>
                        <IconButton aria-label="Backup"><BackupIcon /></IconButton>
                    </Tooltip>
                )}
                <Tooltip title="Description">
                    <IconButton aria-label="Description"><TextIcon /></IconButton>
                </Tooltip>
                {this.props.meta.map(m =>
                    <Tooltip key={m.id} title={"Meta " + m.name}>
                        <IconButton id={m.id} aria-label="Meta" onClick={this.onRecordSelected}><AttachmentIcon /></IconButton>
                    </Tooltip>
                )}
                {this.props.versions.map(v =>
                    <Tooltip key={v.id} title={"Version " + v.tag}>
                        <IconButton id={v.id} aria-label="Version" onClick={this.onRecordSelected}><VersionIcon /></IconButton>
                    </Tooltip>
                )}
                {this.state.selectedRecord ? <Record {...this.state.selectedRecord} /> :
                    (this.props.hierarchyTooltipEnabled ? <div>Select child record for more info</div> : null)
                }
            </div>
        );
    }

    private onRecordSelected(event: React.SyntheticEvent<HTMLElement>) {
        let eventTarget = event.target as any
        let recordId = eventTarget.getAttribute('id')
        while (recordId == null) {
            eventTarget = eventTarget.parentElement
            recordId = eventTarget.getAttribute('id')
        }
        let record = this.props.meta.find(e => e.id === recordId)
        if (record === undefined) {
            record = this.props.versions.find(v => v.id === recordId)
        }
        const newlySelectedRecord: IRecordProps = { ...record!, hierarchyTooltipEnabled: false }
        this.setState({
            selectedRecord: this.state.selectedRecord ?
                (this.state.selectedRecord.id === newlySelectedRecord.id ? undefined : newlySelectedRecord)
                : newlySelectedRecord
        })
    }

    private getRecordsCount(props: IRecord): number {
        let recordsCount = 1
        props.versions.forEach(v => recordsCount += this.getRecordsCount(v))
        props.meta.forEach(m => recordsCount += this.getRecordsCount(m))
        return recordsCount
    }

    private getVersionsDepth(props: IRecord): number {
        return Math.max(0, Math.max.apply(Math, props.versions.map(v => 1 + this.getVersionsDepth(v))))
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
    selectedRecord?: IRecordProps
}

export interface IRecordProps extends IRecord {
    hierarchyTooltipEnabled: boolean
}