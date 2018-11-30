export default class SearchItem {
    public label: string;
    public value: string[];

    constructor(label: string, ids: string | string[]) {
        this.label = label;
        if (typeof ids === "string") {
            this.value = [ids];
        } else {
            this.value = ids;
        }
    }
}