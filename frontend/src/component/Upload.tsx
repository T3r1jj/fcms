import {Button, Checkbox, FormControlLabel, TextField} from '@material-ui/core';
import * as React from 'react';
import Dropzone from 'react-dropzone';
import Formatter from '../utils/Formatter';

export interface IUploadProps {
    isUploadValid(file: File, name: string, parent: string, tag: string): boolean
}

interface IUploadState {
    files: File[]
    name: string
    parent: string
    tag: string
    throughServer: boolean
    valid: boolean
}

export default class Upload extends React.Component<IUploadProps, IUploadState> {
    constructor(props: any) {
        super(props);
        this.state = {files: [], name: "", parent: "", tag: "", valid: true, throughServer: false}
        this.onDrop = this.onDrop.bind(this)
        this.onNameChange = this.onNameChange.bind(this)
        this.onTagChange = this.onTagChange.bind(this)
        this.onParentChange = this.onParentChange.bind(this)
        this.onThroughServerChange = this.onThroughServerChange.bind(this)
        this.onUploadClick = this.onUploadClick.bind(this)
    }

    public onDrop(files: File[]) {
        window.console.log("dropped something")
        this.setState({
            files
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
                    <TextField label="Parent" value={this.state.parent} onChange={this.onParentChange}/>
                    <TextField label="Tag" value={this.state.tag} onChange={this.onTagChange}/>
                    <br/>
                    {!this.state.valid &&
                    <div>Invalid upload</div>
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
        this.setState({parent: event.target.value})
    }

    private onThroughServerChange(event: React.ChangeEvent<HTMLInputElement>, checked: boolean) {
        this.setState({throughServer: checked})
    }

    private onUploadClick() {
        this.setState({valid: this.props.isUploadValid(this.state.files[0], this.state.name, this.state.parent, this.state.tag)})
        if (this.state.valid) {
            window.console.log("start uploading - not implemented yet")
        }
    }
}