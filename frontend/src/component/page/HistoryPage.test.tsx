import Tooltip from "@material-ui/core/Tooltip/Tooltip";
import {mount, shallow} from "enzyme";
import * as React from "react";
import "reflect-metadata";
import Event from "../../model/event/Event";
import EventPage from "../../model/event/EventPage";
import {EventType} from "../../model/event/EventType";
import HistoryPage, {IHistoryPageProps} from "./HistoryPage";

describe("component", () => {
    let props: IHistoryPageProps;
    let getHistoryPage: (size: number, page?: number) => Promise<EventPage>;
    let getHistory: () => Promise<Event[]>;

    beforeEach(() => {
        getHistoryPage = () => {
            return new Promise<EventPage>((resolve) => {
                resolve(new EventPage());
            });
        };
        getHistory = () => {
            return new Promise<Event[]>(((resolve) => {
                resolve([new Event()])
            }));
        };
        props = {getHistory, getHistoryPage};
    });

    describe("rendering", () => {
        function prepareGetHistoryPagePromise(event: Event) {
            getHistoryPage = () => {
                return new Promise<EventPage>((resolve) => {
                    event.time = new Date();
                    event.title = "Database is flooding";
                    event.description = "Switch off the power";
                    event.type = EventType.ERROR;
                    const eventPage = new EventPage();
                    eventPage.totalElements = 1;
                    eventPage.content = [event];
                    resolve(eventPage);
                });
            };
        }

        function prepareGetHistoryPromise(event: Event) {
            getHistory = () => {
                return new Promise<Event[]>((resolve) => {
                    event.time = new Date();
                    event.title = "Database is safe";
                    event.description = "It's in the clouds";
                    event.type = EventType.ERROR;
                    resolve([event]);
                });
            };
        }

        it('renders without crashing', () => {
            shallow(<HistoryPage {...props}/>)
        });

        it('renders event from pageable call after component did mount', (done) => {
            const event = new Event();
            prepareGetHistoryPagePromise(event);
            props = {getHistory, getHistoryPage};

            const wrapper = mount(<HistoryPage {...props}/>);
            setImmediate(() => {
                wrapper.update();
                expect(wrapper.text()).toContain(event.title);
                expect(wrapper.text()).toContain(event.description);
                expect(wrapper.text()).toContain(event.type.toString());
                expect(wrapper.text()).toContain(event.time.toString());
                done();
            });
        });

        it('renders full history after sort click', (done) => {
            const event = new Event();
            const event2 = new Event();
            prepareGetHistoryPagePromise(event);
            prepareGetHistoryPromise(event2);
            props = {getHistory, getHistoryPage};

            const wrapper = mount(<HistoryPage {...props}/>);
            setImmediate(() => {
                wrapper.update();
                const sortButton = wrapper.find(Tooltip).filterWhere(p => p.prop("title") === "Sort").first();
                sortButton.simulate('click');
                setImmediate(() => {
                    wrapper.update();
                    expect(wrapper.text()).toContain(event2.title);
                    expect(wrapper.text()).toContain(event2.description);
                    expect(wrapper.text()).toContain(event2.type.toString());
                    expect(wrapper.text()).toContain(event2.time.toString());
                    done();
                });
            });
        });

        it('renders full history after filter set', (done) => {
            const event = new Event();
            const event2 = new Event();
            prepareGetHistoryPagePromise(event);
            prepareGetHistoryPromise(event2);
            props = {getHistory, getHistoryPage};

            const wrapper = mount(<HistoryPage {...props}/>);
            setImmediate(() => {
                wrapper.update();
                wrapper.find('button').filterWhere(p => p.prop("aria-label") === "Filter Table").first().simulate('click');
                setImmediate(() => {
                    wrapper.update();
                    const input = wrapper.find('input').filterWhere(p => p.prop("value") === "ERROR").first() as any;
                    input.getDOMNode().checked = !input.getDOMNode().checked;
                    input.simulate('change');
                    setImmediate(() => {
                        wrapper.update();
                        expect(wrapper.text()).toContain(event2.title);
                        expect(wrapper.text()).toContain(event2.description);
                        expect(wrapper.text()).toContain(event2.type.toString());
                        expect(wrapper.text()).toContain(event2.time.toString());
                        done();
                    });
                });
            });
        });

        it('renders full history after search', (done) => {
            const event = new Event();
            const event2 = new Event();
            prepareGetHistoryPagePromise(event);
            prepareGetHistoryPromise(event2);
            props = {getHistory, getHistoryPage};

            const wrapper = mount(<HistoryPage {...props}/>);
            setImmediate(() => {
                wrapper.update();
                const searchIcon = wrapper.find('button').filterWhere(p => p.prop("aria-label") === "Search").first();
                searchIcon.simulate('click');
                setImmediate(() => {
                    wrapper.update();
                    const searchText = event2.title;
                    const searchField = wrapper.find('input').first();
                    searchField.simulate('change', {target: {value: searchText}});
                    setImmediate(() => {
                        wrapper.update();
                        expect(wrapper.text()).toContain(event2.title);
                        expect(wrapper.text()).toContain(event2.description);
                        expect(wrapper.text()).toContain(event2.type.toString());
                        expect(wrapper.text()).toContain(event2.time.toString());
                        done();
                    });
                });
            });
        });

        it('renders error after sort', (done) => {
            const event = new Event();
            const error = "some error";
            prepareGetHistoryPagePromise(event);
            getHistory = () => {
                return new Promise<Event[]>((resolve, reject) => {
                    reject(new Error(error));
                });
            };
            props = {getHistory, getHistoryPage};

            const wrapper = mount(<HistoryPage {...props}/>);
            setImmediate(() => {
                wrapper.update();
                const sortButton = wrapper.find(Tooltip).filterWhere(p => p.prop("title") === "Sort").first();
                sortButton.simulate('click');
                setImmediate(() => {
                    wrapper.update();
                    expect(wrapper.text()).toContain(error);
                    done();
                });
            });
        });

        it('renders error component did mount', (done) => {
            const error = "some error";
            getHistoryPage = () => {
                return new Promise<EventPage>((resolve, reject) => {
                    reject(new Error(error));
                });
            };
            props = {getHistory, getHistoryPage};

            const wrapper = mount(<HistoryPage {...props}/>);
            setImmediate(() => {
                wrapper.update();
                expect(wrapper.text()).toContain(error);
                done();
            });
        });
    });
});