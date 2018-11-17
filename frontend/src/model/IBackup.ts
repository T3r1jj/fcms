export default interface IBackup {
    name: string;
    path: string;
    publicPath?: string;
    id?: string;
    size: number;
}