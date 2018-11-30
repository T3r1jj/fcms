import {StyleRulesCallback, WithStyles} from "@material-ui/core";
import Checkbox from '@material-ui/core/Checkbox';
import withStyles from "@material-ui/core/styles/withStyles";

import * as React from 'react';
import IService from '../model/IService';
import {ApiKey} from "./ApiKey";

const styles: StyleRulesCallback = theme => ({
    primaryService: {
        color: theme.palette.primary.main
    },
    secondaryService: {
        color: theme.palette.secondary.main
    }
});

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
        <div key={props.name} style={{display: "flex", alignItems: "center"}}>
            <Checkbox
                checked={props.enabled}
                onChange={handleApiKeyToggle}
            />
            <span
                className={props.primary ? props.classes.primaryService : props.classes.secondaryService}>{props.name}</span>
            {props.apiKeys.map((ak, index) =>
                ApiKey({handleApiKeyChange, index, ...ak})
            )}
        </div>
    );
};

export interface IServiceProps extends IService, WithStyles<typeof styles> {
    onServiceChange: (apiKey: IService) => void;
}

export default withStyles(styles)(Service);