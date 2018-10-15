import * as React from 'react';
import IConfiguration from 'src/model/IConfiguration';
import IService from 'src/model/IService';
import { Service } from './Service';

export default class Configuration extends React.Component<IConfiguration, IConfiguration> {
    constructor(props: IConfiguration) {
        super(props)
        this.state = { ...props }
        this.onApiKeyChange = this.onApiKeyChange.bind(this)
    }

    public render() {
        return (
            <div>
                {this.state.apiKeys.map(ak => Service({ onApiKeyChange: this.onApiKeyChange, ...ak }))}
            </div>
        );
    }

    private onApiKeyChange(service: IService) {
        const currentApiKeys = this.state.apiKeys
        const changedKeyIndex = currentApiKeys.findIndex(ak => ak.name === service.name)
        const newApiKeys = currentApiKeys.slice()
        newApiKeys.splice(changedKeyIndex, 1, service)
        this.setState({ apiKeys: newApiKeys })
    }
}
