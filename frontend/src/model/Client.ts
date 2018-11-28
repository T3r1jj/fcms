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
    private username = "admin";
    private password = "admin";

    constructor() {
        this.headers.set('Authorization', 'Basic ' + new Buffer(this.username + ":" + this.password).toString('base64'));
    }

    public getConfiguration = () => {

        return fetch(this.getBackendPath() + "/api/configuration", {
            headers: this.headers
        })
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
                ...this.headers,
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            method: 'POST'
        });
    };

    public getHealth = () => {
        return fetch(this.getBackendPath() + "/api/health", {
            headers: this.headers
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(response.statusText)
                }
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

        return fetch(this.getBackendPath() + "/api/history" + queryString, {
            headers: this.headers
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(response.statusText)
                }
                return response.json()
            })
            .then(json => plainToClass(EventPage, json as EventPage));
    };

    public getHistory = () => {
        return fetch(this.getBackendPath() + "/api/history", {
            headers: this.headers
        })
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

    public deleteHistory = () => {
        return fetch(this.getBackendPath() + "/api/history", {
            headers: this.headers,
            method: 'DELETE'
        });
    };

    public setEventAsRead = (event: Event) => {
        event.read = true;
        return fetch(this.getBackendPath() + "/api/history?eventId=" + event.id, {
            headers: this.headers,
            method: 'POST'
        });
    };

    public setHistoryAsRead = () => {
        return fetch(this.getBackendPath() + "/api/history", {
            headers: this.headers,
            method: 'PATCH'
        });
    };

    public countUnreadEvents = () => {
        return fetch(this.getBackendPath() + "/api/history/unread", {
            headers: this.headers,
            method: 'GET'
        });
    };

    public getCodeCallback = (type: CodeCallbackType) => {
        return fetch(this.getBackendPath() + "/api/code?type=" + CodeCallbackType[type], {
            headers: this.headers
        })
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
        return fetch(this.getBackendPath() + "/api/code?type=" + CodeCallbackType[type], {
            headers: this.headers,
            method: 'POST'
        })
    };

    public updateCodeCallback = (code: Code) => {
        return fetch(this.getBackendPath() + "/api/code", {
            body: JSON.stringify(code),
            headers: {
                ...this.headers,
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
        return fetch(this.getBackendPath() + "/api/records", {
            headers: this.headers
        })
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

    public restartReplication = () => {
        return fetch(this.getBackendPath() + "/api/replication", {
            headers: this.headers,
            method: 'POST'
        });
    };

    public updateRecordMeta = (meta: RecordMeta) => {
        return fetch(this.getBackendPath() + "/api/records", {
            body: JSON.stringify(meta),
            headers: {
                ...this.headers,
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            method: 'PUT'
        });
    };

    public deleteRecords = (id: string) => {
        return fetch(this.getBackendPath() + "/api/records?id=" + id, {
            headers: this.headers,
            method: 'DELETE'
        });
    };

    public forceDeleteRecords = (id: string) => {
        return fetch(this.getBackendPath() + "/api/records?id=" + id, {
            headers: this.headers,
            method: 'PATCH'
        });
    };

    // Encountered problems:
    // 1. Have to use 2 different urls for web socket (auth through url) and http (auth through header) and successfully deliver header to the backend.
    // 2. The framework doesn't seem to support two different urls - wrong ws auth causes exception, switching url on transportFailure creates duplicate connection (messages x2)
    // 3. Different param combinations for header filters cause some callbacks to not fire. attachHeadersAsQueryString=false and readResponsesHeaders=false are required for long-pooling with basic auth, but then onOpen does not get called (use streaming which seems to work fine
    // 4. attachHeadersAsQueryString=true is required for websocket, otherwise onOpen is not called
    // Workaround: Initially use web socket with below parameters, if it fails on a) invalid auth - catch exception and resubscribe with streaming and long-pooling fallback; on b) standard error - resubscribe likewise
    // TODO: refactor
    public subscribeToNotifications = (notifications: INotifications) => {
        window.console.log("IS DOUBLE?")
        const socket: any = Atmosphere;
        const request: Atmosphere.Request = new (Atmosphere as any).AtmosphereRequest();
        request.url = `ws://localhost:8080/api/notification`
        request.contentType = "application/json";
        request.transport = 'websocket';
        request.fallbackTransport = request.transport;
        request.reconnectInterval = 1000 * 1;
        request.shared = false;
        request.maxReconnectOnClose = 5;
        const headers: any = {};
        this.headers.forEach((value, key) => headers[key] = value);
        request.logLevel = 'debug';
        request.headers = {Authorization: headers.authorization};
        request.enableXDR = true;
        request.enableProtocol = true;
        request.readResponsesHeaders = false;
        request.dropHeaders = true;
        request.withCredentials = false;
        request.attachHeadersAsQueryString = true;
        request.onOpen = notifications.onOpen;
        request.onReconnect = notifications.onReconnect;
        request.onMessage = response => notifications.onMessage(response.status!, (response.status === 200) ? response.responseBody! : response.error!);
        request.onError = () => {
            if (request.transport === 'websocket') {
                request.transport = 'streaming';
                request.fallbackTransport = 'long-polling';
                request.url = this.getBackendPath() + '/api/notification';
                request.enableXDR = true;
                request.enableProtocol = true;
                request.readResponsesHeaders = false;
                request.dropHeaders = false;
                request.withCredentials = false;
                request.attachHeadersAsQueryString = false;
                socket.subscribe(request);
            } else {
                notifications.onError();
            }
        };
        request.onClose = () => notifications.onClose;
        request.onReopen = (_, response: Atmosphere.Response) => notifications.onReopen(response.transport!);
        request.onClientTimeout = notifications.onClientTimeout;
        try {
            socket.subscribe(request);
        } catch (e) {
            request.transport = 'streaming';
            request.fallbackTransport = 'long-polling';
            request.url = this.getBackendPath() + '/api/notification';
            request.enableXDR = true;
            request.enableProtocol = true;
            request.readResponsesHeaders = false;
            request.dropHeaders = false;
            request.withCredentials = false;
            request.attachHeadersAsQueryString = false;
            socket.subscribe(request);
        }
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
        return process.env.REACT_APP_BACKEND_PATH!;
    };
}