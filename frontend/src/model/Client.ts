import * as Atmosphere from "atmosphere.js";
import {plainToClass} from "class-transformer";
import Code from "./code/Code";
import {CodeCallbackType} from "./code/CodeCallbackType";
import Event from "./event/Event";
import EventPage from "./event/EventPage";
import IConfiguration from "./IConfiguration";
import {INotifications} from "./INotifiations";

export default class Client {

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

    public getHistoryPage = (size: number, page: number) => {
        const queryString = "?" + [
            `size=${size}`,
            `page=${page}`,
        ].join('&');

        return fetch(this.getBackendPath() + "/api/history" + queryString)
            .then(response => {
                if (!response.ok) {
                    throw new Error(response.statusText)
                }
                return response.json()
            })
            .then(json => plainToClass(EventPage, json as EventPage));
    };

    public getHistory = () => {
        return fetch(this.getBackendPath() + "/api/history")
            .then(response => {
                if (!response.ok) {
                    throw new Error(response.statusText)
                }
                return response.json()
            })
            .then(json => {
                return plainToClass(Event, json as Event[]);
            })
    };

    public getCodeCallback = (type: CodeCallbackType) => {
        return fetch(this.getBackendPath() + "/api/code?type=" + CodeCallbackType[type])
            .then(response => {
                if (!response.ok) {
                    throw new Error(response.statusText)
                }
                return response.json()
            })
            .then(json => {
                return plainToClass(Code, json as Code);
            })
    };

    public checkCodeCallback = (type: CodeCallbackType) => {
        return fetch(this.getBackendPath() + "/api/code?type=" + CodeCallbackType[type], {method: 'POST'})
    };

    public updateCodeCallback = (code: Code) => {
        return fetch(this.getBackendPath() + "/api/code", {
            body: JSON.stringify(code),
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            method: 'PATCH'
        });
    };

    public subscribeToNotifications = (notifications: INotifications) => {
        const socket: any = Atmosphere;
        const request: Atmosphere.Request = new (Atmosphere as any).AtmosphereRequest();
        request.url = this.getBackendPath() + '/api/notification';
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