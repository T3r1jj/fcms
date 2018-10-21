import "./Description.css"
import Description, {IDescriptionProps} from "./Description";
import {shallow} from "enzyme";
import * as React from "react";


describe("component", () => {
    let props: IDescriptionProps
    beforeEach(() => {
        props = {rawText: "abc", onChange: () => undefined}
    })

    describe("rendering", () => {
        it("renders without crash", () => {
            shallow(<Description {...props}/>)
        })

        it("renders raw text in the textarea", () => {
            const wrapper = shallow(<Description {...props}/>)
            expect(wrapper.html()).toContain(props.rawText)
        })
    })
})