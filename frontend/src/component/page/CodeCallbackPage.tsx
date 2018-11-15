import AppBar from "@material-ui/core/AppBar/AppBar";
import Button from "@material-ui/core/Button/Button";
import Tab from "@material-ui/core/Tab/Tab";
import Tabs from "@material-ui/core/Tabs/Tabs";
import Typography from "@material-ui/core/Typography/Typography";
import * as React from "react";
import Code from "../../model/code/Code";
import {CodeCallbackType} from "../../model/code/CodeCallbackType";
import ICode from "../../model/code/ICode";
import CodeEditor from "../CodeEditor";


export default class CodeCallbackPage extends React.Component<ICodeCallbackPageProps, IHistoryPageState> {

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
                {this.state.codes.size === 0 && <TabContainer>Loading...</TabContainer>}
                {this.state.codes.size !== 0 &&
                <TabContainer>
                    {this.getCurrentError() !== undefined &&
                    <div style={{marginBottom: 8 * 2, color: "red"}}><b>{this.getCurrentError()}</b></div>
                    }
                    <CodeEditor {...this.getCurrentCode()} onCodeChange={this.onCodeChange}/>
                </TabContainer>
                }
                <Button variant="contained" color="primary" onClick={this.handleSave}
                        disabled={this.state.codes.size === 0}>Save</Button>
                <Button variant="contained" color="secondary" onClick={this.handleDelete}
                        disabled={this.state.codes.size === 0}>Delete</Button>
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

    private handleSave() {
        this.setCurrentError(undefined);
        this.props.updateCodeCallback(this.getCurrentCode())
            .then(this.checkCodeStatus)
            .catch(r => this.setCurrentError(r.toString()))
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
            .catch(r => this.setCurrentError(r.toString()))
    }

    private checkCodeStatus() {
        this.props.checkCodeCallback(this.state.value)
            .then(r => {
                if (!r.ok) {
                    return r.json()
                } else {
                    return new Promise<any>((resolve) => {
                        resolve({})
                    })
                }
            })
            .then(r => this.setCurrentError(r.message))
            .catch(r => this.setCurrentError(r.toString()))
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
        window.console.log(code);
        const codes = new Map<CodeCallbackType, Code>(this.state.codes.entries());
        codes.set(this.state.value, code);
        window.console.log(codes.get(this.state.value));
        this.setState({
            codes
        }, callback);
        window.console.log(this.getCurrentCode());
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

const TabContainer: React.StatelessComponent = (props: any) => {
    return (
        <Typography component="div" style={{paddingTop: 8 * 3, paddingLeft: 8 * 3, paddingRight: 8 * 3}}>
            {props.children}
        </Typography>
    );
};

export interface ICodeCallbackPageProps {
    getCodeCallback(type: CodeCallbackType): Promise<Code>;

    checkCodeCallback(type: CodeCallbackType): Promise<Response>

    updateCodeCallback(code: Code): Promise<Response>;
}

interface IHistoryPageState {
    value: CodeCallbackType;
    codes: Map<CodeCallbackType, Code>;
}