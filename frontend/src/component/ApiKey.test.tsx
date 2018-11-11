import {mount, shallow} from "enzyme";
import * as React from "react";
import * as sinon from "sinon";
import {ApiKey, IApiKeyProps} from "./ApiKey";

describe("component", () => {
    let props: IApiKeyProps;

    beforeEach(() => {
        props = {index: 1, value: "value-value", label: "value-label", handleApiKeyChange: event => undefined};
    });

    describe("rendering", () => {
        it('renders without crashing', () => {
            shallow(<ApiKey {...props}/>);
        });

        it('renders label', () => {
            const wrapper = shallow(<ApiKey {...props}/>);
            expect(wrapper.html()).toContain(props.label);
        });

        it('renders value in a textfield', () => {
            const wrapper = shallow(<ApiKey {...props}/>);
            window.console.log(wrapper.debug())
            expect(wrapper.find('TextField').props().value).toEqual(props.value);
        });
    });

    describe("callback", () => {
        it('adds data-index field to input for callback use', () => {
            const wrapper = mount(<ApiKey {...props}/>);
            expect(wrapper.find('input').prop('data-index')).toEqual(1);
        });

        it('calls api value change handler on textfield value change', () => {
            const spyOnChange = sinon.spy(props, 'handleApiKeyChange');
            const wrapper = shallow(<ApiKey {...props}/>);
            const event = {target: {value: 'new api value', attributes: {"data-index": 0}}};
            wrapper.find('TextField').simulate('change', event);
            expect(spyOnChange.callCount).toEqual(1);
            expect(spyOnChange.args[0][0].target.value).toEqual(event.target.value);
            expect(spyOnChange.args[0][0].target.attributes['data-index']).toEqual(event.target.attributes['data-index']);
        });
    });
});