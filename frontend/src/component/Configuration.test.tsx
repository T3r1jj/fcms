import Button from "@material-ui/core/Button/Button";
import LinearProgress from "@material-ui/core/LinearProgress/LinearProgress";
import TextField from "@material-ui/core/TextField/TextField";
import {mount, shallow} from 'enzyme';
import * as React from 'react';
import IConfiguration from '../model/IConfiguration';

import Configuration from './Configuration';

describe('component', () => {
    let getConfiguration: () => Promise<IConfiguration>;
    let updateConfiguration: (configuration: IConfiguration) => Promise<Response>;
    let props: IConfiguration;

    beforeEach(() => {
        props = {
            primaryBackupLimit: 1,
            secondaryBackupLimit: 0,
            services: [{name: "Api 1", apiKeys: [{label: "value 1", value: "value 1"}], primary: true, enabled: true},
                {name: "Api 2", apiKeys: [{label: "value 2", value: "value 2"}], primary: false, enabled: false}]
        };
        getConfiguration = () => {
            return new Promise<IConfiguration>((resolve, reject) => {
                resolve(props)
            });
        };
        updateConfiguration = (configuration: IConfiguration) => {
            return new Promise<Response>((resolve, reject) => {
                JSON.stringify(configuration);
                resolve(new Response())
            });
        };
    });

    describe('rendering', () => {
        it('renders without crashing', () => {
            shallow(<Configuration getConfiguration={getConfiguration} updateConfiguration={updateConfiguration}/>);
        });

        it('renders at least limit fields', () => {
            const wrapper = shallow(<Configuration getConfiguration={getConfiguration}
                                                   updateConfiguration={updateConfiguration}/>);
            expect(wrapper.find(TextField).length).toEqual(2);
            expect(wrapper.find('TextField').at(0).props().label!.toLowerCase()).toContain("primary");
            expect(wrapper.find('TextField').at(0).props().label!.toLowerCase()).toContain("limit");
            expect(wrapper.find('TextField').at(1).props().label!.toLowerCase()).toContain("secondary");
            expect(wrapper.find('TextField').at(1).props().label!.toLowerCase()).toContain("limit");
        });

        it('renders loading', () => {
            const wrapper = shallow(<Configuration getConfiguration={getConfiguration}
                                                   updateConfiguration={updateConfiguration}/>);
            expect(wrapper.find(LinearProgress).exists()).toBeTruthy();
        });

        it('renders child Service', (done) => {
            const wrapper = mount(<Configuration getConfiguration={getConfiguration}
                                                 updateConfiguration={updateConfiguration}/>);
            setImmediate(() => {
                wrapper.update();
                expect(wrapper.text()).toContain(props.services[0].name);
                expect(wrapper.find('TextField').at(0).props().value).toEqual(props.services[0].apiKeys[0].value);

                expect(wrapper.text()).toContain(props.services[1].name);
                expect(wrapper.find('TextField').at(1).props().value).toEqual(props.services[1].apiKeys[0].value);
                done();
            });
        });

        it('does not render loading after load', (done) => {
            const wrapper = mount(<Configuration getConfiguration={getConfiguration}
                                                 updateConfiguration={updateConfiguration}/>);
            setImmediate(() => {
                wrapper.update();
                expect(wrapper.find(LinearProgress).exists()).toBeFalsy();
                done();
            });
        });
    });

    describe('updating', () => {
        it('updates child Service', (done) => {
            const wrapper = mount(<Configuration getConfiguration={getConfiguration}
                                                 updateConfiguration={updateConfiguration}/>);
            setImmediate(() => {
                wrapper.update();
                const event = {target: {value: 'new api value', dataset: {index: 0}}};
                wrapper.find('input').at(0).simulate('change', event);
                expect(wrapper.text()).toContain(props.services[0].name);
                expect(wrapper.find('TextField').at(0).props().value).toEqual('new api value');
                done();
            });
        });

        it('update request does not cause crash', (done) => {
            const wrapper = mount(<Configuration getConfiguration={getConfiguration}
                                                 updateConfiguration={updateConfiguration}/>);
            setImmediate(() => {
                wrapper.update();
                wrapper.find(Button).simulate('click');
                done();
            });
        });

        it('unsuccessful update request causes error message', (done) => {
            const someError = "some error";
            updateConfiguration = (configuration: IConfiguration) => {
                return new Promise<Response>((resolve, reject) => {
                    reject(new Error(someError));
                });
            };
            const wrapper = mount(<Configuration getConfiguration={getConfiguration}
                                                 updateConfiguration={updateConfiguration}/>);
            setImmediate(() => {
                wrapper.update();
                wrapper.find(Button).simulate('click');
                setImmediate(() => {
                    wrapper.update();
                    expect(wrapper.html()).toContain(someError);
                    done();
                });
            });
        });

        it('unsuccessful update request does not render loading', (done) => {
            const someError = "some error";
            updateConfiguration = (configuration: IConfiguration) => {
                return new Promise<Response>((resolve, reject) => {
                    reject(new Error(someError));
                });
            };
            const wrapper = mount(<Configuration getConfiguration={getConfiguration}
                                                 updateConfiguration={updateConfiguration}/>);
            setImmediate(() => {
                wrapper.update();
                wrapper.find(Button).simulate('click');
                setImmediate(() => {
                    wrapper.update();
                    expect(wrapper.find(LinearProgress).exists()).toBeFalsy();
                    done();
                });
            });
        });
    });
});
