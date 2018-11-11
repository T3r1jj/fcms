import {shallow} from 'enzyme';
import * as React from 'react';
import IConfiguration from 'src/model/IConfiguration';

import Configuration from './Configuration';

describe('component', () => {
    let props: IConfiguration;

    beforeEach(() => {
        props = {
            primaryBackupLimit: 1,
            secondaryBackupLimit: 0,
            services: [{name: "Api 1", apiKeys: [{label: "value 1", value: "value 1"}], primary: true, enabled: true},
                {name: "Api 2", apiKeys: [{label: "value 2", value: "value 2"}], primary: false, enabled: false}]
        };
    });

    describe('rendering', () => {
        it('renders without crashing', () => {
            shallow(<Configuration {...props} />);
        });

        it('renders child Service', () => {
            const wrapper = shallow(<Configuration {...props} />);
            expect(wrapper.text()).toContain(props.services[0].name);
            expect(wrapper.find('TextField').at(0).props().value).toEqual(props.services[0].apiKeys[0].value);

            expect(wrapper.text()).toContain(props.services[1].name);
            expect(wrapper.find('TextField').at(1).props().value).toEqual(props.services[1].apiKeys[0].value);
        });
    });

    describe('updating', () => {
        it('updates child Service', () => {
            const wrapper = shallow(<Configuration {...props} />);
            const event = {target: {value: 'new api value', dataset: {index: 0}}};
            wrapper.find('TextField').at(0).simulate('change', event);
            expect(wrapper.text()).toContain(props.services[0].name);
            expect(wrapper.find('TextField').at(0).props().value).toEqual('new api value');
        });
    });
});
