import {Type} from "class-transformer";
import Event from "./Event";
import Pageable from "./Pageable";
import PageSort from "./PageSort";

export default class EventPage {
    @Type(() => Event)
    // @ts-ignore
    public content: Event[];
    public pageable: Pageable;
    public totalElements: number;
    public totalPages: number;
    public last: boolean;
    public size: number;
    public number: number;
    public sort: PageSort;
    public numberOfElements: number;
    public first: boolean;
}