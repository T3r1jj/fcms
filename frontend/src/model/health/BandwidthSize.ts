import {Transform} from "class-transformer";
import {IsDate, IsNumber} from "class-validator";

// tslint:disable-next-line:no-var-requires
const moment = require('moment');

export default class BandwidthSize {
    public static formatDuration(duration: number): string {
        return parseInt(moment.duration(duration).asDays(),10).toString() + "d" + moment.utc(duration).format("HH:mm:ss");
    }

    @IsNumber()
    // @ts-ignore
    public upload: number;
    @IsNumber()
    // @ts-ignore
    public download: number;
    @Transform(value => moment(value, "YYYY-MM-DDTHH:mm:ss.SSSZ").toDate())
    @IsDate()
    // @ts-ignore
    public start: Date;
    @Transform(value => moment(value, "YYYY-MM-DDTHH:mm:ss.SSSZ").toDate())
    @IsDate()
    // @ts-ignore
    public end: Date;
    @Transform(value => moment.duration(value).asMilliseconds())
    @IsDate()
    // @ts-ignore
    public duration: number;

    public getFormattedDuration(): string {
        return BandwidthSize.formatDuration(this.duration);
    }
}