import {Type} from "class-transformer";
import IBackup from "./IBackup";
import IRecord from "./IRecord";
import RecordMeta from "./RecordMeta";

export default class Record implements IRecord {
    public parentIds: string[] = [];
    public id: string;
    @Type(()=> RecordMeta)
    // @ts-ignore
    public meta: RecordMeta;
    public data?: Blob;

    @Type(() => Record)
    // @ts-ignore
    public versions: IRecord[];
    @Type(() => Map)
    // @ts-ignore
    public backups: Map<string, IBackup> = new Map<string, IBackup>();
}