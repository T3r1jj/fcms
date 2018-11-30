import Formatter from "../../utils/Formatter";

export default class Progress {
    public done: number;
    public total: number;
    public recordName: string;
    public serviceName: string;

    public id?: string;
    public timeoutId?: number;
    public action?: string;

    public toString() {
        return `${this.action} ${this.recordName} ${this.action === "UPLOADING" ? "TO" : "FROM"} ${this.serviceName} ${Formatter.formatBytes(this.done)} / ${Formatter.formatBytes(this.total)}`
    }
}