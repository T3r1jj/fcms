package io.github.t3r1jj.fcms.backend.model;

public class CodeBuilder {
    private String name;
    private String code;
    private String exceptionHandler;
    private String finallyHandler;
    private int order;

    public CodeBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public CodeBuilder setCode(String code) {
        this.code = code;
        return this;
    }

    public CodeBuilder setExceptionHandler(String exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public CodeBuilder setFinallyHandler(String finallyHandler) {
        this.finallyHandler = finallyHandler;
        return this;
    }

    public CodeBuilder setOrder(int order) {
        this.order = order;
        return this;
    }

    public Code createCode() {
        return new Code(name, code, exceptionHandler, finallyHandler, order);
    }
}