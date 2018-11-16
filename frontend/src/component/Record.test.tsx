import Dialog from "@material-ui/core/Dialog/Dialog";
import {mount, shallow} from 'enzyme';
import * as React from 'react';
import * as sinon from "sinon";

import Description from "./Description";
import Record, {IRecordProps} from './Record';

describe('component', () => {
    let props: IRecordProps
    beforeEach(() => {
        props = {
            backups: new Map<string, IBackup>(),
            description: "some description",
            hierarchyTooltipEnabled: true,
            id: "1",
            name: "Record name",
            onDescriptionChange: () => undefined,
            versions: []
        }
    });

    describe('rendering', () => {
        it("renders without crashing", () => {
            shallow(<Record {...props} />);
        });

        it("renders name", () => {
            const wrapper = shallow(<Record {...props} />);
            expect(wrapper.text()).toContain(props.name);
        });

        it("renders records count", () => {
            const wrapper = shallow(<Record {...props} />);
            expect(wrapper.text()).toContain("Records: 1");
        });

        it("renders versions depth", () => {
            const wrapper = shallow(<Record {...props} />);
            expect(wrapper.text()).toContain("Depth: 0");
        });

        it("renders backups count", () => {
            const wrapper = shallow(<Record {...props} />);
            expect(wrapper.text()).toContain("Backups: 0");
        });

        it("renders description option", () => {
            const wrapper = mount(<Record {...props} />);
            expect(wrapper.find('Tooltip').props().title).toEqual("Description");
        });

        it("renders hierarchy tooltip", () => {
            const wrapper = mount(<Record {...props} />);
            expect(wrapper.text()).toContain("Select child record");
        });

        it("does not render hierarchy tooltip", () => {
            const propsWithoutTooltip: IRecordProps = {...props, hierarchyTooltipEnabled: false};
            const wrapper = mount(<Record {...propsWithoutTooltip} />);
            expect(wrapper.text()).not.toContain("Select child record");
        });

        it("does not render more than one option", () => {
            const wrapper = mount(<Record {...props} />);
            expect(wrapper.find('Tooltip').length).toEqual(1);
        });

        it("renders backups if there are any", () => {
            const serviceA = "Service A";
            const serviceB = "Service B";
            const propsWithBackups: IRecordProps = {...props, backups: new Map([[serviceA, {} as IBackup], [serviceB, {} as IBackup]])};
            const wrapper = mount(<Record {...propsWithBackups} />);
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
        //     const wrapper = mount(<Record {...propsWithMeta} />)
        //     let metaRecordsCount = 0
        //     wrapper.find('Tooltip').forEach(n => (n.props().title === "Meta " + propsWithMeta.meta[0].name) ? metaRecordsCount++ : null)
        //     wrapper.find('Tooltip').forEach(n => (n.props().title === "Meta " + propsWithMeta.meta[1].name) ? metaRecordsCount++ : null)
        //     expect(metaRecordsCount).toEqual(2)
        // })

        it("renders version records if there are any", () => {
            const propsWithVersions: IRecordProps = {
                ...props,
                versions: [{...props, id: "V1", tag: "Version tag 1"}, {...props, id: "V2", tag: "Version tag 2"}]
            };
            const wrapper = mount(<Record {...propsWithVersions} />);
            let versionRecordCount = 0;
            wrapper.find('Tooltip').forEach(n => (n.props().title === "Version " + propsWithVersions.versions[0].tag) ? versionRecordCount++ : null);
            wrapper.find('Tooltip').forEach(n => (n.props().title === "Version " + propsWithVersions.versions[1].tag) ? versionRecordCount++ : null);
            expect(versionRecordCount).toEqual(2);
        });

        it("renders two records if selected", () => {
            const propsWithVersions: IRecordProps = {
                ...props,
                versions: [{...props, id: "V1", tag: "Version tag 1"}, {...props, id: "V2", tag: "Version tag 2"}]
            };
            const wrapper = mount(<Record {...propsWithVersions} />);
            expect(wrapper.find('Record').length).toEqual(1);
            wrapper.find("Tooltip").last().find('IconButton').simulate('click');
            expect(wrapper.find('Record').length).toEqual(2);
        });

        it("renders one record if selected twice", () => {
            const propsWithVersions: IRecordProps = {
                ...props,
                versions: [{...props, id: "V1", tag: "Version tag 1"}, {...props, id: "V2", tag: "Version tag 2"}]
            };
            const wrapper = mount(<Record {...propsWithVersions} />);
            const button = wrapper.find("Tooltip").last().find('IconButton');
            button.simulate('click');
            button.simulate('click');
            expect(wrapper.find('Record').length).toEqual(1);
        });

        it("renders description on click", () => {
            const wrapper = shallow(<Record {...props} />);
            const button = wrapper.findWhere(w => w.prop("title") === "Description").first();
            expect(wrapper.find(Description).length).toEqual(1);
            expect(wrapper.find(Dialog).prop('open')).toEqual(false);
            button.simulate('click');
            expect(wrapper.find(Dialog).prop('open')).toEqual(true);
        });
    });

    describe("state", () => {
        it("state update after in dialog", () => {
            const wrapper = mount(<Record {...props} />);
            const button = wrapper.findWhere(w => w.prop("title") === "Description").first();
            button.simulate('click');
            expect(wrapper.findWhere(c => c.prop("rawText") === props.description).length).toEqual(1);
            const updatedText = "new text";
            wrapper.find('textarea').last().simulate('change', {target: {value: updatedText}});
            expect(wrapper.findWhere(c => c.prop("rawText") === props.description).length).toEqual(0);
            expect(wrapper.findWhere(c => c.prop("rawText") === updatedText).length).toEqual(1);
        });

        it("state reset after closing dialog", () => {
            const wrapper = mount(<Record {...props} />);
            const openButton = wrapper.findWhere(w => w.prop("title") === "Description").first();
            openButton.simulate('click');
            const updatedText = "new text";
            wrapper.find('textarea').last().simulate('change', {target: {value: updatedText}});
            const closeButton = wrapper.find('Button').last();
            closeButton.simulate('click');
            openButton.simulate('click');
            expect(wrapper.findWhere(c => c.prop("rawText") === props.description).length).toEqual(1);
        });

        it("state update callback", () => {
            const spyOnSave = sinon.spy(props, 'onDescriptionChange');
            const wrapper = mount(<Record {...props} />);
            const button = wrapper.findWhere(w => w.prop("title") === "Description").first();
            button.simulate('click');
            expect(wrapper.findWhere(c => c.prop("rawText") === props.description).length).toEqual(1);
            const updatedText = "new text";
            wrapper.find('textarea').last().simulate('change', {target: {value: updatedText}});
            const allButtons = wrapper.find('Button');
            const saveButton = allButtons.at(allButtons.length - 2);
            saveButton.simulate('click');
            expect(spyOnSave.callCount).toEqual(1);
            expect(spyOnSave.args[0][0]).toEqual(props.id);
            expect(spyOnSave.args[0][1]).not.toEqual(props.description);
            expect(spyOnSave.args[0][1]).toEqual(updatedText);
        });
    });
});