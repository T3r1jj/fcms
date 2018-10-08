export interface IRecord {
    id: string;
    name: string;
    description: string;
    data: Blob;

    versions: IRecord[];
    meta: IRecord[];
    backups: IBackup[];

}