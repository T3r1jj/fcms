import * as Atmosphere from "atmosphere.js";
import {plainToClass} from "class-transformer";
import Code from "./code/Code";
import {CodeCallbackType} from "./code/CodeCallbackType";
import Event from "./event/Event";
import EventPage from "./event/EventPage";
import IConfiguration from "./IConfiguration";
import {INotifications} from "./INotifiations";
import Record from "./Record";

export default class Client {

    public getConfiguration = () => {
        return fetch(this.getBackendPath() + "/api/configuration")
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

    public isUploadValid = (file: File, name: string, parent: string, tag: string) => {
        return name.length !== 0 && ((parent.length === 0 && tag.length === 0) || (parent.length !== 0 && tag.length !== 0))
    };

    public upload = (file: File, name: string, parentId: string, tag: string, onProgress?: (event: ProgressEvent) => void) => {
        const params = [];
        params.push(`name=${name}`);
        if (parentId.length !== 0) {
            params.push(`parentId=${parentId}`);
        }
        params.push(`tag=${tag}`);
        const queryString = "?" + params.join('&');
        const formData = new FormData();
        formData.append("file", file, name);
        return this.futch(this.getBackendPath() + "/api/records" + queryString, formData, onProgress);
    };

    public getRecords = () => {
        return fetch(this.getBackendPath() + "/api/records")
            .then(response => {
                if (!response.ok) {
                    throw new Error(response.statusText)
                }
                return response.json()
            })
            .then(json => {
                return plainToClass(Record, json as Record[]);
            });
    };

    public updateRecordDescription = (id: string, description: string) => {
        return fetch(this.getBackendPath() + "/api/records?id=" + id, {
            body: description,
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            method: 'PUT'
        });
    };

    public deleteRecords = (id: string) => {
        return fetch(this.getBackendPath() + "/api/records?id=" + id, {
            method: 'DELETE'
        });
    };

    public forceDeleteRecords = (id: string) => {
        return fetch(this.getBackendPath() + "/api/records?id=" + id, {
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
        request.maxReconnectOnClose = 5;

        request.onOpen = notifications.onOpen;
        request.onReconnect = notifications.onReconnect;
        request.onMessage = response => notifications.onMessage(response.status!, (response.status === 200) ? response.responseBody! : response.error!);
        request.onError = notifications.onError;
        request.onClose = notifications.onClose;
        request.onReopen = (_, response: Atmosphere.Response) => notifications.onReopen(response.transport!);
        request.onClientTimeout = notifications.onClientTimeout;
        socket.subscribe(request);
    };

    private futch(url: string, formData: FormData, onProgress?: (event: ProgressEvent) => void) {
        return new Promise<Response>((resolve, reject) => {
            const xhr: XMLHttpRequest = new XMLHttpRequest();

            xhr.onreadystatechange = () => {
                if (xhr.readyState === 4) {
                    const body = (xhr.response !== undefined && xhr.response !== null) ? xhr.response : xhr.responseText;
                    resolve(new Response(body, {
                        status: xhr.status,
                        statusText: xhr.statusText,
                    }))
                }
            };
            xhr.onerror = () => {
                reject(new TypeError('Network request failed'))
            };

            xhr.ontimeout = () => {
                reject(new TypeError('Network request failed'))
            };

            xhr.onabort = () => {
                reject(new DOMException('Aborted', 'AbortError'))
            };

            xhr.upload.onprogress = (onProgress ? onProgress : null);
            xhr.open('POST', url, true);
            xhr.send(formData);
        });
    }

    private getBackendPath = () => {
        return process.env.REACT_APP_BACKEND_PATH!;
    };
}