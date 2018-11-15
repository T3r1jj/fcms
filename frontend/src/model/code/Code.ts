import ICode from "./ICode";

export default class Code implements ICode {
    public type: string;
    public name: string;
    public code: string;
    public methodHeader: string;
    public exceptionHandler: string;
    public finallyHandler: string;
    public tryHeader: string;
    public catchHeader: string;
    public finallyHeader: string;
    public finallyFooter: string;
    public methodFooter: string;
    public error?: string;
}