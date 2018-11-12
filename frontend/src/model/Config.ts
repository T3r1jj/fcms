import IConfiguration from "./IConfiguration";

export class Config {
    private getBackendPath = () => {
        return process.env.REACT_APP_BACKEND_PATH!;
    }

    public getConfiguration = () => {
        return fetch(this.getBackendPath + "/api/configuration")
            .then(response => {
                if (!response.ok) {
                    throw new Error(response.statusText)
                }
                return response.json() as Promise<IConfiguration>
            })
    }

    public updateConfiguration = (configuration: IConfiguration) => {
        return fetch(this.getBackendPath() + '/api/configuration', {
            body: JSON.stringify(configuration),
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            method: 'POST'
        })
    }
}