import {StyleRulesCallback, WithStyles} from "@material-ui/core";
import AppBar from "@material-ui/core/AppBar/AppBar";
import Button from "@material-ui/core/Button/Button";
import withStyles from "@material-ui/core/styles/withStyles";
import Tab from "@material-ui/core/Tab/Tab";
import Tabs from "@material-ui/core/Tabs/Tabs";
import Typography from "@material-ui/core/Typography/Typography";
import DeleteIcon from '@material-ui/icons/Delete';
import RefreshIcon from '@material-ui/icons/Refresh';
import SaveIcon from '@material-ui/icons/Save';
import * as React from "react";
import Code from "../../model/code/Code";
import {CodeCallbackType} from "../../model/code/CodeCallbackType";
import ICode from "../../model/code/ICode";
import CodeEditor from "../CodeEditor";

const styles: StyleRulesCallback = theme => ({
    button: {
        marginLeft: theme.spacing.unit,
        marginRight: theme.spacing.unit
    },
    error: {
        color: theme.palette.secondary.main,
        marginBottom: theme.spacing.unit * 2
    },
    iconLeft: {
        marginRight: theme.spacing.unit
    },
    tabContainer: {
        paddingLeft: theme.spacing.unit * 3,
        paddingRight: theme.spacing.unit * 3,
        paddingTop: theme.spacing.unit * 3
    }
});

export class CodeCallbackPage extends React.PureComponent<ICodeCallbackPageProps, IHistoryPageState> {

    private readonly values = this.getValues();

    constructor(props: any) {
        super(props);
        this.fetchCode = this.fetchCode.bind(this);
        this.checkCodeStatus = this.checkCodeStatus.bind(this);
        this.onCodeChange = this.onCodeChange.bind(this);
        this.handleSave = this.handleSave.bind(this);
        this.handleDelete = this.handleDelete.bind(this);
        this.getCurrentError = this.getCurrentError.bind(this);
        this.setCurrentError = this.setCurrentError.bind(this);
        this.getCurrentCode = this.getCurrentCode.bind(this);
        this.state = {value: 0, codes: new Map<CodeCallbackType, Code>()};
    }

    public componentDidMount() {
        window.document.title = "FCMS - Code";
        this.fetchCode(this.state.value);
    }

    public render() {
        return (
            <div>
                <AppBar position="static" color="default">
                    <Tabs
                        value={this.state.value}
                        onChange={this.handleChange}
                        indicatorColor="primary"
                        textColor="primary"
                        fullWidth={true}
                    >
                        {this.values.map(v => <Tab key={v} label={this.formatTitle(v)}/>)}
                    </Tabs>
                </AppBar>
                {this.state.codes.size === 0 && <TabContainer classes={this.props.classes}>Loading...</TabContainer>}
                {this.state.codes.size !== 0 &&
                <TabContainer classes={this.props.classes}>
                    {this.getCurrentError() !== undefined &&
                    <div className={this.props.classes.error}><b>{this.getCurrentError()}</b></div>
                    }
                    <CodeEditor {...this.getCurrentCode()} onCodeChange={this.onCodeChange}/>
                </TabContainer>
                }
                <Button variant="contained" color="primary" onClick={this.handleSave}
                        disabled={this.state.codes.size === 0} className={this.props.classes.button}>
                    <SaveIcon className={this.props.classes.iconLeft}/>
                    Save
                </Button>
                <Button variant="contained" color="secondary" onClick={this.handleDelete}
                        disabled={this.state.codes.size === 0} className={this.props.classes.button}>
                    <DeleteIcon className={this.props.classes.iconLeft}/>
                    Delete
                </Button>
                <Button variant="contained" onClick={this.handleUndo}
                        disabled={this.state.codes.size === 0} className={this.props.classes.button}>
                    <RefreshIcon className={this.props.classes.iconLeft}/>
                    Reload
                </Button>
            </div>
        );
    }

    private handleChange = (event: React.ChangeEvent, value: number) => {
        const codeCallbackType = CodeCallbackType[CodeCallbackType[value]];
        if (this.state.codes.has(codeCallbackType)) {
            this.setState({value: codeCallbackType});
        } else {
            this.fetchCode(codeCallbackType);
        }
    };

    private handleDelete() {
        this.onCodeChange({
            ...this.getCurrentCode(),
            code: "",
            exceptionHandler: "",
            finallyHandler: "",
        }, this.handleSave);
    };

    private handleUndo = () => {
        this.fetchCode(this.state.value);
    };

    private handleSave() {
        this.setCurrentError(undefined);
        this.props.updateCodeCallback(this.getCurrentCode())
            .then(this.checkCodeStatus)
            .catch(e => this.setCurrentError(e.toString()))
    };

    private getCurrentCode() {
        return this.state.codes.get(this.state.value)!;
    }

    private fetchCode(type: CodeCallbackType) {
        this.props.getCodeCallback(type)
            .then(code => {
                const codes = new Map<CodeCallbackType, Code>(this.state.codes.entries());
                codes.set(type, code);
                this.setState({
                    codes,
                    value: type
                })
            })
            .then(this.checkCodeStatus)
            .catch(e => this.setCurrentError(e.toString()))
    }

    private checkCodeStatus() {
        this.props.checkCodeCallback(this.state.value)
            .catch(e => this.setCurrentError(e.toString()))
    }

    private getValues() {
        const e = CodeCallbackType;
        return Object.keys(e)
            .map(k => e[k])
            .filter(v => typeof v === "number") as CodeCallbackType[];
    }

    private formatTitle(index: number) {
        return CodeCallbackType[index]!.match(/[A-Z][a-z]+/g)!.join(" ");
    }

    private onCodeChange(code: ICode, callback?: () => void) {
        const codes = new Map<CodeCallbackType, Code>(this.state.codes.entries());
        codes.set(this.state.value, code);
        this.setState({
            codes
        }, callback);
    }

    private getCurrentError() {
        return this.getCurrentCode().error
    }

    private setCurrentError(error?: string) {
        const codes = new Map<CodeCallbackType, Code>(this.state.codes.entries());
        codes.get(this.state.value)!.error = error;
        this.setState({
            codes
        })
    }
}

const TabContainer: React.StatelessComponent<{ children?: any, classes?: any }> = (props: { children?: any, classes?: any }) => {
    return (
        <Typography component="div" className={props.classes.tabContainer}>
            {props.children}
        </Typography>
    );
};

export interface ICodeCallbackPageProps extends WithStyles<typeof styles> {
    getCodeCallback(type: CodeCallbackType): Promise<Code>;

    checkCodeCallback(type: CodeCallbackType): Promise<Response>

    updateCodeCallback(code: Code): Promise<Response>;
}

interface IHistoryPageState {
    value: CodeCallbackType;
    codes: Map<CodeCallbackType, Code>;
}

export default withStyles(styles)(CodeCallbackPage)