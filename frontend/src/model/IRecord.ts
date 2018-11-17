import IBackup from "./IBackup";

export default interface IRecord {
    parentIds: string[];
    id: string;
    name: string;
    description: string;
    data?: Blob;
    tag?: string;

    versions: IRecord[];
    backups: Map<string, IBackup>;
}