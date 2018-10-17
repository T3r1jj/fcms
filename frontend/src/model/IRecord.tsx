export default interface IRecord {
    id: string;
    name: string;
    description: string;
    data?: Blob;
    tag?: string;

    versions: IRecord[];
    meta: IRecord[];
    backups: IBackup[];

}