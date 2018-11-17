import {Button, Checkbox, FormControlLabel, TextField} from '@material-ui/core';
import LinearProgress from "@material-ui/core/LinearProgress/LinearProgress";
import * as React from 'react';
import Dropzone from 'react-dropzone';
import Formatter from '../utils/Formatter';

export interface IUploadProps {
    parentId?: string

    isUploadValid(file: File, name: string, parent: string, tag: string): boolean

    upload(file: File, name: string, parent: string, tag: string, onProgress?: (event: ProgressEvent) => void): Promise<Response>
}

interface IUploadState {
    files: File[]
    name: string
    parentId: string
    tag: string
    throughServer: boolean
    error?: string
    ok?: boolean
    serverProgress?: number
    clientProgress?: number
}

export default class Upload extends React.Component<IUploadProps, IUploadState> {
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
    }

    public componentWillReceiveProps(nextProps: Readonly<IUploadProps>, nextContext: any): void {
        if (nextProps.parentId) {
            this.setState({parentId: nextProps.parentId!});
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
                    <TextField label="Tag" value={this.state.tag} onChange={this.onTagChange}/>
                    <br/>
                    {this.state.error &&
                    <div style={{color: "red"}}>{this.state.error}</div>
                    }
                    {this.state.ok &&
                    <div style={{color: "green"}}>OK</div>
                    }
                    {this.state.ok === undefined && this.state.error === undefined && this.state.serverProgress &&
                    <LinearProgress variant="buffer" value={this.state.serverProgress}
                                    valueBuffer={this.state.clientProgress}/>
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
        this.setState({
            clientProgress,
            serverProgress,
        })
    }

    private onUploadClick() {
        const valid = this.props.isUploadValid(this.state.files[0], this.state.name, this.state.parentId, this.state.tag);
        this.setState({
            clientProgress: undefined,
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