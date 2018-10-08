import * as React from 'react';
import { IRecord } from './model/IRecord';

export default class Record extends React.Component<IRecord, IRecord> {
    constructor(props: IRecord) {
        super(props);
    }

    public render() {
        return (
            <p>
                {this.props.id}
            </p>
        );
    }
}