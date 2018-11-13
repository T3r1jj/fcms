export interface INotifications {
    onOpen: () => void;
    onReconnect: () => void;
    onMessage: (status: number, message: string) => void;
    onError: () => void;
    onClose: () => void;
    onReopen: (protocol: string) => void;
    onClientTimeout: () => void;
}