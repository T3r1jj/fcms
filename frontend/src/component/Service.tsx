import Checkbox from '@material-ui/core/Checkbox';
import TextField from '@material-ui/core/TextField';
import * as React from 'react';
import IService from 'src/model/IService';

export const Service: React.StatelessComponent<IServiceProps> = props => {
    const handleApiKeyChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        props.onApiKeyChange({ ...props, apiKey: event.target.value })
    }
    const handleApiKeyToggle = (event: React.ChangeEvent<HTMLInputElement>, checked: boolean) => {
        props.onApiKeyChange({ ...props, enabled: checked })
    }

    return (
        <div key={props.name}>
            {props.name}:
            <TextField
                id={props.name + "-key"}
                label="API key"
                value={props.apiKey}
                onChange={handleApiKeyChange}
                margin="normal"
            />
            <Checkbox
                checked={props.enabled}
                onChange={handleApiKeyToggle}
            />
        </div>
    );
}

export interface IServiceProps extends IService {
    onApiKeyChange: (apiKey: IService) => void;
}

