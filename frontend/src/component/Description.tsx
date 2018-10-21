import "./Description.css"

import TextField from "@material-ui/core/TextField/TextField"
import ReactMarkdown from "react-markdown"
import * as React from "react"

export const Description: React.StatelessComponent<IDescriptionProps> = props => {
    return (
        <div className="flex-wrapper">
            <div className="flex-left">
                <TextField multiline={true} onChange={props.onChange}/>
            </div>
            <div className="flex-right">
                <ReactMarkdown source={props.rawText}/>
            </div>
        </div>
    )
}

export interface IDescriptionProps {
    rawText: string
    onChange: (event: React.ChangeEvent<HTMLTextAreaElement>) => void
}

export default Description