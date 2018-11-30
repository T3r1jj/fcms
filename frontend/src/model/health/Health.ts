import {Type} from "class-transformer";
import {IsString} from "class-validator";
import BandwidthSize from "./BandwidthSize";
import StorageInfo from "./StorageInfo";

export default class Health {
    @IsString()
    // @ts-ignore
    public dbSize: string;
    @IsString()
    // @ts-ignore
    public dbLimit: string;
    @Type(() => BandwidthSize)
    // @ts-ignore
    public bandwidth: BandwidthSize[];
    @Type(() => StorageInfo)
    // @ts-ignore
    public storageQuotas: StorageInfo[]
}


