export enum EventType {
    INFO, WARNING, ERROR, DEBUG
}

// tslint:disable-next-line:no-namespace
export namespace EventType {
    export function toNotificationType(eventType: EventType): string {
        switch (eventType) {
            case EventType.INFO:
                return 'success';
            case EventType.WARNING:
                return 'warning';
            case EventType.ERROR:
                return 'error';
            case EventType.DEBUG:
                return 'info';
            default:
                return 'default';
        }
    }
}