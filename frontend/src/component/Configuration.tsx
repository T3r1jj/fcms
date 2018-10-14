import * as React from 'react';
import IApiKey from 'src/model/IApiKey';
import IConfiguration from 'src/model/IConfiguration';
import { ApiKey } from './ApiKey';

export default class Configuration extends React.Component<IConfiguration, IConfiguration> {
    constructor(props: IConfiguration) {
        super(props);
        this.state = { ...props }
        this.onApiKeyChange = this.onApiKeyChange.bind(this);
    }

    public render() {
        return (
            <div>
                {this.state.apiKeys.map(ak => ApiKey({ onApiKeyChange: this.onApiKeyChange, ...ak }))}
            </div>
        );
    }

    private onApiKeyChange(apiKey: IApiKey) {
        const currentApiKeys = this.state.apiKeys;
        const changedKeyIndex = currentApiKeys.findIndex(ak => ak.name === apiKey.name);
        const newApiKeys = currentApiKeys.slice();
        newApiKeys.splice(changedKeyIndex, 1, apiKey);
        this.setState({ apiKeys: newApiKeys })
    }
}
