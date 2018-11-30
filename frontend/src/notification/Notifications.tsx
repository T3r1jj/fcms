import Button from "@material-ui/core/Button/Button";
import {deserialize} from "class-transformer";
import {validateSync} from "class-validator";
import {InjectedNotistackProps, withSnackbar} from "notistack";
import * as React from "react";
import Event from "../model/event/Event";
import {EventType} from "../model/event/EventType";
import {ClientPayloadType} from "../model/event/PayloadType";
import {INotifications} from "../model/INotifiations";

// TODO: Enforce VariantType from App
export class Notifications extends React.Component<INotificationsProps, {}> implements INotifications {

    private static readonly ONE_MINUTE = 1000 * 60;
    private static readonly SEC_5 = 1000 * 5;
    private static readonly FOREVER = 2000000000;

    public componentDidMount(): void {
        this.props.subscribeToNotifications(this);
    }

    public render() {
        return (null);
    }

    public componentWillReceiveProps(nextProps: Readonly<INotificationsProps>, nextContext: any): void {
        if (nextProps.eventToDismiss !== undefined && nextProps.eventToDismiss.id !== (this.props.eventToDismiss ? this.props.eventToDismiss.id : undefined)) {
            const element: any = document.getElementById(nextProps.eventToDismiss.id);
            if (element) {
                element.click();
            }
        }
    }

    public onOpen = () => {
        this.props.enqueueSnackbar("Connected to the server", {
            action: this.createDismissButton(),
            autoHideDuration: Notifications.ONE_MINUTE,
            variant: 'success'
        })
    };

    public onReconnect = () => {
        this.props.enqueueSnackbar("Reconnecting to the server", {
            action: this.createDismissButton(),
            autoHideDuration: Notifications.ONE_MINUTE,
            variant: 'warning',
        })
    };

    public onMessage = (status: number, message: string) => {
        if (status === 200) {
            const event = deserialize(Event, message);
            let errors: any;
            try {
                errors = validateSync(event);
            } catch (e) {
                errors = [e];
            } finally {
                if (errors.length === 0) {
                    if (event.type === EventType.PAYLOAD) {
                        event.payload!.onConsume = (info, variant) => {
                            this.props.enqueueSnackbar(info, {
                                action: this.createDismissButton(event),
                                autoHideDuration: Notifications.SEC_5,
                                variant: ClientPayloadType.toNotificationType(variant) as any
                            });
                        }
                    } else {
                        this.props.enqueueSnackbar(event.title, {
                            action: this.createDismissButton(event),
                            variant: EventType.toNotificationType(event.type) as any
                        });
                    }
                    this.props.onEventReceived(event);
                } else {
                    this.props.enqueueSnackbar('Invalid json', {
                        action: this.createDismissButton(),
                        autoHideDuration: Notifications.FOREVER,
                        variant: 'error',
                    });
                    window.console.error("Validation failed: ", errors);
                }
            }
        } else {
            this.props.enqueueSnackbar('Invalid response status' + status + "\n" + message, {
                action: this.createDismissButton(),
                autoHideDuration: Notifications.FOREVER,
                variant: 'error',
            });
            window.console.error('Invalid response status' + status + "\n" + message);
        }
    };

    public onError = () => {
        this.props.enqueueSnackbar('No connection with socket or the server is down', {
            action: this.createDismissButton(),
            autoHideDuration: Notifications.FOREVER,
            variant: 'error',
        });
    };

    public onClose = () => {
        this.props.enqueueSnackbar('Server closed the connection after a timeout', {
            action: this.createDismissButton(),
            autoHideDuration: Notifications.ONE_MINUTE,
            variant: 'warning',
        });
    };

    public onReopen = (protocol: string) => {
        this.props.enqueueSnackbar("Reconnecting to the server using " + protocol, {
            action: this.createDismissButton(),
            autoHideDuration: Notifications.ONE_MINUTE,
            variant: 'warning',
        })
    };

    public onClientTimeout = () => {
        this.props.enqueueSnackbar("Connection timeout", {
            action: this.createDismissButton(),
            autoHideDuration: Notifications.FOREVER,
            variant: 'warning',
        })
    };

    private createDismissButton = (event?: Event) => {
        if (event && event.type !== EventType.PAYLOAD) {
            const onClick = (e: any) => {
                if (e.isTrusted) {
                    this.props.onEventDismiss({...event} as Event);
                }
            };
            return <Button id={event.id} size="small" color={"inherit"} onClick={onClick}>Mark as read</Button>
        } else {
            return <Button size="small" color={"inherit"}>Dismiss</Button>
        }
    };
}

export interface INotificationsProps extends InjectedNotistackProps {
    eventToDismiss?: Event

    subscribeToNotifications(notifications: INotifications): void

    onEventReceived(event: Event): void

    onEventDismiss(event: Event): void
}

export default withSnackbar(Notifications);