import TextField from "@material-ui/core/TextField/TextField";
import {shallow} from 'enzyme';
import * as React from 'react';
import sinon from 'sinon';
import Code from "../model/code/Code";
import ICode from "../model/code/ICode";
import CodeEditor, {ICodeEditorProps} from "./CodeEditor";

describe('component', () => {
    const onCodeChange = (c: ICode) => undefined;
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
    });

    describe('rendering', () => {
        it('renders without crashing', () => {
            shallow(<CodeEditor onCodeChange={onCodeChange} {...code}/>);
        });

        it('renders whole method', () => {
            const wrapper = shallow(<CodeEditor onCodeChange={onCodeChange} {...code}/>);

            expect(wrapper.html()).toContain(code.code);
            expect(wrapper.html()).toContain(code.exceptionHandler);
            expect(wrapper.html()).toContain(code.finallyHandler);

            expect(wrapper.html()).toContain(code.tryHeader);
            expect(wrapper.html()).toContain(code.catchHeader);
            expect(wrapper.html()).toContain(code.finallyHeader);
            expect(wrapper.html()).toContain(code.finallyFooter);
            expect(wrapper.html()).toContain(code.methodFooter);
            expect(wrapper.html()).toContain(code.methodHeader);
        });

        it('renders 3 textfields', () => {
            const wrapper = shallow(<CodeEditor onCodeChange={onCodeChange} {...code}/>);
            expect(wrapper.find(TextField).length).toEqual(3);
        });
    });
    describe('callback', () => {
        it('calls back on the first textfield change', () => {
            const props: ICodeEditorProps = {onCodeChange, ...code}
            const codeChangeSpy = sinon.spy(props, "onCodeChange");
            const wrapper = shallow(<CodeEditor {...props}/>);
            const newValue = 'new value';
            const event = {target: {value: newValue}};
            wrapper.find('TextField').at(0).simulate('change', event);
            expect(codeChangeSpy.callCount).toEqual(1);
            expect(codeChangeSpy.args[0][0].code).toEqual(newValue);
        });

        it('calls back on the second textfield change', () => {
            const props: ICodeEditorProps = {onCodeChange, ...code}
            const codeChangeSpy = sinon.spy(props, "onCodeChange");
            const wrapper = shallow(<CodeEditor {...props}/>);
            const newValue = 'new value';
            const event = {target: {value: newValue}};
            wrapper.find('TextField').at(1).simulate('change', event);
            expect(codeChangeSpy.callCount).toEqual(1);
            expect(codeChangeSpy.args[0][0].exceptionHandler).toEqual(newValue);
        });

        it('calls back on the third textfield change', () => {
            const props: ICodeEditorProps = {onCodeChange, ...code}
            const codeChangeSpy = sinon.spy(props, "onCodeChange");
            const wrapper = shallow(<CodeEditor {...props}/>);
            const newValue = 'new value';
            const event = {target: {value: newValue}};
            wrapper.find('TextField').at(2).simulate('change', event);
            expect(codeChangeSpy.callCount).toEqual(1);
            expect(codeChangeSpy.args[0][0].finallyHandler).toEqual(newValue);
        });
    });
});
