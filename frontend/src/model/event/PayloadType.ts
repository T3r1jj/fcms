export enum PayloadType {
    SAVE = 'SAVE', DELETE = 'DELETE'
}

export enum ClientPayloadType {
    ADD = 'ADD', UPDATE = 'UPDATE', DELETE = 'DELETE'
}

// TODO: Refactor
// tslint:disable-next-line:no-namespace
export namespace ClientPayloadType {
    export function toNotificationType(eventType?: ClientPayloadType): string {
        switch (eventType) {
            case ClientPayloadType.ADD:
                return 'add';
            case ClientPayloadType.UPDATE:
                return 'update';
            case ClientPayloadType.DELETE:
                return 'delete';
            default:
                return 'default';
        }
    }
}