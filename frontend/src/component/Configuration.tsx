import {StyleRulesCallback, WithStyles} from "@material-ui/core";
import Button from "@material-ui/core/Button/Button";
import Dialog from "@material-ui/core/Dialog/Dialog";
import DialogActions from "@material-ui/core/DialogActions/DialogActions";
import DialogContent from "@material-ui/core/DialogContent/DialogContent";
import DialogTitle from "@material-ui/core/DialogTitle/DialogTitle";
import InputLabel from "@material-ui/core/InputLabel/InputLabel";
import LinearProgress from "@material-ui/core/LinearProgress/LinearProgress";
import withStyles from "@material-ui/core/styles/withStyles";
import TextField from "@material-ui/core/TextField/TextField";
import SaveIcon from '@material-ui/icons/Save';
import * as React from "react";
import {ComponentState} from "react";
import IConfiguration from '../model/IConfiguration';
import IService from '../model/IService';
import Service from './Service';

const styles: StyleRulesCallback = theme => ({
    bottomSpace: {
        "margin-bottom": theme.spacing.unit
    },
    cssFocused: {},
    cssLabelPrimary: {
        '&$cssFocused': {
            color: theme.palette.primary.dark,
        },
    },
    cssLabelSecondary: {
        '&$cssFocused': {
            color: theme.palette.secondary.dark,
        },
    },
    cssOutlinedInputPrimary: {
        '&$cssFocused $notchedOutline': {
            borderColor: theme.palette.primary.dark,
        },
    },
    cssOutlinedInputSecondary: {
        '&$cssFocused $notchedOutline': {
            borderColor: theme.palette.secondary.dark,
        },
    },
    notchedOutline: {},
    rightSpace: {
      "margin-right": theme.spacing.unit
    }
});

export class Configuration extends React.Component<IConfigurationProps, IConfigurationState> {

    constructor(props: IConfigurationProps) {
        super(props);
        this.state = {
            configOpen: false,
            error: false,
            errorText: undefined,
            loading: true,
            primaryBackupLimit: 0,
            secondaryBackupLimit: 0,
            services: []
        };
        this.fetchData = this.fetchData.bind(this);
        this.onApiKeyChange = this.onApiKeyChange.bind(this);
        this.sendSaveRequest = this.sendSaveRequest.bind(this);
        this.handleConfigClick = this.handleConfigClick.bind(this);
        this.handleConfigClose = this.handleConfigClose.bind(this);
    }

    public componentDidMount() {
        this.fetchData();
    }

    public render() {
        const classes = this.props.classes;
        return (
            <div className={classes.bottomSpace}>
                <Button variant="contained" onClick={this.handleConfigClick}>Configuration</Button>
                <Dialog onClose={this.handleConfigClose} aria-labelledby="simple-dialog-title"
                        open={this.state.configOpen}>
                    <DialogTitle id="simple-dialog-title">Configuration</DialogTitle>
                    <DialogContent>
                        {this.state.loading && <LinearProgress/>}
                        {this.state.services.map(s => <Service key={s.name}
                                                               onServiceChange={this.onApiKeyChange} {...s}/>)}
                        <br/>
                        <div style={{display: "flex", justifyContent: "space-around"}}>
                            <InputLabel/>
                            <TextField
                                className={classes.margin}
                                InputLabelProps={{
                                    FormLabelClasses: {
                                        focused: classes.cssFocused,
                                        root: classes.cssLabelPrimary,
                                    }
                                }}
                                InputProps={{
                                    classes: {
                                        focused: classes.cssFocused,
                                        notchedOutline: classes.notchedOutline,
                                        root: classes.cssOutlinedInputPrimary,
                                    },
                                }}
                                disabled={this.state.loading}
                                label="Primary backups limit"
                                value={this.state.primaryBackupLimit}
                                type="number"
                                onChange={this.handleLimitChange('primaryBackupLimit')}
                                variant="outlined"
                                margin="normal"
                            />
                            <TextField
                                className={classes.margin}
                                InputLabelProps={{
                                    FormLabelClasses: {
                                        focused: classes.cssFocused,
                                        root: classes.cssLabelSecondary,
                                    }
                                }}
                                InputProps={{
                                    classes: {
                                        focused: classes.cssFocused,
                                        notchedOutline: classes.notchedOutline,
                                        root: classes.cssOutlinedInputSecondary,
                                    },
                                }}
                                disabled={this.state.loading}
                                label="Secondary backups limit"
                                value={this.state.secondaryBackupLimit}
                                type="number"
                                variant="outlined"
                                onChange={this.handleLimitChange('secondaryBackupLimit')}
                                margin="normal"
                            />
                        </div>
                        {this.state.error && <br/> && this.state.errorText}
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={this.sendSaveRequest} variant={"contained"} color={"primary"}
                                disabled={this.state.loading || this.state.services.length === 0}>
                            <SaveIcon className={classes.rightSpace}/>
                            Save
                        </Button>
                        <Button onClick={this.handleConfigClose} variant={"contained"}>
                            Cancel
                        </Button>
                    </DialogActions>
                </Dialog>
            </div>
        );
    }

    private handleLimitChange = (field: string) => (event: any) => {
        this.setState({[field]: Math.max(event.target.value, 0)} as ComponentState);
    };

    private onApiKeyChange(service: IService) {
        const currentApiKeys = this.state.services;
        const changedKeyIndex = currentApiKeys.findIndex(ak => ak.name === service.name);
        const newApiKeys = currentApiKeys.slice();
        newApiKeys.splice(changedKeyIndex, 1, service);
        this.setState({services: newApiKeys});
    }

    private fetchData() {
        this.setState({loading: true, error: false, errorText: undefined});
        this.props.getConfiguration()
            .then(config => this.setState({...config, loading: false, error: false}))
            .catch(error => this.setState({loading: false, error: true, errorText: error.toString()}));
    }

    private sendSaveRequest() {
        this.setState({loading: true, error: false, errorText: undefined});
        this.props.updateConfiguration(this.state)
            .then(() => this.setState({loading: false, error: false}))
            .catch(error => this.setState({loading: false, error: true, errorText: error.toString()}));
    }

    private handleConfigClick() {
        this.setState({configOpen: true});
        if (!this.state.loading && this.state.error) {
            this.fetchData();
        }
    }

    private handleConfigClose() {
        this.setState({configOpen: false})
    }
}

export interface IConfigurationProps extends WithStyles<typeof styles> {
    getConfiguration: () => Promise<IConfiguration>
    updateConfiguration: (configuration: IConfiguration) => Promise<Response>
}

interface IConfigurationState extends IConfiguration {
    configOpen: boolean;
    loading: boolean;
    error: boolean;
    errorText: string | undefined;
}

export default withStyles(styles)(Configuration);