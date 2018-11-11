import Checkbox from '@material-ui/core/Checkbox';

import * as React from 'react';
import IService from 'src/model/IService';
import {ApiKey} from "./ApiKey";

export const Service: React.StatelessComponent<IServiceProps> = props => {
    const handleApiKeyChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const index = (event.target as any).dataset.index as number;
        const newApiKeys = props.apiKeys.slice();
        newApiKeys[index].value = event.target.value;
        props.onServiceChange({...props, apiKeys: newApiKeys});
    };
    const handleApiKeyToggle = (event: React.ChangeEvent<HTMLInputElement>, checked: boolean) => {
        props.onServiceChange({...props, enabled: checked});
    };

    return (
        <div key={props.name}>
            {props.name}:
            {props.apiKeys.map((ak, index) =>
                ApiKey({handleApiKeyChange, index, ...ak})
            )}
            <Checkbox
                checked={props.enabled}
                onChange={handleApiKeyToggle}
            />
        </div>
    );
};

export interface IServiceProps extends IService {
    onServiceChange: (apiKey: IService) => void;
}

