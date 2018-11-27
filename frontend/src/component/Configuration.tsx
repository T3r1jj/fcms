import {StyleRulesCallback, WithStyles} from "@material-ui/core";
import Button from "@material-ui/core/Button/Button";
import Dialog from "@material-ui/core/Dialog/Dialog";
import DialogActions from "@material-ui/core/DialogActions/DialogActions";
import DialogContent from "@material-ui/core/DialogContent/DialogContent";
import InputLabel from "@material-ui/core/InputLabel/InputLabel";
import LinearProgress from "@material-ui/core/LinearProgress/LinearProgress";
import withStyles from "@material-ui/core/styles/withStyles";
import Tab from "@material-ui/core/Tab";
import Tabs from "@material-ui/core/Tabs";
import TextField from "@material-ui/core/TextField/TextField";
import RestartReplicationIcon from '@material-ui/icons/CallSplit';
import SaveIcon from '@material-ui/icons/Save';
import * as React from "react";
import {ComponentState} from "react";
import {Line} from "react-chartjs-2";
import BandwidthSize from "../model/health/BandwidthSize";
import Health from "../model/health/Health";
import IConfiguration from '../model/IConfiguration';
import IRecord from "../model/IRecord";
import IService from '../model/IService';
import Formatter from "../utils/Formatter";
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
            loadingQuota: false,
            primaryBackupLimit: 0,
            secondaryBackupLimit: 0,
            services: [],
            tab: 0
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

    // TODO: use cache
    public componentWillReceiveProps(nextProps: Readonly<IConfigurationProps>, nextContext: any): void {
        if (this.props.records !== nextProps.records) {
            this.props.onStatusChange("Status: " + this.props.records.map(r => r.backups.size).reduce((prev, curr) => prev + curr, 0) + " / " + (this.props.records.length * (this.state.primaryBackupLimit + this.state.secondaryBackupLimit)))
        }
    }

    public render() {
        const pp = {
            borderCapStyle: 'butt',
            borderDash: [],
            borderDashOffset: 0.0,
            borderJoinStyle: 'miter',
            fill: false,
            lineTension: 0.1,
            pointBackgroundColor: '#fff',
            pointBorderWidth: 1,
            pointHitRadius: 10,
            pointHoverBorderWidth: 2,
            pointHoverRadius: 5,
            pointRadius: 1,
        };

        const chartProps: any = {
            data: {
                datasets: [{
                    backgroundColor: "#5C6BC0",
                    borderColor: "#5C6BC0",
                    data: this.state.health ? this.state.health.bandwidth.map(b => {
                        return {
                            x: b.end,
                            y: b.download
                        }
                    }) : [],
                    label: 'Download',
                    ...pp
                }, {
                    backgroundColor: "#66BB6A",
                    borderColor: "#66BB6A",
                    data: this.state.health ? this.state.health.bandwidth.map(b => {
                        return {
                            x: b.end,
                            y: b.upload
                        }
                    }) : [],
                    label: 'Upload',
                    ...pp
                }, {
                    backgroundColor: "#78909C",
                    borderColor: "#78909C",
                    data: this.state.health ? this.state.health.bandwidth.map(b => {
                        return {
                            x: b.end,
                            y: b.upload + b.download
                        }
                    }) : [],
                    label: 'Total',
                    ...pp
                }]
            },
            options: {
                responsive: true,
                scales: {
                    xAxes: [{
                        display: true,
                        ticks: {
                            major: {
                                fontColor: '#FF0000',
                                fontStyle: 'bold'
                            }
                        },
                        type: 'time'
                    }],
                    yAxes: [{
                        display: true,
                        ticks: {
                            beginAtZero: true,
                            callback: (label: number, index: number, labels: any) => {
                                return Formatter.formatBytes(label);
                            }
                        }
                    }]
                },
                title: {
                    display: true,
                    text: 'Bandwidth'
                },
                tooltips: {
                    callbacks: {
                        label: (tooltipItem: any, data: any) => {
                            let label = data.datasets[tooltipItem.datasetIndex].label || '';
                            if (label) {
                                label += ': ';
                            }
                            window.console.log(tooltipItem.yLabel);
                            label += Formatter.formatBytes(tooltipItem.yLabel);
                            return label;
                        }
                    }
                }
            },
            type: 'line',
        };
        const classes = this.props.classes;
        const records = this.flatArray(this.props.records);
        return (
            <div className={classes.bottomSpace}>
                <Button variant="contained" onClick={this.handleConfigClick}>Configuration</Button>
                <Dialog onClose={this.handleConfigClose} aria-labelledby="simple-dialog-title"
                        open={this.state.configOpen}>
                    <Tabs
                        value={this.state.tab}
                        onChange={this.handleTabChange}
                        indicatorColor="primary"
                        textColor="primary"
                        fullWidth={true}
                    >
                        <Tab label={"Configuration"}/>
                        <Tab label={"Quota"}/>
                    </Tabs>
                    {this.state.tab === 0 && <div>
                        {this.state.loading && <LinearProgress color={"secondary"}/>}
                        <DialogContent>
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
                            <Button onClick={this.props.restartReplication} variant={"contained"} color={"secondary"}
                                    style={{marginRight: "auto"}}>
                                <RestartReplicationIcon className={classes.rightSpace}/>
                                Restart Replication
                            </Button>
                            <Button onClick={this.sendSaveRequest} variant={"contained"} color={"primary"}
                                    disabled={this.state.loading || this.state.services.length === 0}>
                                <SaveIcon className={classes.rightSpace}/>
                                Save
                            </Button>
                            <Button onClick={this.handleConfigClose} variant={"contained"}>
                                Cancel
                            </Button>
                        </DialogActions>
                    </div>
                    }
                    {this.state.tab === 1 &&
                    <div>
                        {this.state.loadingQuota && <LinearProgress color={"secondary"}/>}
                        {!this.state.loading && this.state.health &&
                        <DialogContent>
                            <div>Database size: {this.state.health.dbSize}</div>
                            <div>Database limit: {this.state.health.dbLimit}</div>
                            {this.state.health.storageQuotas.map(q => <div
                                key={q.name}>{q.name} {q.usedSpace} of {q.totalSpace} ({q.usedSpace / q.totalSpace}%)</div>)}
                            <div>Oldest
                                replication: {this.state.health.bandwidth.length > 0 ? this.state.health.bandwidth[0].end.toString() : "none"}</div>
                            <div>Latest
                                replication: {this.state.health.bandwidth.length > 0 ? this.state.health.bandwidth[this.state.health.bandwidth.length - 1].end.toString() : "none"}</div>
                            <div>Total replication count: {this.state.health.bandwidth.length}</div>
                            <div>Total replication
                                duration: {BandwidthSize.formatDuration(this.state.health.bandwidth.map(b => b.duration).reduce((prev, curr) => prev + curr, 0))}</div>
                            <div>Total replication
                                bandwidth: {Formatter.formatBytes(this.state.health.bandwidth.map(b => b.download + b.upload).reduce((prev, curr) => prev + curr, 0))}</div>
                            <div>Total replication
                                download: {Formatter.formatBytes(this.state.health.bandwidth.map(b => b.download).reduce((prev, curr) => prev + curr, 0))}</div>
                            <div>Total replication
                                upload: {Formatter.formatBytes(this.state.health.bandwidth.map(b => b.upload).reduce((prev, curr) => prev + curr, 0))}</div>
                            <div>Current replication
                                status: {records.map(r => r.backups.size).reduce((prev, curr) => prev + curr, 0)}
                                ({Formatter.formatBytes(records.map(r => Array.from(r.backups.values()).map(b => b.size).reduce((prev, curr) => prev + curr, 0)).reduce((prev, curr) => prev + curr, 0))})
                                / {records.length * (this.state.primaryBackupLimit + this.state.secondaryBackupLimit)}
                                ({Formatter.formatBytes(records.map(r => r.backups.values().next({size: 0}).value.size).reduce((prev, curr) => prev + curr, 0) * (this.state.primaryBackupLimit + this.state.secondaryBackupLimit))})
                            </div>
                            <div>Current replication status
                                (primary): {records.map(r => Array.from(r.backups.keys()).filter(s => this.state.services.findIndex(p => p.name === s && p.primary) >= 0).length).reduce((prev, curr) => prev + curr, 0)}
                                ({Formatter.formatBytes(records.map(r => Array.from(r.backups.entries()).map(([s, b]) => this.state.services.findIndex(p => p.name === s && p.primary) >= 0 ? b.size : 0).reduce((prev, curr) => prev + curr, 0)).reduce((prev, curr) => prev + curr, 0))})
                                / {records.length * this.state.primaryBackupLimit}
                                ({Formatter.formatBytes(records.map(r => r.backups.values().next({size: 0}).value.size).reduce((prev, curr) => prev + curr, 0) * this.state.primaryBackupLimit)})
                            </div>
                            <div>Current replication status
                                (secondary): {records.map(r => Array.from(r.backups.keys()).filter(s => this.state.services.findIndex(p => p.name === s && !p.primary) >= 0).length).reduce((prev, curr) => prev + curr, 0)}
                                ({Formatter.formatBytes(records.map(r => Array.from(r.backups.entries()).map(([s, b]) => this.state.services.findIndex(p => p.name === s && !p.primary) >= 0 ? b.size : 0).reduce((prev, curr) => prev + curr, 0)).reduce((prev, curr) => prev + curr, 0))})
                                / {records.length * this.state.secondaryBackupLimit}
                                ({Formatter.formatBytes(records.map(r => r.backups.values().next({size: 0}).value.size).reduce((prev, curr) => prev + curr, 0) * this.state.secondaryBackupLimit)})
                            </div>
                            <table>
                                <thead>
                                <tr>
                                    <th>x</th>
                                    <th>Min</th>
                                    <th>Avg</th>
                                    <th>Max</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr>
                                    <td>Bandwidth / Replication</td>
                                    <td>{Formatter.formatBytes(Math.max(0, Math.min.apply(null, this.state.health.bandwidth.map(b => b.download + b.upload))))}</td>
                                    <td>{Formatter.formatBytes(Math.max(0, this.state.health.bandwidth.map(b => b.download + b.upload).reduce((prev, curr) => prev + curr, 0) / this.state.health.bandwidth.length))}</td>
                                    <td>{Formatter.formatBytes(Math.max(0, Math.max.apply(null, this.state.health.bandwidth.map(b => b.download + b.upload))))}</td>
                                </tr>
                                <tr>
                                    <td>Download / Replication</td>
                                    <td>{Formatter.formatBytes(Math.max(0, Math.min.apply(null, this.state.health.bandwidth.map(b => b.download))))}</td>
                                    <td>{Formatter.formatBytes(this.state.health.bandwidth.map(b => b.download).reduce((prev, curr) => prev + curr, 0) / this.state.health.bandwidth.length)}</td>
                                    <td>{Formatter.formatBytes(Math.max(0, Math.max.apply(null, this.state.health.bandwidth.map(b => b.download))))}</td>
                                </tr>
                                <tr>
                                    <td>Upload / Replication</td>
                                    <td>{Formatter.formatBytes(Math.max(0, Math.min.apply(null, this.state.health.bandwidth.map(b => b.upload))))}</td>
                                    <td>{Formatter.formatBytes(this.state.health.bandwidth.map(b => b.upload).reduce((prev, curr) => prev + curr, 0) / this.state.health.bandwidth.length)}</td>
                                    <td>{Formatter.formatBytes(Math.max(0, Math.max.apply(null, this.state.health.bandwidth.map(b => b.upload))))}</td>
                                </tr>
                                <tr>
                                    <td>Bandwidth / s</td>
                                    <td>{Formatter.formatBytes(Math.max(0, Math.min.apply(null, this.state.health.bandwidth.map(b => (b.download + b.upload) / b.duration))))}</td>
                                    <td>{Formatter.formatBytes(this.state.health.bandwidth.map(b => (b.download + b.upload) / b.duration).reduce((prev, curr) => prev + curr, 0) / this.state.health.bandwidth.length)}</td>
                                    <td>{Formatter.formatBytes(Math.max(0, Math.max.apply(null, this.state.health.bandwidth.map(b => (b.download + b.upload) / b.duration))))}</td>
                                </tr>
                                <tr>
                                    <td>Download / s</td>
                                    <td>{Formatter.formatBytes(Math.max(0, Math.min.apply(null, this.state.health.bandwidth.map(b => b.download / b.duration))))}</td>
                                    <td>{Formatter.formatBytes(this.state.health.bandwidth.map(b => b.download / b.duration).reduce((prev, curr) => prev + curr, 0) / this.state.health.bandwidth.length)}</td>
                                    <td>{Formatter.formatBytes(Math.max(0, Math.max.apply(null, this.state.health.bandwidth.map(b => b.download / b.duration))))}</td>
                                </tr>
                                <tr>
                                    <td>Upload / s</td>
                                    <td>{Formatter.formatBytes(Math.max(0, Math.min.apply(null, this.state.health.bandwidth.map(b => b.upload / b.duration))))}</td>
                                    <td>{Formatter.formatBytes(this.state.health.bandwidth.map(b => b.upload / b.duration).reduce((prev, curr) => prev + curr, 0) / this.state.health.bandwidth.length)}</td>
                                    <td>{Formatter.formatBytes(Math.max(0, Math.max.apply(null, this.state.health.bandwidth.map(b => b.upload / b.duration))))}</td>
                                </tr>
                                <tr>
                                    <td>Replication Time</td>
                                    <td>{BandwidthSize.formatDuration(Math.max(0, Math.min.apply(null, this.state.health.bandwidth.map(b => b.duration))))}</td>
                                    <td>{BandwidthSize.formatDuration(this.state.health.bandwidth.map(b => b.duration).reduce((prev, curr) => prev + curr, 0) / this.state.health.bandwidth.length)}</td>
                                    <td>{BandwidthSize.formatDuration(Math.max(0, Math.max.apply(null, this.state.health.bandwidth.map(b => b.duration))))}</td>
                                </tr>
                                <tr>
                                    <td>Record Size</td>
                                    <td>{Formatter.formatBytes(Math.max(0, Math.max.apply(null, this.props.records.map(r => r.backups.values().next({size: 0}).value.size))))}</td>
                                    <td>{Formatter.formatBytes(this.props.records.map(r => r.backups.values().next({size: 0}).value.size).reduce((prev, curr) => prev + curr, 0))}</td>
                                    <td>{Formatter.formatBytes(Math.max(0, Math.min.apply(null, this.props.records.map(r => r.backups.values().next({size: 0}).value.size))))}</td>
                                </tr>
                                </tbody>
                            </table>
                            <Line data={chartProps.data} options={chartProps.options}/>
                        </DialogContent>
                        }
                    </div>
                    }
                </Dialog>
            </div>
        );
    }

    private flatArray(records: IRecord[]): IRecord[] {
        const array = [...records]
        for (const record of records) {
            array.push(...this.flatArray(record.versions))
        }
        return array;
    }

    private handleTabChange = (event: React.ChangeEvent, value: number) => {
        this.setState({tab: value});
        if (value === 1) {
            this.fetchHealth();
        }
    };

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
            .then(_ => this.props.onStatusChange("Status: " + this.props.records.map(r => r.backups.size).reduce((prev, curr) => prev + curr, 0) + " / " + (this.props.records.length * (this.state.primaryBackupLimit + this.state.secondaryBackupLimit))))
            .catch(error => this.setState({loading: false, error: true, errorText: error.toString()}));

    }

    private fetchHealth = () => {
        this.setState({loadingQuota: true});
        this.props.getHealth()
            .then(health => {
                const a: BandwidthSize[] = [];
                for (let i = 0; i < 10; i++) {
                    const b = new BandwidthSize()
                    b.download = Math.round(Math.random() * 21474836480)
                    b.upload = Math.round(Math.random() * 21474836480)
                    b.end = new Date(new Date().setDate(new Date().getDate() + i));
                    b.duration = Math.random() * 1000 * 60 * 60 * 24;
                    a.push(b)
                }
                health.bandwidth = a;
                return health;
            })
            .then(health => {
                this.setState({health, loadingQuota: false})
            });

    };

    private sendSaveRequest() {
        this.setState({loading: true, error: false, errorText: undefined});
        this.props.updateConfiguration(this.state)
            .then(() => this.setState({loading: false, error: false}))
            .then(_ => this.props.onStatusChange("Status: " + this.props.records.map(r => r.backups.size).reduce((prev, curr) => prev + curr, 0) + " / " + (this.props.records.length * (this.state.primaryBackupLimit + this.state.secondaryBackupLimit))))
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
    records: IRecord[];
    getConfiguration: () => Promise<IConfiguration>;
    updateConfiguration: (configuration: IConfiguration) => Promise<Response>;

    onStatusChange(status: string): void;

    getHealth(): Promise<Health>;

    restartReplication(): Promise<Response>;
}

interface IConfigurationState extends IConfiguration {
    configOpen: boolean;
    loading: boolean;
    loadingQuota: boolean;
    error: boolean;
    errorText: string | undefined;
    tab: number;
    health?: Health;
}

export default withStyles(styles)(Configuration);