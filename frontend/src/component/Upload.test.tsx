import Button from "@material-ui/core/Button/Button";
import LinearProgress from "@material-ui/core/LinearProgress/LinearProgress";
import {mount, shallow} from "enzyme";
import * as React from "react";
import Dropzone from "react-dropzone";
import sinon from 'sinon';
import Upload, {IUploadProps} from "./Upload";

describe("component", () => {
    let validProps: IUploadProps;
    let invalidProps: IUploadProps;

    beforeEach(() => {
        validProps = {
            isUploadValid: (file, name1, parent1, tag) => true,
            upload(file: File, name: string, parent: string, tag: string): Promise<Response> {
                return new Promise<Response>((resolve => resolve(new Response())))
            }
        };
        invalidProps = {
            isUploadValid: (file, name1, parent1, tag) => false,
            upload(file: File, name: string, parent: string, tag: string): Promise<Response> {
                return new Promise<Response>((resolve => resolve(new Response())))
            }
        }
    });

    describe("rendering", () => {
        it("renders without crashing", () => {
            shallow(<Upload {...validProps}/>);
        });

        it("renders name", () => {
            const wrapper = shallow(<Upload {...validProps}/>);
            expect(wrapper.find("TextField").at(0).prop("label")).toEqual("Name");
        });
        it("renders parentId", () => {
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

        it("renders error on server not ok response", (done) => {
            const errorMessage = "Not found parentId id";
            validProps.upload = (file: File, name: string, parent: string, tag: string) => {
                const response = new Response(null, {
                    status: 404
                });
                response.json = () => new Promise((resolve => resolve({message: errorMessage})));
                return new Promise<Response>((resolve => resolve(response)))
            };
            const wrapper = mount(<Upload {...validProps}/>);
            wrapper.find(Button).simulate('click');
            setImmediate(() => {
                wrapper.update();
                expect(wrapper.html()).toContain(errorMessage);
                done();
            });
        });

        it("does not render progress on server not ok response", (done) => {
            const errorMessage = "Not found parentId id";
            validProps.upload = () => {
                const response = new Response(null, {
                    status: 404
                });
                response.json = () => new Promise((resolve => resolve({message: errorMessage})));
                return new Promise<Response>((resolve => resolve(response)))
            };
            const wrapper = mount(<Upload {...validProps}/>);
            wrapper.find(Button).simulate('click');
            setImmediate(() => {
                wrapper.update();
                expect(wrapper.find(LinearProgress).length).toEqual(0);
                done();
            });
        });

        it("renders OK on server ok response", (done) => {
            validProps.upload = (file: File, name: string, parent: string, tag: string) => {
                const response = new Response(null, {
                    status: 200
                });
                return new Promise<Response>((resolve => resolve(response)))
            };
            const wrapper = mount(<Upload {...validProps}/>);
            wrapper.find(Button).simulate('click');
            setImmediate(() => {
                wrapper.update();
                expect(wrapper.html()).toContain("OK");
                done();
            });
        });

        it("does not render progress on server ok response", (done) => {
            validProps.upload = () => {
                const response = new Response(null, {
                    status: 200
                });
                return new Promise<Response>((resolve => resolve(response)))
            };
            const wrapper = mount(<Upload {...validProps}/>);
            wrapper.find(Button).simulate('click');
            setImmediate(() => {
                wrapper.update();
                expect(wrapper.find(LinearProgress).length).toEqual(0);
                done();
            });
        });

        it("renders progress when uploading", (done) => {
            validProps.upload = (file: File, name: string, parent: string, tag: string, onProgress?: (event: ProgressEvent) => void) => {
                return new Promise<Response>(() => {
                        onProgress!({
                            loaded: 1,
                            total: 2,
                        } as ProgressEvent);
                    }
                )
            };
            const wrapper = mount(<Upload {...validProps}/>);
            wrapper.find(Button).simulate('click');
            setImmediate(() => {
                wrapper.update();
                expect(wrapper.find(LinearProgress).length).toEqual(1);
                done();
            });
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

        it("on parentId change", () => {
            const wrapper = shallow(<Upload {...validProps}/>);
            let parentTextField = wrapper.find("TextField").at(1);
            const newValue = 'new parentId';
            parentTextField.simulate('change', {target: {value: newValue}});
            parentTextField = wrapper.find("TextField").at(1);
            expect(parentTextField.prop('value')).toEqual(newValue);
        });

        it("on tag change", () => {
            const wrapper = shallow(<Upload {...validProps}/>);
            let tagTextField = wrapper.find("TextField").at(2);
            const newValue = 'new tag';
            tagTextField.simulate('change', {target: {value: newValue}});
            tagTextField = wrapper.find("TextField").at(2);
            expect(tagTextField.prop('value')).toEqual(newValue);
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
            setImmediate(() => {
                expect(dropzone.html()).toContain("Dropped");
                expect(dropzone.html()).toContain("filename");
                expect(dropzone.html()).toContain("Bytes");
                wrapper.unmount();
                done();
            });
        });
    });

    describe("callback", () => {
        it("calls client with correct input", (done) => {
            validProps.upload = (file: File, name: string, parent: string, tag: string, onProgress?: (event: ProgressEvent) => void) => {
                const response = new Response(null, {
                    status: 200
                });
                return new Promise<Response>((resolve) => resolve(response));
            };
            const uploadStub = sinon.stub(validProps, "upload").callThrough();
            const wrapper = mount(<Upload {...validProps}/>);
            const newName = 'new name';
            const newParent = 'new parentId';
            const newTag = 'new tag';
            const overriddenName = "filename";
            const fileToUpload = new File([""], overriddenName);
            const nameTextField = wrapper.find("TextField").at(0).find('input');
            const parentTextField = wrapper.find("TextField").at(1).find('input');
            const tagTextField = wrapper.find("TextField").at(2).find('input');
            nameTextField.simulate('change', {target: {value: newName}});
            (nameTextField.getDOMNode() as any).value = newName;
            nameTextField.simulate('change', nameTextField);
            (parentTextField.getDOMNode() as any).value = newParent;
            parentTextField.simulate('change', parentTextField);
            (tagTextField.getDOMNode() as any).value = newTag;
            tagTextField.simulate('change', tagTextField);
            const dropzone = wrapper.find(Dropzone);
            const evt = {
                dataTransfer: {files: [fileToUpload]}
            };
            dropzone.simulate('drop', evt);
            setImmediate(() => {
                wrapper.update();
                wrapper.find(Button).simulate('click');
                setImmediate(() => {
                    wrapper.update();
                    expect(uploadStub.calledOnce).toBeTruthy();
                    expect(uploadStub.args[0][0]).toEqual(fileToUpload);
                    expect(uploadStub.args[0][1]).toEqual(overriddenName);
                    expect(uploadStub.args[0][2]).toEqual(newParent);
                    expect(uploadStub.args[0][3]).toEqual(newTag);
                    done();
                })
            })
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
})
;
