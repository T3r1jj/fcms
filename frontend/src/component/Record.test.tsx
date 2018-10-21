import {mount, shallow} from 'enzyme';
import * as React from 'react';

import Record, {IRecordProps} from './Record';
import Description from "./Description";
import Dialog from "@material-ui/core/Dialog/Dialog";

describe('component', () => {
    let props: IRecordProps
    beforeEach(() => {
        props = {
            name: "Record name",
            id: "1",
            description: "some description",
            meta: [],
            versions: [],
            backups: [],
            hierarchyTooltipEnabled: true,
            onDescriptionChange: () => undefined
        }
    })

    describe('rendering', () => {
        it("renders without crashing", () => {
            shallow(<Record {...props} />)
        })

        it("renders name", () => {
            const wrapper = shallow(<Record {...props} />)
            expect(wrapper.text()).toContain(props.name)
        })

        it("renders records count", () => {
            const wrapper = shallow(<Record {...props} />)
            expect(wrapper.text()).toContain("Records: 1")
        })

        it("renders versions depth", () => {
            const wrapper = shallow(<Record {...props} />)
            expect(wrapper.text()).toContain("Depth: 0")
        })

        it("renders backups count", () => {
            const wrapper = shallow(<Record {...props} />)
            expect(wrapper.text()).toContain("Backups: 0")
        })

        it("renders description option", () => {
            const wrapper = mount(<Record {...props} />)
            expect(wrapper.find('Tooltip').props().title).toEqual("Description")
        })

        it("renders hierarchy tooltip", () => {
            const wrapper = mount(<Record {...props} />)
            expect(wrapper.text()).toContain("Select child record")
        })

        it("does not render hierarchy tooltip", () => {
            const propsWithoutTooltip: IRecordProps = {...props, hierarchyTooltipEnabled: false}
            const wrapper = mount(<Record {...propsWithoutTooltip} />)
            expect(wrapper.text()).not.toContain("Select child record")
        })

        it("does not render more than one option", () => {
            const wrapper = mount(<Record {...props} />)
            expect(wrapper.find('Tooltip').length).toEqual(1)
        })

        it("renders backups if there are any", () => {
            const propsWithBackups: IRecordProps = {...props, backups: [{service: "Service A"}, {service: "Service B"}]}
            const wrapper = mount(<Record {...propsWithBackups} />)
            let backupOptionCount = 0
            wrapper.find('Tooltip').forEach(n => (n.props().title === "Backup " + propsWithBackups.backups[0].service) ? backupOptionCount++ : null)
            wrapper.find('Tooltip').forEach(n => (n.props().title === "Backup " + propsWithBackups.backups[1].service) ? backupOptionCount++ : null)
            expect(backupOptionCount).toEqual(2)
        })

        it("renders meta records if there are any", () => {
            const propsWithMeta: IRecordProps = {
                ...props,
                meta: [{...props, id: "Meta1", name: "Meta name 1"}, {...props, id: "Meta2", name: "Meta name 2"}]
            }
            const wrapper = mount(<Record {...propsWithMeta} />)
            let metaRecordsCount = 0
            wrapper.find('Tooltip').forEach(n => (n.props().title === "Meta " + propsWithMeta.meta[0].name) ? metaRecordsCount++ : null)
            wrapper.find('Tooltip').forEach(n => (n.props().title === "Meta " + propsWithMeta.meta[1].name) ? metaRecordsCount++ : null)
            expect(metaRecordsCount).toEqual(2)
        })

        it("renders version records if there are any", () => {
            const propsWithVersions: IRecordProps = {
                ...props,
                versions: [{...props, id: "V1", tag: "Version tag 1"}, {...props, id: "V2", tag: "Version tag 2"}]
            }
            const wrapper = mount(<Record {...propsWithVersions} />)
            let versionRecordCount = 0
            wrapper.find('Tooltip').forEach(n => (n.props().title === "Version " + propsWithVersions.versions[0].tag) ? versionRecordCount++ : null)
            wrapper.find('Tooltip').forEach(n => (n.props().title === "Version " + propsWithVersions.versions[1].tag) ? versionRecordCount++ : null)
            expect(versionRecordCount).toEqual(2)
        })

        it("renders two records if selected", () => {
            const propsWithVersions: IRecordProps = {
                ...props,
                versions: [{...props, id: "V1", tag: "Version tag 1"}, {...props, id: "V2", tag: "Version tag 2"}]
            }
            const wrapper = mount(<Record {...propsWithVersions} />)
            expect(wrapper.find('Record').length).toEqual(1)
            wrapper.find("Tooltip").last().find('IconButton').simulate('click')
            expect(wrapper.find('Record').length).toEqual(2)
        })

        it("renders one record if selected twice", () => {
            const propsWithVersions: IRecordProps = {
                ...props,
                versions: [{...props, id: "V1", tag: "Version tag 1"}, {...props, id: "V2", tag: "Version tag 2"}]
            }
            const wrapper = mount(<Record {...propsWithVersions} />)
            const button = wrapper.find("Tooltip").last().find('IconButton')
            button.simulate('click')
            button.simulate('click')
            expect(wrapper.find('Record').length).toEqual(1)
        })

        it("renders description on click", () => {
            const wrapper = shallow(<Record {...props} />)
            const button = wrapper.findWhere(w => w.prop("title") === "Description").first()
            expect(wrapper.find(Description).length).toEqual(1)
            expect(wrapper.find(Dialog).prop('open')).toEqual(false)
            button.simulate('click')
            expect(wrapper.find(Dialog).prop('open')).toEqual(true)
        })
    })
})