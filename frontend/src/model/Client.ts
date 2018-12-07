import * as Atmosphere from "atmosphere.js";
import {plainToClass} from "class-transformer";
import Code from "./code/Code";
import {CodeCallbackType} from "./code/CodeCallbackType";
import Event from "./event/Event";
import EventPage from "./event/EventPage";
import Health from "./health/Health";
import IConfiguration from "./IConfiguration";
import {INotifications} from "./INotifiations";
import Record from "./Record";
import RecordMeta from "./RecordMeta";

export default class Client {

    private headers = new Headers();
    private readonly username: string;
    private readonly password: string;
    private readonly serverUri: string;
    private readonly socket: any = Atmosphere;
    private readonly request: Atmosphere.Request = new (Atmosphere as any).AtmosphereRequest();

    constructor(username: string, password: string, serverUri: string) {
        this.username = username;
        this.password = password;
        this.serverUri = serverUri;
        this.headers.set('Authorization', 'Basic ' + new Buffer(this.username + ":" + this.password).toString('base64'));
    }

    public validateStatus(res: Response) {
        if (!res.ok) {
            if (res.statusText !== "") {
                throw new Error(res.statusText);
            } else {
                return res.json().then(json => {
                    throw new Error(json.message)
                }) as any;
            }
        }
        return res;
    }

    public isValidUser = () => {
        return this.countUnreadEvents();
    };

    public logout = () => {
        this.request.onOpen = undefined;
        this.request.onReconnect = undefined;
        this.request.onMessage = undefined;
        this.request.onError = undefined;
        this.request.onClose = undefined;
        this.request.onReopen = undefined;
        this.request.onClientTimeout = undefined;
        this.socket.unsubscribe();
    };

    public getConfiguration = () => {
        return fetch(this.getApiPath() + "/configuration", {
            headers: this.headers
        })
            .then(this.validateStatus)
            .then(response => {
                return response.json() as Promise<IConfiguration>
            });
    };

    public updateConfiguration = (configuration: IConfiguration) => {
        return fetch(this.getBackendPath() + '/api/configuration', {
            body: JSON.stringify(configuration),
            headers: {
                ...this.headers,
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            method: 'POST'
        }).then(this.validateStatus)
    };

    public getHealth = () => {
        return fetch(this.getApiPath() + "/health", {
            headers: this.headers
        })
            .then(this.validateStatus)
            .then(response => {
                return response.json()
            })
            .then(json => {
                return plainToClass(Health, json as Health);
            })
    };

    public getHistoryPage = (size: number, page: number) => {
        const queryString = "?" + [
            `size=${size}`,
            `page=${page}`,
        ].join('&');

        return fetch(this.getApiPath() + "/history" + queryString, {
            headers: this.headers
        })
            .then(this.validateStatus)
            .then(response => {
                return response.json()
            })
            .then(json => plainToClass(EventPage, json as EventPage));
    };

    public getHistory = () => {
        return fetch(this.getApiPath() + "/history", {
            headers: this.headers
        })
            .then(this.validateStatus)
            .then(response => {
                return response.json()
            })
            .then(json => {
                return plainToClass(Event, json as Event[]);
            });
    };

    public deleteHistory = () => {
        return fetch(this.getApiPath() + "/history", {
            headers: this.headers,
            method: 'DELETE'
        }).then(this.validateStatus);
    };

    public setEventAsRead = (event: Event) => {
        event.read = true;
        return fetch(this.getApiPath() + "/history?eventId=" + event.id, {
            headers: this.headers,
            method: 'POST'
        }).then(this.validateStatus);
    };

    public setHistoryAsRead = () => {
        return fetch(this.getApiPath() + "/history", {
            headers: this.headers,
            method: 'PATCH'
        }).then(this.validateStatus);
    };

    public countUnreadEvents = () => {
        return fetch(this.getApiPath() + "/history/unread", {
            headers: this.headers,
            method: 'GET'
        }).then(this.validateStatus);
    };

    public getCodeCallback = (type: CodeCallbackType) => {
        return fetch(this.getApiPath() + "/code?type=" + CodeCallbackType[type], {
            headers: this.headers
        })
            .then(this.validateStatus)
            .then(response => {
                return response.json()
            })
            .then(json => {
                return plainToClass(Code, json as Code);
            });
    };

    public checkCodeCallback = (type: CodeCallbackType) => {
        return fetch(this.getApiPath() + "/code?type=" + CodeCallbackType[type], {
            headers: this.headers,
            method: 'POST'
        }).then(this.validateStatus);
    };

    public updateCodeCallback = (code: Code) => {
        return fetch(this.getApiPath() + "/code", {
            body: JSON.stringify(code),
            headers: {
                ...this.headers,
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            method: 'PATCH'
        }).then(this.validateStatus);
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
        return this.futch(this.getApiPath() + "/records" + queryString, formData, onProgress)
            .then(this.validateStatus);
    };

    public getRecords = () => {
        return fetch(this.getApiPath() + "/records", {
            headers: this.headers
        })
            .then(this.validateStatus)
            .then(response => {
                return response.json()
            })
            .then(json => {
                return plainToClass(Record, json as Record[]);
            });
    };

    public restartReplication = () => {
        return fetch(this.getApiPath() + "/replication", {
            headers: this.headers,
            method: 'POST'
        }).then(this.validateStatus);
    };

    public updateRecordMeta = (meta: RecordMeta) => {
        return fetch(this.getApiPath() + "/records", {
            body: JSON.stringify(meta),
            headers: {
                ...this.headers,
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            method: 'PUT'
        }).then(this.validateStatus);
    };

    public deleteRecords = (id: string) => {
        return fetch(this.getApiPath() + "/records?id=" + id, {
            headers: this.headers,
            method: 'DELETE'
        }).then(this.validateStatus);
    };

    public forceDeleteRecords = (id: string) => {
        return fetch(this.getApiPath() + "/records?id=" + id, {
            headers: this.headers,
            method: 'PATCH'
        }).then(this.validateStatus);
    };

    public subscribeToNotifications = (notifications: INotifications) => {
        const request = this.request;
        request.url = this.getAuthApiPath() + "/notification";
        request.contentType = "application/json";
        request.transport = 'websocket';
        request.fallbackTransport = 'websocket';
        request.reconnectInterval = 1000 * 15;
        request.shared = true;
        request.maxReconnectOnClose = 5;
        request.enableXDR = true;
        request.enableProtocol = true;
        request.readResponsesHeaders = true;
        request.dropHeaders = true;
        request.withCredentials = false;
        request.attachHeadersAsQueryString = true;
        request.onOpen = notifications.onOpen;
        request.onReconnect = notifications.onReconnect;
        request.onMessage = response => notifications.onMessage(response.status!, (response.status === 200) ? response.responseBody! : response.error!);
        request.onError = () => notifications.onError;
        request.onClose = () => notifications.onClose;
        request.onReopen = (_, response: Atmosphere.Response) => notifications.onReopen(response.transport!);
        request.onClientTimeout = notifications.onClientTimeout;
        this.socket.subscribe(request);
    };

    private futch(url: string, formData: FormData, onProgress?: (event: ProgressEvent) => void) {
        return new Promise<Response>((resolve, reject) => {
            const xhr: XMLHttpRequest = new XMLHttpRequest();
            this.headers.forEach((value, key) => xhr.setRequestHeader(key, value));
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
        return this.serverUri;
    };

    private getApiPath = () => {
        return this.getBackendPath() + "/api";
    };

    private getAuthApiPath = () => {
        const pathParts = this.getBackendPath().split("//");
        pathParts[1] = `${this.username}:${this.password}@${pathParts[1]}`;
        return pathParts.join("//") + "/api";
    };
}