import { shallow } from 'enzyme';
import * as React from 'react';
import IConfiguration from 'src/model/IConfiguration';

import Configuration from './Configuration';

describe('component', () => {
    let props: IConfiguration

    beforeEach(() => {
        props = {
            apiKeys: [{ name: "Api 1", apiKey: "key 1", primary: true, enabled: true },
            { name: "Api 2", apiKey: "key 2", primary: false, enabled: false }]
        };
    })

    describe('rendering', () => {
        it('renders without crashing', () => {
            shallow(<Configuration {...props} />)
        })

        it('renders child Service', () => {
            const wrapper = shallow(<Configuration {...props} />)
            expect(wrapper.text()).toContain(props.apiKeys[0].name)
            expect(wrapper.find('TextField').at(0).props().value).toEqual(props.apiKeys[0].apiKey)

            expect(wrapper.text()).toContain(props.apiKeys[1].name)
            expect(wrapper.find('TextField').at(1).props().value).toEqual(props.apiKeys[1].apiKey)
        })
    })

    describe('updating', () => {
        it('updates child Service', () => {
            const wrapper = shallow(<Configuration {...props} />)
            wrapper.find('TextField').at(0).simulate('change', { target: { value: 'new api key' } })
            expect(wrapper.text()).toContain(props.apiKeys[0].name)
            expect(wrapper.find('TextField').at(0).props().value).toEqual('new api key')
        })
    })
})
