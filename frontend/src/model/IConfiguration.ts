import IService from "./IService";

export default interface IConfiguration {
    services: IService[];
    primaryBackupLimit: number;
    secondaryBackupLimit: number;
}