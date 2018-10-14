import * as React from 'react';
import IConfiguration from 'src/model/IConfiguration';

export default class Configuration extends React.Component<IConfiguration, IConfiguration> {
    constructor(props: IConfiguration) {
        super(props);
    }

    public render() {
        return (
            <div>
                {this.props.apiKeys.length}
            </div>
        );
    }
}
