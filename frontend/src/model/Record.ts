import {Type} from "class-transformer";
import IBackup from "./IBackup";
import IRecord from "./IRecord";

export default class Record implements IRecord {
    public parentIds: string[] = [];
    public id: string;
    public name: string;
    public description: string;
    public data?: Blob;
    public tag?: string;

    @Type(() => Record)
    // @ts-ignore
    public versions: IRecord[];
    @Type(() => Map)
    // @ts-ignore
    public backups: Map<string, IBackup> = new Map<string, IBackup>();
}