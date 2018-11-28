import IBackup from "./IBackup";
import RecordMeta from "./RecordMeta";

export default interface IRecord {
    id: string;
    meta: RecordMeta;
    data?: Blob;

    versions: IRecord[];
    backups: Map<string, IBackup>;
}