import IApiKey from "./IApiKey";

export default interface IService {
    primary: boolean;
    name: string;
    apiKeys: IApiKey[];
    enabled: boolean;
}