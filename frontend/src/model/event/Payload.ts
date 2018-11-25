import {Transform, Type} from "class-transformer";
import IRecord from "../IRecord";
import Record from "../Record";
import {ClientPayloadType, PayloadType} from "./PayloadType";
import Progress from "./Progress";

export default class Payload {
    @Type(() => Record)
    // @ts-ignore
    public record?: IRecord;
    @Type(() => Progress)
    // @ts-ignore
    public progress?: Progress;
    @Transform(value => PayloadType[value as keyof typeof PayloadType])
    // @ts-ignore
    public type: PayloadType;

    public onConsume?: (info: string, clientType?: ClientPayloadType) => void
}