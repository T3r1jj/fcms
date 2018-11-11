import Button from "@material-ui/core/Button/Button";
import * as React from 'react';
import IConfiguration from 'src/model/IConfiguration';
import IService from 'src/model/IService';
import {Service} from './Service';

export default class Configuration extends React.Component<IConfiguration, IConfiguration> {
    constructor(props: IConfiguration) {
        super(props);
        this.state = {...props};
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
                {this.state.services.map(s => Service({onServiceChange: this.onApiKeyChange, ...s}))}
                <br/>
                <Button onClick={this.sendSaveRequest}>Save</Button>
            </div>
        );
    }

    private onApiKeyChange(service: IService) {
        const currentApiKeys = this.state.services;
        const changedKeyIndex = currentApiKeys.findIndex(ak => ak.name === service.name);
        const newApiKeys = currentApiKeys.slice();
        newApiKeys.splice(changedKeyIndex, 1, service);
        this.setState({services: newApiKeys});
    }

    private fetchData() {
        fetch('http://localhost:8080/api/configuration')
            .then(result => result.json())
            .then(config => this.setState({...config}))
            .catch(error => window.console.error('Error:', error));
    }

    private sendSaveRequest() {
        fetch('http://localhost:8080/api/configuration', {
            body: JSON.stringify(this.state),
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            method: 'POST'
        })
            .catch(error => window.console.error('Error:', error));
    }
}
