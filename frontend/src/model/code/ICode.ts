export default interface ICode {
    type: string;
    name: string;
    code: string;
    methodHeader: string;
    exceptionHandler: string;
    finallyHandler: string;
    tryHeader: string;
    catchHeader: string;
    finallyHeader: string;
    finallyFooter: string;
    methodFooter: string;
}