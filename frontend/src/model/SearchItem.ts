export default class SearchItem {
    public label: string;
    public value: string;

    constructor(label: string, id: string) {
        this.label = label;
        this.value = id;
    }
}