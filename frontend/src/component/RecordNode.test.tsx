import Button from "@material-ui/core/Button/Button";
import Dialog from "@material-ui/core/Dialog/Dialog";
import {mount, shallow} from 'enzyme';
import * as React from 'react';
import * as sinon from "sinon";
import IBackup from "../model/IBackup";

import RecordMeta from "../model/RecordMeta";
import Description from "./Description";
import {IRecordProps, RecordNode} from './RecordNode';

describe('component', () => {
    let props: IRecordProps;
    let meta: RecordMeta;
    beforeEach(() => {
        meta = new RecordMeta();
        meta.id = "2";
        meta.description = "some description";
        meta.name = "RecordNode name";
        meta.tag = "some tag";
        props = {
            backups: new Map<string, IBackup>(),
            classes: {},
            expand: false,
            hierarchyTooltipEnabled: true,
            id: "1",
            lazyLoad: false,
            meta,
            root: true,
            updateParentId: (parentId: string) => {
                ;
            },
            updateRecordMeta: () => new Promise<Response>((resolve => resolve(new Response()))),

            deleteRecords: () => new Promise<Response>((resolve => resolve(new Response()))),


            forceDeleteRecords: () => new Promise<Response>((resolve => resolve(new Response()))),
            versions: []
        }
    });

    describe('rendering', () => {
        it("renders without crashing", () => {
            shallow(<RecordNode {...props} />);
        });

        it("renders name", () => {
            const wrapper = shallow(<RecordNode {...props} />);
            expect(wrapper.text()).toContain(props.meta.name);
        });

        it("renders records count", () => {
            const wrapper = shallow(<RecordNode {...props} />);
            expect(wrapper.text()).toContain("Records: 1");
        });

        it("renders versions depth", () => {
            const wrapper = shallow(<RecordNode {...props} />);
            expect(wrapper.text()).toContain("Depth: 0");
        });

        it("renders backups count", () => {
            const wrapper = shallow(<RecordNode {...props} />);
            expect(wrapper.text()).toContain("Backups: 0");
        });

        it("renders description option", () => {
            const wrapper = mount(<RecordNode {...props} />);
            expect(wrapper.find('Tooltip').at(3).props().title).toEqual("Description");
        });

        it("renders hierarchy expand option", () => {
            const wrapper = mount(<RecordNode {...props} />);
            expect(wrapper.find('Tooltip').at(0).props().title).toEqual(props.meta.tag);
        });

        it("does not render hierarchy tooltip", () => {
            const propsWithoutTooltip: IRecordProps = {...props, hierarchyTooltipEnabled: false};
            const wrapper = mount(<RecordNode {...propsWithoutTooltip} />);
            expect(wrapper.text()).not.toContain("Select child record");
        });

        it("does not render more or less than 5 options (expand,add,update,delete)", () => {
            const wrapper = mount(<RecordNode {...props} />);
            expect(wrapper.find('Tooltip').length).toEqual(5);
        });

        it("renders backups if there are any", () => {
            const serviceA = "Service A";
            const serviceB = "Service B";
            const propsWithBackups: IRecordProps = {
                ...props,
                backups: new Map([[serviceA, {} as IBackup], [serviceB, {} as IBackup]])
            };
            const wrapper = mount(<RecordNode {...propsWithBackups} />);
            let backupOptionCount = 0;
            wrapper.find('Tooltip').forEach(n => (n.props().title === "Backup " + serviceA) ? backupOptionCount++ : null);
            wrapper.find('Tooltip').forEach(n => (n.props().title === "Backup " + serviceB) ? backupOptionCount++ : null);
            expect(backupOptionCount).toEqual(2);
        });

        // it("renders meta records if there are any", () => {
        //     const propsWithMeta: IRecordProps = {
        //         ...props,
        //         meta: [{...props, id: "Meta1", name: "Meta name 1"}, {...props, id: "Meta2", name: "Meta name 2"}]
        //     }
        //     const wrapper = mount(<RecordNode {...propsWithMeta} />)
        //     let metaRecordsCount = 0
        //     wrapper.find('Tooltip').forEach(n => (n.props().title === "Meta " + propsWithMeta.meta[0].name) ? metaRecordsCount++ : null)
        //     wrapper.find('Tooltip').forEach(n => (n.props().title === "Meta " + propsWithMeta.meta[1].name) ? metaRecordsCount++ : null)
        //     expect(metaRecordsCount).toEqual(2)
        // })

        it("does not render children if not expanded", () => {
            const propsWithVersions: IRecordProps = {
                ...props,
                expand: false,
                versions: [{...props, id: "V1", meta: {...props.meta, tag: "Version tag 1"}}, {...props, id: "V2", meta: {...props.meta, tag: "Version tag 2"}}]
            };
            const wrapper = mount(<RecordNode {...propsWithVersions} />);
            expect(wrapper.find(RecordNode).length).toEqual(1);
        });

        it("renders two records if expanded", () => {
            const propsWithVersions: IRecordProps = {
                ...props,
                expand: true,
                versions: [{...props, id: "V1", meta: {...props.meta, tag: "Version tag 1"}, versions: []}, {
                    ...props,
                    id: "V2",
                    meta: {...props.meta, tag: "Version tag 2"},
                    versions: []
                }]
            };
            const wrapper = mount(<RecordNode {...propsWithVersions} />);
            expect(wrapper.find(RecordNode).length).toEqual(propsWithVersions.versions.length + 1);
        });

        it("renders one record if selected twice", () => {
            const propsWithVersions: IRecordProps = {
                ...props,
                versions: [{...props, id: "V1", meta: {...props.meta, tag: "Version tag 1"}, versions: []}, {
                    ...props,
                    id: "V2",
                    meta: {...props.meta, tag: "Version tag 2"},
                    versions: []
                }]
            };
            const wrapper = mount(<RecordNode {...propsWithVersions} />);
            const button = wrapper.find("Tooltip").last().find('IconButton');
            button.simulate('click');
            button.simulate('click');
            expect(wrapper.find('RecordNode').length).toEqual(1);
        });

        it("renders description on click", () => {
            const wrapper = shallow(<RecordNode {...props} />);
            const button = wrapper.findWhere(w => w.prop("title") === "Description").first();
            expect(wrapper.find(Description).length).toEqual(1);
            expect(wrapper.find(Dialog).at(1).prop('open')).toEqual(false);
            button.simulate('click');
            expect(wrapper.find(Dialog).at(1).prop('open')).toEqual(true);
        });

        it("renders deletion on click", () => {
            const wrapper = shallow(<RecordNode {...props} />);
            const button = wrapper.findWhere(w => w.prop("title") === "Delete").first();
            expect(wrapper.find(Description).length).toEqual(1);
            expect(wrapper.find(Dialog).at(2).prop('open')).toEqual(false);
            button.simulate('click');
            expect(wrapper.find(Dialog).at(2).prop('open')).toEqual(true);
        });
    });

    describe("state", () => {
        it("state update after in dialog", () => {
            const wrapper = mount(<RecordNode {...props} />);
            const button = wrapper.findWhere(w => w.prop("title") === "Description").first();
            button.simulate('click');
            expect(wrapper.findWhere(c => c.prop("rawText") === props.meta.description).length).toEqual(1);
            const updatedText = "new text";
            wrapper.find('textarea').last().simulate('change', {target: {value: updatedText}});
            expect(wrapper.findWhere(c => c.prop("rawText") === props.meta.description).length).toEqual(0);
            expect(wrapper.findWhere(c => c.prop("rawText") === updatedText).length).toEqual(1);
        });

        it("state reset after closing dialog", () => {
            const wrapper = mount(<RecordNode {...props} />);
            const openButton = wrapper.findWhere(w => w.prop("title") === "Description").first();
            openButton.simulate('click');
            const updatedText = "new text";
            wrapper.find('textarea').last().simulate('change', {target: {value: updatedText}});
            const closeButton = wrapper.find('Button').last();
            closeButton.simulate('click');
            openButton.simulate('click');
            expect(wrapper.findWhere(c => c.prop("rawText") === props.meta.description).length).toEqual(1);
        });

        it("state update callback", () => {
            const spyOnSave = sinon.spy(props, 'updateRecordMeta');
            const wrapper = mount(<RecordNode {...props} />);
            const button = wrapper.findWhere(w => w.prop("title") === "Description").first();
            button.simulate('click');
            expect(wrapper.findWhere(c => c.prop("rawText") === props.meta.description).length).toEqual(1);
            const updatedText = "new text";
            wrapper.find('textarea').last().simulate('change', {target: {value: updatedText}});
            const allButtons = wrapper.find('Button');
            const saveButton = allButtons.at(allButtons.length - 2);
            saveButton.simulate('click');
            expect(spyOnSave.callCount).toEqual(1);
            expect(spyOnSave.args[0][0]).toEqual({...props.meta, description: updatedText});
        });
    });

    describe("callback", () => {
        it("pressing new fires a callback with record id", () => {
            const updateParentIdSpy = sinon.spy(props, "updateParentId");
            const wrapper = mount(<RecordNode {...props} />);
            const button = wrapper.findWhere(w => w.prop("title") === "Create new record").first();
            button.simulate('click');
            expect(updateParentIdSpy.calledOnce).toBeTruthy();
            expect(updateParentIdSpy.args[0][0]).toEqual(props.id);
        });

        it("calls description update on save click", () => {
            const updateDescriptionSpy = sinon.spy(props, "updateRecordMeta");
            const wrapper = shallow(<RecordNode {...props} />);
            const button = wrapper.findWhere(w => w.prop("title") === "Description").first();
            button.simulate('click');
            const saveButton = wrapper.find(Dialog).first().find(Button).first();
            saveButton.simulate('click');
            expect(updateDescriptionSpy.calledOnce).toBeTruthy();
            expect(updateDescriptionSpy.args[0][0]).toEqual(props.meta);
        });

        it("does not call description update on cancel click", () => {
            const updateDescriptionSpy = sinon.spy(props, "updateRecordMeta");
            const wrapper = shallow(<RecordNode {...props} />);
            const button = wrapper.findWhere(w => w.prop("title") === "Description").first();
            button.simulate('click');
            const saveButton = wrapper.find(Dialog).first().find(Button).at(1);
            saveButton.simulate('click');
            expect(updateDescriptionSpy.calledOnce).toBeFalsy();
        });

        it("calls delete on standard delete click", () => {
            const deleteSpy = sinon.spy(props, "deleteRecords");
            const wrapper = shallow(<RecordNode {...props} />);
            const button = wrapper.findWhere(w => w.prop("title") === "Delete").first();
            button.simulate('click');
            const deleteButton = wrapper.find(Dialog).at(2).find(Button).first();
            deleteButton.simulate('click');
            expect(deleteSpy.calledOnce).toBeTruthy();
            expect(deleteSpy.args[0][0]).toEqual(props.id);
        });

        it("calls force delete on force delete click", () => {
            const forceDeleteSpy = sinon.spy(props, "forceDeleteRecords");
            const wrapper = shallow(<RecordNode {...props} />);
            const button = wrapper.findWhere(w => w.prop("title") === "Delete").first();
            button.simulate('click');
            const deleteButton = wrapper.find(Dialog).at(2).find(Button).at(1);
            deleteButton.simulate('click');
            expect(forceDeleteSpy.calledOnce).toBeTruthy();
            expect(forceDeleteSpy.args[0][0]).toEqual(props.id);
        });

        it("does not call any delete on cancel click", () => {
            const deleteSpy = sinon.spy(props, "deleteRecords");
            const forceDeleteSpy = sinon.spy(props, "forceDeleteRecords");
            const wrapper = shallow(<RecordNode {...props} />);
            const button = wrapper.findWhere(w => w.prop("title") === "Delete").first();
            button.simulate('click');
            const deleteButton = wrapper.find(Dialog).at(2).find(Button).at(2);
            deleteButton.simulate('click');
            expect(deleteSpy.calledOnce).toBeFalsy();
            expect(forceDeleteSpy.calledOnce).toBeFalsy();
        });
    });
});