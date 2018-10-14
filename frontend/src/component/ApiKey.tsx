import Checkbox from '@material-ui/core/Checkbox';
import TextField from '@material-ui/core/TextField';
import * as React from 'react';
import IApiKey from 'src/model/IApiKey';

export const ApiKey: React.StatelessComponent<IApiKeyProps> = props => {
    const handleApiKeyChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        props.onApiKeyChange({ ...props, key: event.target.value });
    }
    const handleApiKeyToggle = (event: React.ChangeEvent<HTMLInputElement>, checked: boolean) => {
        props.onApiKeyChange({ ...props, enabled: checked });
    }

    return (
        <div key={props.name}>
            {props.name}:
            <TextField
                id={props.name + "-key"}
                label="API key"
                value={props.key}
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

export interface IApiKeyProps extends IApiKey {
    onApiKeyChange: (apiKey: IApiKey) => void;
}

