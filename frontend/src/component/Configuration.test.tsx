import Button from "@material-ui/core/Button/Button";
import Dialog from "@material-ui/core/Dialog/Dialog";
import LinearProgress from "@material-ui/core/LinearProgress/LinearProgress";
import TextField from "@material-ui/core/TextField/TextField";
import {mount, shallow} from 'enzyme';
import * as React from 'react';
import 'reflect-metadata';
import Health from "../model/health/Health";
import IConfiguration from '../model/IConfiguration';
import IRecord from "../model/IRecord";
import {Configuration} from './Configuration';

describe('component', () => {
    let getConfiguration: () => Promise<IConfiguration>;
    let getHealth: () => Promise<Health>;
    let updateConfiguration: (configuration: IConfiguration) => Promise<Response>;
    let restartReplication: () => Promise<Response>;
    let onStatusChange: (status: string) => void;
    let props: IConfiguration;
    let records: IRecord[];
    const classes = {};

    beforeEach(() => {
        onStatusChange = () => undefined;
        restartReplication = () => {
            return new Promise<Response>((resolve, reject) => {
                resolve(new Response())
            });
        };
        records = [];
        props = {
            primaryBackupLimit: 1,
            secondaryBackupLimit: 0,
            services: [{name: "Api 1", apiKeys: [{label: "value 1", value: "value 1"}], primary: true, enabled: true},
                {name: "Api 2", apiKeys: [{label: "value 2", value: "value 2"}], primary: false, enabled: false}],
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
        getHealth = () => {
            return new Promise<Health>((resolve, reject) => {
                resolve(new Health())
            });
        };
    });

    describe('rendering', () => {
        it('renders without crashing', () => {
            shallow(<Configuration classes={classes} getConfiguration={getConfiguration} onStatusChange={onStatusChange}
                                   restartReplication={restartReplication}
                                   updateConfiguration={updateConfiguration} getHealth={getHealth} records={records}/>);
        });

        it('renders at least limit fields', () => {
            const wrapper = shallow(<Configuration classes={classes} getConfiguration={getConfiguration}
                                                   records={records} restartReplication={restartReplication}
                                                   updateConfiguration={updateConfiguration} getHealth={getHealth}
                                                   onStatusChange={onStatusChange}/>);
            expect(wrapper.find(TextField).length).toEqual(2);
            expect(wrapper.find('TextField').at(0).props().label!.toLowerCase()).toContain("primary");
            expect(wrapper.find('TextField').at(0).props().label!.toLowerCase()).toContain("limit");
            expect(wrapper.find('TextField').at(1).props().label!.toLowerCase()).toContain("secondary");
            expect(wrapper.find('TextField').at(1).props().label!.toLowerCase()).toContain("limit");
        });

        it('renders loading', () => {
            const wrapper = shallow(<Configuration classes={classes} getConfiguration={getConfiguration}
                                                   records={records} onStatusChange={onStatusChange}
                                                   restartReplication={restartReplication}
                                                   updateConfiguration={updateConfiguration} getHealth={getHealth}/>);
            expect(wrapper.find(LinearProgress).exists()).toBeTruthy();
        });

        it('renders child Service', (done) => {
            const wrapper = mount(<Configuration classes={classes} getConfiguration={getConfiguration} records={records}
                                                 onStatusChange={onStatusChange} restartReplication={restartReplication}
                                                 updateConfiguration={updateConfiguration} getHealth={getHealth}/>);
            wrapper.find(Button).first().simulate('click');
            setImmediate(() => {
                wrapper.update();
                expect(wrapper.find(Dialog).text()).toContain(props.services[0].name);
                expect(wrapper.find(Dialog).find('TextField').at(0).props().value).toEqual(props.services[0].apiKeys[0].value);

                expect(wrapper.find(Dialog).text()).toContain(props.services[1].name);
                expect(wrapper.find(Dialog).find('TextField').at(1).props().value).toEqual(props.services[1].apiKeys[0].value);
                done();
            });
        });

        it('does not render loading after load', (done) => {
            const wrapper = mount(<Configuration classes={classes} getConfiguration={getConfiguration} records={records}
                                                 onStatusChange={onStatusChange} restartReplication={restartReplication}
                                                 updateConfiguration={updateConfiguration} getHealth={getHealth}/>);
            wrapper.find(Button).first().simulate('click');
            setImmediate(() => {
                wrapper.update();
                expect(wrapper.find(LinearProgress).exists()).toBeFalsy();
                done();
            });
        });
    });

    describe('updating', () => {
        it('updates child Service', (done) => {
            const wrapper = mount(<Configuration classes={classes} getConfiguration={getConfiguration} records={records}
                                                 onStatusChange={onStatusChange} restartReplication={restartReplication}
                                                 updateConfiguration={updateConfiguration} getHealth={getHealth}/>);
            wrapper.find(Button).first().simulate('click');
            setImmediate(() => {
                wrapper.update();
                const event = {target: {value: 'new api value', dataset: {index: 0}}};
                const input = wrapper.find(Dialog).find(TextField).first().find('input');
                input.simulate('change', event);
                expect(wrapper.find(Dialog).text()).toContain(props.services[0].name);
                expect(wrapper.find(Dialog).find('TextField').at(0).props().value).toEqual('new api value');
                done();
            });
        });

        it('update request does not cause crash', (done) => {
            const wrapper = mount(<Configuration classes={classes} getConfiguration={getConfiguration} records={records}
                                                 onStatusChange={onStatusChange} restartReplication={restartReplication}
                                                 updateConfiguration={updateConfiguration} getHealth={getHealth}/>);
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
            const wrapper = mount(<Configuration classes={classes} getConfiguration={getConfiguration} records={records}
                                                 onStatusChange={onStatusChange} restartReplication={restartReplication}
                                                 updateConfiguration={updateConfiguration} getHealth={getHealth}/>);
            wrapper.find(Button).first().simulate('click');
            setImmediate(() => {
                wrapper.update();
                wrapper.find(Button).at(wrapper.find(Button).length - 2).simulate('click');
                setImmediate(() => {
                    wrapper.update();
                    expect(wrapper.find(Dialog).html()).toContain(someError);
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
            const wrapper = mount(<Configuration classes={classes} getConfiguration={getConfiguration} records={records}
                                                 onStatusChange={onStatusChange} restartReplication={restartReplication}
                                                 updateConfiguration={updateConfiguration} getHealth={getHealth}/>);
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
