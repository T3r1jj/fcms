import Tab from "@material-ui/core/Tab/Tab";
import {mount, shallow} from 'enzyme';
import * as React from 'react';
import {BrowserRouter} from "react-router-dom";
import sinon from 'sinon';
import Code from "../../model/code/Code";
import {CodeCallbackType} from "../../model/code/CodeCallbackType";
import ICode from "../../model/code/ICode";
import CodeEditor from "../CodeEditor";
import CodeCallbackPage from "./CodeCallbackPage";

describe('component', () => {
    let getCodeCallback: (t: CodeCallbackType) => Promise<Code>;
    let checkCodeCallback: (t: CodeCallbackType) => Promise<Response>;
    let updateCodeCallback: (c: Code) => Promise<Response>;
    let code: ICode;

    beforeEach(() => {
        code = new Code();
        code.code = "something wrong";
        code.exceptionHandler = "System.out.println();";
        code.finallyHandler = "System.exit(0)";

        code.tryHeader = "try {";
        code.catchHeader = "} catch (Exception e) {";
        code.finallyHeader = "} finally {";
        code.finallyFooter = "}";
        code.methodFooter = "}";
        code.methodHeader = "foo() {";

        getCodeCallback = () => {
            return new Promise<Code>((resolve) => {
                resolve(new Code());
            })
        };
        checkCodeCallback = () => {
            return new Promise<Response>((resolve) => {
                resolve(new Response());
            })
        };
        updateCodeCallback = () => {
            return new Promise<Response>((resolve) => {
                resolve(new Response());
            })
        };
    });

    describe('rendering', () => {
        it('renders without crashing', () => {
            shallow(<BrowserRouter><CodeCallbackPage getCodeCallback={getCodeCallback}
                                                     checkCodeCallback={checkCodeCallback}
                                                     updateCodeCallback={updateCodeCallback}/></BrowserRouter>);
        });
        it('renders two tabs with correct names', () => {
            const wrapper = shallow(<BrowserRouter><CodeCallbackPage getCodeCallback={getCodeCallback}
                                                                     checkCodeCallback={checkCodeCallback}
                                                                     updateCodeCallback={updateCodeCallback}/></BrowserRouter>);
            expect(wrapper.html()).toContain("After Replication Code");
            expect(wrapper.html()).toContain("On Replication Code");
        });
        it('renders loading', () => {
            const wrapper = shallow(<BrowserRouter><CodeCallbackPage getCodeCallback={getCodeCallback}
                                                                     checkCodeCallback={checkCodeCallback}
                                                                     updateCodeCallback={updateCodeCallback}/></BrowserRouter>);
            expect(wrapper.html()).toContain("Loading...");
        });
        it('renders CodeEditor after component did mount', (done) => {
            const wrapper = mount(<BrowserRouter><CodeCallbackPage getCodeCallback={getCodeCallback}
                                                                   checkCodeCallback={checkCodeCallback}
                                                                   updateCodeCallback={updateCodeCallback}/></BrowserRouter>);
            setImmediate(() => {
                wrapper.update();
                expect(wrapper.find(CodeEditor).length).toEqual(1);
                done();
            })
        });
        it('renders CodeEditor with another code after tab switch', (done) => {
            const onReplicationText = "on replication text";
            const afterReplicationText = "after replication text";
            getCodeCallback = (t) => {
                return new Promise<Code>((resolve) => {
                    if (t === CodeCallbackType.OnReplicationCode) {
                        code.code = onReplicationText;
                        resolve(code);
                    } else {
                        code.code = afterReplicationText;
                        resolve(code);
                    }
                })
            };
            const wrapper = mount(<BrowserRouter><CodeCallbackPage getCodeCallback={getCodeCallback}
                                                                   checkCodeCallback={checkCodeCallback}
                                                                   updateCodeCallback={updateCodeCallback}/></BrowserRouter>);
            setImmediate(() => {
                wrapper.update();
                expect(wrapper.html()).toContain(onReplicationText);
                const secondTab = wrapper.find(Tab).last();
                secondTab.simulate('click');
                setImmediate(() => {
                    wrapper.update();
                    expect(wrapper.html()).toContain(afterReplicationText);
                    done();
                })
            })
        });

        it('renders error on uncompilable code', (done) => {
            const errorMsg = "sikrit msg";
            checkCodeCallback = () => {
                return new Promise<Response>((resolve, reject) => {
                    reject(new TypeError(errorMsg));
                })
            };
            const wrapper = mount(<BrowserRouter><CodeCallbackPage getCodeCallback={getCodeCallback}
                                                                   checkCodeCallback={checkCodeCallback}
                                                                   updateCodeCallback={updateCodeCallback}/></BrowserRouter>);
            setImmediate(() => {
                wrapper.update();
                expect(wrapper.html()).toContain(errorMsg);
                done();
            })
        });
    });

    describe('callback', () => {
        it('save click causes update and status check', (done) => {
            const onReplicationText = "on replication text";
            const afterReplicationText = "after replication text";
            getCodeCallback = (t) => {
                return new Promise<Code>((resolve) => {
                    if (t === CodeCallbackType.OnReplicationCode) {
                        code.code = onReplicationText;
                        resolve(code);
                    } else {
                        code.code = afterReplicationText;
                        resolve(code);
                    }
                })
            };

            const props = {
                checkCodeCallback,
                getCodeCallback,
                updateCodeCallback
            };
            const checkCodeCallbackSpy = sinon.spy(props, "checkCodeCallback");
            const updateCodeCallbackSpy = sinon.spy(props, "updateCodeCallback");
            const wrapper = mount(<BrowserRouter><CodeCallbackPage {...props}/></BrowserRouter>);
            setImmediate(() => {
                wrapper.update();
                const buttons = wrapper.find('button');
                const saveButton = buttons.at(buttons.length - 3);
                saveButton.simulate('click');
                expect(updateCodeCallbackSpy.callCount).toEqual(1);
                expect(updateCodeCallbackSpy.args[0][0].code).toEqual(onReplicationText);
                expect(checkCodeCallbackSpy.callCount).toEqual(1);
                expect(checkCodeCallbackSpy.args[0][0]).toEqual(CodeCallbackType.OnReplicationCode);
                done();
            });
        });

        it('delete click causes update and status check', (done) => {
            const onReplicationText = "on replication text";
            const afterReplicationText = "after replication text";
            getCodeCallback = (t) => {
                return new Promise<Code>((resolve) => {
                    if (t === CodeCallbackType.OnReplicationCode) {
                        code.code = onReplicationText;
                        resolve(code);
                    } else {
                        code.code = afterReplicationText;
                        resolve(code);
                    }
                })
            };

            const props = {
                checkCodeCallback,
                getCodeCallback,
                updateCodeCallback
            };
            const checkCodeCallbackSpy = sinon.spy(props, "checkCodeCallback");
            const updateCodeCallbackSpy = sinon.spy(props, "updateCodeCallback");
            const wrapper = mount(<BrowserRouter><CodeCallbackPage {...props}/></BrowserRouter>);
            setImmediate(() => {
                wrapper.update();
                const buttons = wrapper.find('button');
                const deleteButton = buttons.at(buttons.length - 2);
                deleteButton.simulate('click');
                expect(updateCodeCallbackSpy.callCount).toEqual(1);
                expect(updateCodeCallbackSpy.args[0][0].code).toEqual("");
                expect(checkCodeCallbackSpy.callCount).toEqual(1);
                expect(checkCodeCallbackSpy.args[0][0]).toEqual(CodeCallbackType.OnReplicationCode);
                done();
            });
        });
    });
});
