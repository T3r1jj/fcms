import PageSort from "./PageSort";

export default class Pageable {
    public sort: PageSort;
    public offset: number;
    public pageNumber: number;
    public pageSize: number;
    public paged: boolean;
    public unpaged: boolean;
}