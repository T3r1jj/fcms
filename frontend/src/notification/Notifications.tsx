import Button from "@material-ui/core/Button/Button";
import {deserialize} from "class-transformer";
import {validateSync} from "class-validator";
import {InjectedNotistackProps, withSnackbar} from "notistack";
import * as React from "react";
import Event from "../model/event/Event";
import {EventType} from "../model/event/EventType";
import {INotifications} from "../model/INotifiations";

export class Notifications extends React.Component<INotificationsProps, {}> implements INotifications {

    private static readonly DISMISS_BUTTON = <Button size="small" color={"inherit"}>Dismiss</Button>;
    private static readonly ONE_MINUTE = 1000 * 60;
    private static readonly FOREVER = Number.MAX_VALUE;

    public componentDidMount(): void {
        this.props.subscribeToNotifications(this);
    }

    public render() {
        return (null);
    }

    public onOpen = () => {
        this.props.enqueueSnackbar("Connected to the server", {
            action: Notifications.DISMISS_BUTTON,
            autoHideDuration: Notifications.ONE_MINUTE,
            variant: 'success'
        })
    };

    public onReconnect = () => {
        this.props.enqueueSnackbar("Reconnecting to the server", {
            action: Notifications.DISMISS_BUTTON,
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
                    this.props.enqueueSnackbar(event.title, {
                        action: Notifications.DISMISS_BUTTON,
                        variant: EventType.toNotificationType(event.type) as any,
                    });
                    this.props.onEventReceived(event);
                } else {
                    this.props.enqueueSnackbar('Invalid json', {
                        action: Notifications.DISMISS_BUTTON,
                        autoHideDuration: Notifications.FOREVER,
                        variant: 'error',
                    });
                    window.console.error("Validation failed: ", errors);
                }
            }
        } else {
            this.props.enqueueSnackbar('Invalid response status' + status + "\n" + message, {
                action: Notifications.DISMISS_BUTTON,
                autoHideDuration: Notifications.FOREVER,
                variant: 'error',
            });
            window.console.error('Invalid response status' + status + "\n" + message);
        }
    };

    public onError = () => {
        this.props.enqueueSnackbar('No connection with socket or the server is down', {
            action: Notifications.DISMISS_BUTTON,
            autoHideDuration: Notifications.FOREVER,
            variant: 'error',
        });
    };

    public onClose = () => {
        this.props.enqueueSnackbar('Server closed the connection after a timeout', {
            action: Notifications.DISMISS_BUTTON,
            autoHideDuration: Notifications.ONE_MINUTE,
            variant: 'warning',
        });
    };

    public onReopen = (protocol: string) => {
        this.props.enqueueSnackbar("Reconnecting to the server using " + protocol, {
            action: Notifications.DISMISS_BUTTON,
            autoHideDuration: Notifications.ONE_MINUTE,
            variant: 'warning',
        })
    };

    public onClientTimeout = () => {
        this.props.enqueueSnackbar("Connection timeout", {
            action: Notifications.DISMISS_BUTTON,
            autoHideDuration: Notifications.FOREVER,
            variant: 'warning',
        })
    };
}

export interface INotificationsProps extends InjectedNotistackProps {
    subscribeToNotifications(notifications: INotifications): void
    onEventReceived(event: Event): void
}

export default withSnackbar(Notifications);
