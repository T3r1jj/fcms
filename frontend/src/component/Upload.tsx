import {Button, Checkbox, FormControlLabel, TextField} from '@material-ui/core';
import FormLabel from "@material-ui/core/es/FormLabel";
import LinearProgress from "@material-ui/core/LinearProgress/LinearProgress";
import * as React from "react";
import Dropzone from 'react-dropzone';
import Progress from "../model/event/Progress";
import Formatter from '../utils/Formatter';

export interface IUploadProps {
    parentId?: string

    progresses: Progress[]

    isUploadValid(file: File, name: string, parent: string, tag: string): boolean

    upload(file: File, name: string, parent: string, tag: string, onProgress?: (event: ProgressEvent) => void): Promise<Response>
}

interface IUploadState {
    files: File[]
    name: string
    currentUploadName?: string
    parentId: string
    tag: string
    throughServer: boolean
    error?: string
    ok?: boolean
    progressInfo?: string
    serverProgress?: number
    clientProgress?: number
}

export default class Upload extends React.Component<IUploadProps, IUploadState> {
    private readonly uploadRef: React.RefObject<HTMLInputElement>;

    constructor(props: IUploadProps) {
        super(props);
        this.state = {files: [], name: "", parentId: "", tag: "", throughServer: false};
        this.onDrop = this.onDrop.bind(this);
        this.onNameChange = this.onNameChange.bind(this);
        this.onTagChange = this.onTagChange.bind(this);
        this.onParentChange = this.onParentChange.bind(this);
        this.onThroughServerChange = this.onThroughServerChange.bind(this);
        this.onUploadClick = this.onUploadClick.bind(this);
        this.onProgress = this.onProgress.bind(this);
        this.uploadRef = React.createRef();
    }

    public componentWillReceiveProps(nextProps: Readonly<IUploadProps>, nextContext: any): void {
        if (this.props.parentId !== nextProps.parentId) {
            this.setState({parentId: nextProps.parentId!});
            this.uploadRef.current!.focus();
        }
        const currentUploadProgressIndex = nextProps.progresses.findIndex(p => p.recordName === this.state.currentUploadName);
        if (currentUploadProgressIndex >= 0) {
            const currentProgress = nextProps.progresses[currentUploadProgressIndex];
            const serverProgress = 50 + 50 * currentProgress.bytesWritten / currentProgress.bytesTotal;
            this.setState({progressInfo: currentProgress.toString(), serverProgress});
        }
    }

    public onDrop(files: File[]) {
        this.setState({
            files,
            name: files[0].name,
            ok: undefined,
        });
    }

    public render() {
        return (
            <section>
                <div className="dropzone">
                    {this.props.progresses
                        .filter(p => p.recordName !== this.state.currentUploadName)
                        .map(p =>
                            <div key={p.id}>
                                <FormLabel>{p.toString()}</FormLabel>
                                <LinearProgress variant="determinate" value={100 * p.bytesWritten / p.bytesTotal}/>
                            </div>)}
                    <Dropzone onDrop={this.onDrop} multiple={false}>
                        <p>Try dropping some files here, or click to select files to upload.</p>
                        {this.state.files.length > 0 &&
                        <h3>Dropped
                            file: <br/> {this.state.files[0].name}
                            <br/> {Formatter.formatBytes(this.state.files[0].size)}</h3>
                        }
                    </Dropzone>
                    <TextField label="Name" value={this.state.name} onChange={this.onNameChange}/>
                    <TextField label="Parent ID" value={this.state.parentId} onChange={this.onParentChange}/>
                    <TextField label="Tag" value={this.state.tag} onChange={this.onTagChange}
                               inputRef={this.uploadRef}/>
                    <br/>
                    {this.state.error &&
                    <div style={{color: "red"}}>{this.state.error}</div>
                    }
                    {this.state.ok &&
                    <div style={{color: "green"}}>OK</div>
                    }
                    {this.state.ok === undefined && this.state.error === undefined && this.state.serverProgress &&
                    <div>
                        <FormLabel>{this.state.progressInfo}</FormLabel>
                        <LinearProgress variant="buffer" value={this.state.serverProgress}
                                        valueBuffer={this.state.clientProgress}/>
                    </div>
                    }
                    <FormControlLabel
                        control={
                            <Checkbox checked={this.state.throughServer} onChange={this.onThroughServerChange}/>
                        }
                        label="through server"
                    />
                    <Button variant="contained" color="primary" onClick={this.onUploadClick}>Upload</Button>
                </div>
            </section>
        );
    }

    private onNameChange(event: React.ChangeEvent<HTMLInputElement>) {
        this.setState({name: event.target.value})
    }

    private onTagChange(event: React.ChangeEvent<HTMLInputElement>) {
        this.setState({tag: event.target.value})
    }

    private onParentChange(event: React.ChangeEvent<HTMLInputElement>) {
        this.setState({parentId: event.target.value})
    }

    private onThroughServerChange(event: React.ChangeEvent<HTMLInputElement>, checked: boolean) {
        this.setState({throughServer: checked})
    }

    private onProgress(event: ProgressEvent) {
        const clientProgress = 100 * event.loaded / event.total;
        const serverProgress = clientProgress / 2;
        const progressInfo = (event.loaded === event.total ? "SEARCHING configuration for enabled primary service for backup replication" :
            `UPLOADING ${this.state.currentUploadName} TO SERVER ${Formatter.formatBytes(event.loaded)} / ${Formatter.formatBytes(event.total)}`);
        this.setState({
            clientProgress,
            progressInfo,
            serverProgress
        })
    }

    private onUploadClick() {
        const valid = this.props.isUploadValid(this.state.files[0], this.state.name, this.state.parentId, this.state.tag);
        this.setState({
            clientProgress: undefined,
            currentUploadName: this.state.name,
            error: valid ? undefined : "Invalid upload: name must not be empty and if you provide parentId, do also provide tag",
            ok: undefined,
            serverProgress: undefined
        });
        if (valid) {
            this.props.upload(this.state.files[0], this.state.name, this.state.parentId, this.state.tag, this.onProgress)
                .then(r => {
                    if (!r.ok) {
                        return r.json()
                    } else {
                        return new Promise<any>((resolve) => {
                            this.setState({ok: true});
                            resolve({})
                        })
                    }
                })
                .then(r => this.setState({error: r.message}))
                .catch(r => this.setState({error: r}))
        }
    }
}