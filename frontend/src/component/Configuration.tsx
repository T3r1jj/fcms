import Button from "@material-ui/core/Button/Button";
import LinearProgress from "@material-ui/core/LinearProgress/LinearProgress";
import TextField from "@material-ui/core/TextField/TextField";
import * as React from "react";
import {ComponentState} from "react";
import IConfiguration from '../model/IConfiguration';
import IService from '../model/IService';
import {Service} from './Service';

export default class Configuration extends React.Component<IConfigurationProps, IConfigurationState> {

    constructor(props: IConfigurationProps) {
        super(props);
        this.state = {
            error: false,
            errorText: undefined,
            loading: true,
            primaryBackupLimit: 0,
            secondaryBackupLimit: 0,
            services: []
        };
        this.fetchData = this.fetchData.bind(this);
        this.onApiKeyChange = this.onApiKeyChange.bind(this);
        this.sendSaveRequest = this.sendSaveRequest.bind(this);
    }

    public componentDidMount() {
        this.fetchData();
    }

    public render() {
        return (
            <div>
                {this.state.loading && <LinearProgress/>}
                {this.state.services.map(s => Service({onServiceChange: this.onApiKeyChange, ...s}))}
                <br/>
                <TextField
                    label="Primary backups limit"
                    value={this.state.primaryBackupLimit}
                    onChange={this.handleLimitChange('primaryBackupLimit')}
                    margin="normal"
                />
                <TextField
                    label="Secondary backups limit"
                    value={this.state.secondaryBackupLimit}
                    type="number"
                    onChange={this.handleLimitChange('secondaryBackupLimit')}
                    margin="normal"
                />
                {this.state.error && <br/> && this.state.errorText}
                <br/>
                <Button onClick={this.sendSaveRequest}
                        disabled={this.state.loading || this.state.services.length === 0}>Save</Button>
            </div>
        );
    }

    private handleLimitChange = (field: string) => (event: any) => {
        this.setState({[field]: Math.min(event.target.value, 0)} as ComponentState);
    };

    private onApiKeyChange(service: IService) {
        const currentApiKeys = this.state.services;
        const changedKeyIndex = currentApiKeys.findIndex(ak => ak.name === service.name);
        const newApiKeys = currentApiKeys.slice();
        newApiKeys.splice(changedKeyIndex, 1, service);
        this.setState({services: newApiKeys});
    }

    private fetchData() {
        this.setState({loading: true, error: false, errorText: undefined});
        this.props.getConfiguration()
            .then(config => this.setState({...config, loading: false, error: false}))
            .catch(error => this.setState({loading: false, error: true, errorText: error.toString()}));
    }

    private sendSaveRequest() {
        this.setState({loading: true, error: false, errorText: undefined});
        this.props.updateConfiguration(this.state)
            .then(() => this.setState({loading: false, error: false}))
            .catch(error => this.setState({loading: false, error: true, errorText: error.toString()}));
    }
}

export interface IConfigurationProps {
    getConfiguration: () => Promise<IConfiguration>
    updateConfiguration: (configuration: IConfiguration) => Promise<Response>
}

interface IConfigurationState extends IConfiguration {
    loading: boolean;
    error: boolean;
    errorText: string | undefined;
}
