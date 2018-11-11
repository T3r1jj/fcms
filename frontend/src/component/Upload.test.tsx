import {mount, shallow} from "enzyme";
import * as React from "react";
import Dropzone from "react-dropzone";
import Upload, {IUploadProps} from "./Upload";

describe("component", () => {
    let validProps: IUploadProps;
    let invalidProps: IUploadProps;

    beforeEach(() => {
        validProps = {isUploadValid: (file, name1, parent1, tag) => true};
        invalidProps = {isUploadValid: (file, name1, parent1, tag) => false};
    });

    describe("rendering", () => {
        it("renders without crashing", () => {
            shallow(<Upload {...validProps}/>);
        });

        it("renders name", () => {
            const wrapper = shallow(<Upload {...validProps}/>);
            expect(wrapper.find("TextField").at(0).prop("label")).toEqual("Name");
        });
        it("renders parent", () => {
            const wrapper = shallow(<Upload {...validProps}/>);
            expect(wrapper.find("TextField").at(1).prop("label")).toEqual("Parent");
        });
        it("renders tag", () => {
            const wrapper = shallow(<Upload {...validProps}/>);
            expect(wrapper.find("TextField").at(2).prop("label")).toEqual("Tag");
        });
        it("renders checkbox", () => {
            const wrapper = mount(<Upload {...validProps}/>);
            expect(wrapper.findWhere(c => c.prop("label") === "through server").find("Checkbox").length).toEqual(1);
        });
        it("renders submit", () => {
            const wrapper = mount(<Upload {...validProps}/>);
            expect(wrapper.find("Button").text()).toContain("Upload");
        });
    });

    describe("events", () => {
        it("on name change", () => {
            const wrapper = shallow(<Upload {...validProps}/>);
            let nameTextField = wrapper.find("TextField").at(0);
            const newValue = 'new name';
            nameTextField.simulate('change', {target: {value: newValue}});
            nameTextField = wrapper.find("TextField").at(0);
            expect(nameTextField.prop('value')).toEqual(newValue);
        });

        it("on parent change", () => {
            const wrapper = shallow(<Upload {...validProps}/>);
            let nameTextField = wrapper.find("TextField").at(1);
            const newValue = 'new parent';
            nameTextField.simulate('change', {target: {value: newValue}});
            nameTextField = wrapper.find("TextField").at(1);
            expect(nameTextField.prop('value')).toEqual(newValue);
        });

        it("on tag change", () => {
            const wrapper = shallow(<Upload {...validProps}/>);
            let nameTextField = wrapper.find("TextField").at(2);
            const newValue = 'new tag';
            nameTextField.simulate('change', {target: {value: newValue}});
            nameTextField = wrapper.find("TextField").at(2);
            expect(nameTextField.prop('value')).toEqual(newValue);
        });

        it("on checkbox change", () => {
            const wrapper = mount(<Upload {...validProps}/>);
            let checkbox = wrapper.find("Checkbox");
            expect(checkbox.prop('checked')).toEqual(false);
            const input = checkbox.find('input') as any;
            input.getDOMNode().checked = !input.getDOMNode().checked;
            input.simulate('change');
            checkbox = wrapper.find("Checkbox");
            expect(checkbox.prop('checked')).toEqual(true);
        });

        it("on file drop", (done) => {
            const file = new File([""], "filename");
            const wrapper = mount(<Upload {...validProps}/>);
            let dropzone = wrapper.find(Dropzone);
            const evt = {
                dataTransfer: {files: [file]}
            };
            dropzone.simulate('drop', evt);
            dropzone = wrapper.find(Dropzone);
            setTimeout(() => {
                expect(dropzone.html()).toContain("Dropped");
                expect(dropzone.html()).toContain("filename");
                expect(dropzone.html()).toContain("Bytes");
                wrapper.unmount();
                done();
            }, 10);
        });
    });


    describe("validation", () => {
        it("is valid", () => {
            const wrapper = mount(<Upload {...validProps}/>);
            wrapper.find("Button").simulate("click");
            expect(wrapper.html()).not.toContain("Invalid upload");
        });

        it("is invalid", () => {
            const wrapper = mount(<Upload {...invalidProps}/>);
            wrapper.find("Button").simulate("click");
            expect(wrapper.html()).toContain("Invalid upload");
        });
    });
});
