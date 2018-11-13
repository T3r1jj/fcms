import * as Atmosphere from "atmosphere.js";
import IConfiguration from "./IConfiguration";
import {INotifications} from "./INotifiations";

export class Client {

    public getConfiguration = () => {
        return fetch(this.getBackendPath + "/api/configuration")
            .then(response => {
                if (!response.ok) {
                    throw new Error(response.statusText)
                }
                return response.json() as Promise<IConfiguration>
            });
    };

    public updateConfiguration = (configuration: IConfiguration) => {
        return fetch(this.getBackendPath() + '/api/configuration', {
            body: JSON.stringify(configuration),
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            method: 'POST'
        });
    };

    public subscribeToNotifications(notifications: INotifications) {
        const socket: any = Atmosphere;
        const request: Atmosphere.Request = new (Atmosphere as any).AtmosphereRequest();
        request.url = 'http://localhost:8080/api/notification';
        request.contentType = "application/json";
        request.transport = 'websocket';
        request.fallbackTransport = 'long-polling';
        request.reconnectInterval = 1000 * 15;
        request.shared = true;

        request.onOpen = notifications.onOpen;
        request.onReconnect = notifications.onReconnect;
        request.onMessage = response => notifications.onMessage(response.status!, (response.status === 200) ? response.responseBody! : response.error!);
        request.onError = notifications.onError;
        request.onClose = notifications.onClose;
        request.onReopen = (_, response: Atmosphere.Response) => notifications.onReopen(response.transport!);
        request.onClientTimeout = notifications.onClientTimeout;
        socket.subscribe(request);
    }

    private getBackendPath = () => {
        return process.env.REACT_APP_BACKEND_PATH!;
    };
}