import {AppBar} from "@material-ui/core";
import Button from "@material-ui/core/Button";
import TextField from "@material-ui/core/TextField";
import Toolbar from "@material-ui/core/Toolbar";
import Typography from "@material-ui/core/Typography";
import * as React from "react";
import {ComponentState} from "react";
import {RouteComponentProps, withRouter} from 'react-router'
import Client from "../../model/Client";

export class LoginPage extends React.Component<ILoginPageProps, ILoginPageState> {
    constructor(props: ILoginPageProps) {
        super(props);
        this.state = {
            password: "",
            username: "",
        }
    }

    public render(): React.ReactNode {
        return (
            <div>
                <AppBar position="static" color="primary">
                    <Toolbar>
                        <Typography variant="h6" color="inherit">
                            FCMS
                        </Typography>
                    </Toolbar>
                </AppBar>
                <div className={"centerScreen"}>
                    <div>
                        <Typography variant="h6" color="inherit" style={{margin: "0.5em"}}>
                            Log in to FCMS
                        </Typography>
                        <TextField
                            label="Username"
                            onChange={this.onStateChange("username")}
                            variant={"outlined"}
                            margin={"normal"}
                        />
                        <br/>
                        <TextField
                            // type="password"
                            label="Password"
                            onChange={this.onStateChange("password")}
                            variant={"outlined"}
                            margin={"normal"}
                        />
                    </div>
                    {this.state.error && <div style={{color: "red"}}>{this.state.error}</div>}
                    <Button style={{margin: "1em"}}
                            color={"primary"} variant={"contained"} onClick={this.handleAuthentication}>Login</Button>
                </div>
            </div>
        );
    }

    private onStateChange = (field: string) => (event: React.ChangeEvent<HTMLInputElement>) => {
        this.setState({[field]: event.target.value} as ComponentState)
    };

    private handleAuthentication = () => {
        this.setState({error: undefined});
        const client = new Client(this.state.username, this.state.password);
        client.isValidUser()
            .then(r => {
                if (r.ok) {
                    this.props.onAuthenticated(client, () => {
                        if (this.props.location.state && this.props.location.state.referrer) {
                            this.props.history.push(this.props.location.state.referrer);
                        } else {
                            this.props.history.push("/");
                        }
                    });
                } else {
                    this.setState({error: "Could not authenticate"})
                }
            })
            .catch(e => this.setState({error: e.toString()}))

    };
}

interface ILoginPageProps extends RouteComponentProps {
    onAuthenticated(client: Client, redirectCallback: () => void): void;
}

interface ILoginPageState {
    username: string,
    password: string,
    error?: string,
}

export default withRouter(LoginPage)