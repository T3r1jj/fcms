import Card from "@material-ui/core/Card/Card";
import CardContent from "@material-ui/core/CardContent/CardContent";
import TextField from "@material-ui/core/TextField/TextField";
import * as React from "react";
import ICode from "../model/code/ICode";

export default class CodeEditor extends React.Component<ICodeEditorProps> {
    public render(): React.ReactNode {
        return (
            <pre className={"code-container"}>
                {this.props.methodHeader}<br/>
                {this.indentText(1, this.props.tryHeader)}<br/>
                <Card>
                    <CardContent>
                        <TextField multiline={true} onChange={this.handleLimitChange("code")}
                                   value={this.props.code} fullWidth={true}/>
                    </CardContent>
                </Card>
                <br/>
                {this.indentText(1, this.props.catchHeader)}<br/>
                <Card>
                    <CardContent>
                        <TextField multiline={true} onChange={this.handleLimitChange("exceptionHandler")}
                                   value={this.props.exceptionHandler} fullWidth={true}/>
                    </CardContent>
                </Card>
                <br/>
                {this.indentText(1, this.props.finallyHeader)}<br/>
                <Card>
                    <CardContent>
                        <TextField multiline={true} onChange={this.handleLimitChange("finallyHandler")}
                                   value={this.props.finallyHandler} fullWidth={true}/>
                    </CardContent>
                </Card>
                <br/>
                {this.indentText(1, this.props.finallyFooter)}<br/>
                {this.indentText(0, this.props.methodFooter)}
            </pre>
        )
    }

    private handleLimitChange = (field: string) => (event: React.ChangeEvent<HTMLTextAreaElement>) => {
        this.props.onCodeChange({
                ...this.props as ICode,
                [field]: event.target.value
            }
        )
    };

    private indentText(indents: number, text: string) {
        let indentation = "";
        for (let i = 0; i < indents; i++) {
            indentation += "\t";
        }
        return indentation + (text !== undefined ? text : "MISSING-TEXT-ERROR").trim() + "\n"
    }
}

export interface ICodeEditorProps extends ICode {
    onCodeChange(code: ICode): void;
}