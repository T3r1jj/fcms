import TextField from "@material-ui/core/TextField/TextField";
import * as React from "react";
import IApiKey from "../model/IApiKey";

export const ApiKey: React.StatelessComponent<IApiKeyProps> = props => {
    return (
        <TextField
            key={props.index}
            inputProps={{'data-index': props.index}}
            label={props.label}
            value={props.value}
            onChange={props.handleApiKeyChange}
            margin="normal"
        />
    );
};

export interface IApiKeyProps extends IApiKey {
    handleApiKeyChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
    index: number;
}