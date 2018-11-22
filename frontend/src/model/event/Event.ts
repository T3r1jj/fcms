import {Transform} from "class-transformer";
import {IsBoolean, IsDate, IsString} from "class-validator";
import {EventType} from "./EventType";

// tslint:disable-next-line:no-var-requires
const moment = require('moment')

export default class Event {
    @IsString()
    // @ts-ignore
    public id: string;
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
    @IsBoolean()
    // @ts-ignore
    public read: boolean;

    public getKeys() {
        return ["id", "read", "title", "description", "type", "time"];
    }
}

