import { mount, shallow } from 'enzyme';
import * as React from 'react';
import * as sinon from 'sinon';
import IService from 'src/model/IService';
import { IServiceProps, Service } from './Service';

describe('component', () => {
    let props: IServiceProps

    beforeEach(() => {
        props = { name: "api name", apiKey: "key name", enabled: true, primary: true, onApiKeyChange: (apiKey: IService) => undefined }
    })

    describe('rendering', () => {
        it('renders without crashing', () => {
            shallow(<Service {...props} />)
        })

        it('renders API name', () => {
            const wrapper = shallow(<Service {...props} />)
            expect(wrapper.text()).toContain(props.name)
        })

        it('renders API key', () => {
            const wrapper = shallow(<Service {...props} />)
            expect(wrapper.find('TextField').props().value).toEqual(props.apiKey)
        })

        it('renders API enabled checkbox checked', () => {
            const wrapper = mount(<Service {...props} />)
            expect(wrapper.find('Checkbox').props().checked).toEqual(props.enabled)
        })

        it('renders API disable checkbox unchecked', () => {
            const trickyProps: IServiceProps = { ...props, enabled: undefined }
            const wrapper = mount(<Service {...trickyProps} />)
            expect(wrapper.find('Checkbox').props().checked).not.toEqual('true')
        })
    })

    describe('callback', () => {
        it('TextField should call onApiKeyChange on api key change', () => {
            const spyOnChange = sinon.spy(props, 'onApiKeyChange');
            const wrapper = shallow(<Service {...props} />)
            wrapper.find('TextField').simulate('change', { target: { value: 'new api key' } })
            expect(spyOnChange.callCount).toEqual(1)
            expect(spyOnChange.args[0][0].apiKey).toEqual('new api key')
        })

        it('Checkbox should call onApiKeyChange on api toggle', () => {
            const spyOnChange = sinon.spy(props, 'onApiKeyChange');
            const wrapper = mount(<Service {...props} />)
            const input = wrapper.find('input[checked]') as any
            input.getDOMNode().checked = !input.getDOMNode().checked;
            input.simulate('change');
            expect(spyOnChange.callCount).toEqual(1)
            expect(spyOnChange.args[0][0].enabled).toEqual(false)
        })
    })
})
