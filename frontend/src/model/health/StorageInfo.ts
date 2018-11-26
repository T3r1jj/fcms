import {IsNumber, IsString} from "class-validator";

export default class StorageInfo {
    @IsString()
    // @ts-ignore
    public name: string;
    @IsNumber()
    // @ts-ignore
    public totalSpace: number;
    @IsNumber()
    // @ts-ignore
    public usedSpace: number;
}