export enum EventType {
    INFO = 'INFO', WARNING = 'WARNING', ERROR = 'ERROR', DEBUG = 'DEBUG', PAYLOAD = 'PAYLOAD'
}

// TODO: Refactor
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