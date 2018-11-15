import {mount, shallow} from 'enzyme';
import * as React from 'react';
import sinon from 'sinon';
import Code from "../../model/code/Code";
import {CodeCallbackType} from "../../model/code/CodeCallbackType";
import ICode from "../../model/code/ICode";
import CodeEditor from "../CodeEditor";
import CodeCallbackPage, {ICodeCallbackPageProps} from "./CodeCallbackPage";

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
            shallow(<CodeCallbackPage getCodeCallback={getCodeCallback}
                                      checkCodeCallback={checkCodeCallback}
                                      updateCodeCallback={updateCodeCallback}/>);
        });
        it('renders two tabs with correct names', () => {
            const wrapper = shallow(<CodeCallbackPage getCodeCallback={getCodeCallback}
                                                      checkCodeCallback={checkCodeCallback}
                                                      updateCodeCallback={updateCodeCallback}/>);
            expect(wrapper.html()).toContain("After Replication Code");
            expect(wrapper.html()).toContain("On Replication Code");
        });
        it('renders loading', () => {
            const wrapper = shallow(<CodeCallbackPage getCodeCallback={getCodeCallback}
                                                      checkCodeCallback={checkCodeCallback}
                                                      updateCodeCallback={updateCodeCallback}/>);
            expect(wrapper.html()).toContain("Loading...");
        });
        it('renders CodeEditor after component did mount', (done) => {
            const wrapper = shallow(<CodeCallbackPage getCodeCallback={getCodeCallback}
                                                      checkCodeCallback={checkCodeCallback}
                                                      updateCodeCallback={updateCodeCallback}/>);
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
            const wrapper = mount(<CodeCallbackPage getCodeCallback={getCodeCallback}
                                                    checkCodeCallback={checkCodeCallback}
                                                    updateCodeCallback={updateCodeCallback}/>);
            setImmediate(() => {
                wrapper.update();
                expect(wrapper.html()).toContain(onReplicationText);
                const secondTab = wrapper.find('button').at(1);
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
                return new Promise<Response>((resolve) => {
                    const response = new Response(null, {
                        status: 422,
                    });
                    response.json = () => {
                        return new Promise((r) => {
                            r({message: errorMsg})
                        })
                    };
                    resolve(response);
                })
            };
            const wrapper = mount(<CodeCallbackPage getCodeCallback={getCodeCallback}
                                                    checkCodeCallback={checkCodeCallback}
                                                    updateCodeCallback={updateCodeCallback}/>);
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

            const props: ICodeCallbackPageProps = {
                checkCodeCallback,
                getCodeCallback,
                updateCodeCallback
            };
            const checkCodeCallbackSpy = sinon.spy(props, "checkCodeCallback");
            const updateCodeCallbackSpy = sinon.spy(props, "updateCodeCallback");
            const wrapper = mount(<CodeCallbackPage {...props}/>);
            setImmediate(() => {
                wrapper.update();
                const buttons = wrapper.find('button');
                const saveButton = buttons.at(buttons.length - 2);
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

            const props: ICodeCallbackPageProps = {
                checkCodeCallback,
                getCodeCallback,
                updateCodeCallback
            };
            const checkCodeCallbackSpy = sinon.spy(props, "checkCodeCallback");
            const updateCodeCallbackSpy = sinon.spy(props, "updateCodeCallback");
            const wrapper = mount(<CodeCallbackPage {...props}/>);
            setImmediate(() => {
                wrapper.update();
                const buttons = wrapper.find('button');
                const deleteButton = buttons.at(buttons.length - 1);
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
