import {Transform} from "class-transformer";
import {IsDate, IsString} from "class-validator";
import {EventType} from "./EventType";

// tslint:disable-next-line:no-var-requires
const moment = require('moment')

export class Event {
    @IsString()
    // @ts-ignore
    public title: string;
    @IsString()
    // @ts-ignore
    public description: string;

    @Transform(value => EventType[value as keyof typeof EventType])
    // @ts-ignore
    public type: EventType;
    @Transform(value => moment(value, "YYYY-MM-DDTHH:mm:ss.SSSZ").toDate())
    @IsDate()
    // @ts-ignore
    public time: Date;
}