import IBackup from "./IBackup";

export default class Backup implements IBackup {
    public name: string;
    public path: string;
    public publicPath?: string;
    public id?: string;
    public size: number;
}