import {mount, shallow} from 'enzyme';
import * as React from 'react';
import * as sinon from 'sinon';
import IService from "../model/IService";
import {IServiceProps, Service} from './Service';

describe('component', () => {
    let props: IServiceProps;

    beforeEach(() => {
        props = {
            apiKeys: [{label: "value name", value: "value name"}],
            classes: {
                primaryService: "primaryService",
                secondaryService: "secondaryService"
            },
            enabled: true,
            name: "api name",
            onServiceChange: (service: IService) => undefined,
            primary: true
        }
    });

    describe('rendering', () => {
        it('renders without crashing', () => {
            shallow(<Service {...props} />);
        });

        it('renders API name', () => {
            const wrapper = shallow(<Service {...props} />);
            expect(wrapper.text()).toContain(props.name);
        });

        it('renders API value', () => {
            const wrapper = shallow(<Service {...props} />);
            expect(wrapper.find('TextField').props().value).toEqual(props.apiKeys[0].value);
        });

        it('renders API enabled checkbox checked', () => {
            const wrapper = mount(<Service {...props} />);
            expect(wrapper.find('Checkbox').props().checked).toEqual(props.enabled);
        });

        it('renders API disable checkbox unchecked', () => {
            const trickyProps: IServiceProps = {...props, enabled: false};
            const wrapper = mount(<Service {...trickyProps} />);
            expect(wrapper.find('Checkbox').props().checked).not.toEqual('true');
        });
    });

    describe('callback', () => {
        it('TextField should call onServiceChange on api value change', () => {
            const spyOnChange = sinon.spy(props, 'onServiceChange');
            const wrapper = shallow(<Service {...props} />);
            const event = {target: {value: 'new api value', dataset: {index: 0}}};
            wrapper.find('TextField').simulate('change', event);
            expect(spyOnChange.callCount).toEqual(1);
            expect(spyOnChange.args[0][0].apiKeys[0].value).toEqual('new api value');
        });

        it('Checkbox should call onServiceChange on api toggle', () => {
            const spyOnChange = sinon.spy(props, 'onServiceChange');
            const wrapper = mount(<Service {...props} />);
            const input = wrapper.find('input[checked]') as any;
            input.getDOMNode().checked = !input.getDOMNode().checked;
            input.simulate('change');
            expect(spyOnChange.callCount).toEqual(1);
            expect(spyOnChange.args[0][0].enabled).toEqual(false);
        });
    });
});
