import TextField from "@material-ui/core/TextField/TextField"
import * as React from "react"
import ReactMarkdown from "react-markdown"
import "./Description.css"

export const Description: React.StatelessComponent<IDescriptionProps> = props => {
    return (
        <div className="flex-wrapper description">
            <div className="flex-left">
                <TextField multiline={true} onChange={props.onChange}
                           value={props.rawText} fullWidth={true}/>
            </div>
            <div className="flex-right" style={{wordBreak: "break-word"}}>
                <ReactMarkdown source={props.rawText}/>
            </div>
        </div>
    )
};

export interface IDescriptionProps {
    rawText: string
    onChange: (event: React.ChangeEvent<HTMLTextAreaElement>) => void
}

export default Description