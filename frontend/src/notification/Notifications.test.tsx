import {mount, shallow} from "enzyme";
import {OptionsObject, SnackbarProvider} from "notistack";
import * as React from "react";
import * as sinon from "sinon";
import {SinonSpy} from "sinon";
import {Event} from "../model/Event";
import {INotifications} from "../model/INotifiations";
import {INotificationsProps, Notifications} from "./Notifications";

describe('notifications', () => {
    let props: INotificationsProps;

    beforeEach(() => {
        props = {
            enqueueSnackbar: (message, options?) => undefined,
            onPresentSnackbar: variant => undefined,
            subscribeToNotifications: (notifications: INotifications) => undefined,
        }
    });

    describe('rendering', () => {
        it('renders without crashing', () => {
            shallow(<Notifications {...props} />)
        })

    });
    describe('notificating', () => {
        let snackbarSpy: SinonSpy;
        let notificationCallbacks: INotifications;
        const defaultAutoHideTime = 1000 * 60 * 15;

        beforeEach(() => {
            snackbarSpy = sinon.spy(props, "enqueueSnackbar");
            props = {
                ...props, subscribeToNotifications(notifications: INotifications) {
                    notificationCallbacks = notifications;
                }
            };
        });

        function getTitle() {
            return snackbarSpy.args[0][0] as string;
        }

        function getOptions() {
            return snackbarSpy.args[0][1] as OptionsObject;
        }

        function mountWrapperWithProps() {
            return mount(<SnackbarProvider
                maxSnack={1}
                autoHideDuration={defaultAutoHideTime}>
                <div><Notifications enqueueSnackbar={props.enqueueSnackbar}
                                    subscribeToNotifications={props.subscribeToNotifications}
                                    onPresentSnackbar={props.onPresentSnackbar}/></div>
            </SnackbarProvider>)
        }

        it('notifies about connecting', () => {
            mountWrapperWithProps();
            notificationCallbacks.onOpen();
            expect(snackbarSpy.called).toBeTruthy();
            expect(getTitle()).toContain("Connected");
        });

        it('notifies about timeout', () => {
            mountWrapperWithProps();
            notificationCallbacks.onClientTimeout();
            expect(snackbarSpy.called).toBeTruthy();
            expect(getTitle().toLowerCase()).toContain("timeout");
        });

        it('timeout notification is long enough', () => {// TODO: check what's the resumeHide
            mountWrapperWithProps();
            notificationCallbacks.onClientTimeout();
            expect(snackbarSpy.called).toBeTruthy();
            expect(getOptions().autoHideDuration).toBeGreaterThan(1000 * 60 * 60);
        });

        it('notifies about error', () => {
            mountWrapperWithProps();
            notificationCallbacks.onError();
            expect(snackbarSpy.called).toBeTruthy();
            expect(getTitle().toLowerCase()).toContain("no connection");
        });

        it('error notification is long enough', () => {
            mountWrapperWithProps();
            notificationCallbacks.onError();
            expect(snackbarSpy.called).toBeTruthy();
            expect(getOptions().autoHideDuration).toBeGreaterThan(1000 * 60 * 60);
        });

        it('notifies about disconnect', () => {
            mountWrapperWithProps();
            notificationCallbacks.onClose();
            expect(snackbarSpy.called).toBeTruthy();
            expect(getTitle().toLowerCase()).toContain("closed");
        });

        it('notifies about protocol change', () => {
            mountWrapperWithProps();
            const protocol = "thoughts transfer";
            notificationCallbacks.onReopen(protocol);
            expect(snackbarSpy.called).toBeTruthy();
            expect(getTitle().toLowerCase()).toContain(protocol);
        });

        it('notifies about reconnecting', () => {
            mountWrapperWithProps();
            notificationCallbacks.onReconnect();
            expect(snackbarSpy.called).toBeTruthy();
            expect(getTitle().toLowerCase()).toContain("reconnecting");
        });

        it('notifies about event from the backend', () => {
            mountWrapperWithProps();
            const event = new Event();
            event.title = "end of the world";
            event.description = "end of the description";
            const preMessage: any = JSON.parse(JSON.stringify(event));
            preMessage.time = "2018-11-13T12:48:51.731Z";
            const message = JSON.stringify(preMessage);
            notificationCallbacks.onMessage(200, message);
            expect(snackbarSpy.called).toBeTruthy();
            expect(getTitle().toLowerCase()).toContain(event.title);
        });

        it('notifies about event parsing error', () => {
            mountWrapperWithProps();
            const event = "end of the world";
            const message = JSON.stringify(event);
            notificationCallbacks.onMessage(200, message);
            expect(snackbarSpy.called).toBeTruthy();
            expect(getTitle().toLowerCase()).toContain("json");
        });

        it('notifies about event parsing error', () => {
            mountWrapperWithProps();
            const error = "end of the world";
            notificationCallbacks.onMessage(404, error);
            expect(snackbarSpy.called).toBeTruthy();
            expect(getTitle().toLowerCase()).toContain("status");
        });
    });
});
