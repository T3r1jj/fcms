package io.github.t3r1jj.fcms.backend.model;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ScriptEvaluator;

public class Code implements CodeExecutor {
    private String name;
    private String code;
    private String exceptionHandler;
    private String finallyHandler;
    private int order;

    public Code(String name, String code, String exceptionHandler, String finallyHandler, int order) {
        this.name = emptyIfNull(name);
        this.code = code;
        this.exceptionHandler = emptyIfNull(exceptionHandler);
        this.finallyHandler = emptyIfNull(finallyHandler);
        this.order = order;
    }

    private String emptyIfNull(String param) {
        return (param == null) ? "" : param;
    }

    private transient String methodHeader = " public void execute(StoredRecord storedRecord) {\n";
    private transient String tryHeader = "try {\n";
    private transient String catchHeader = "\n} catch (Exception e) {\n";
    private transient String finallyHeader = "} finally {\n";
    private transient String finallyFooter = "}\n";
    private transient String methodFooter = "}\n";

    private ScriptEvaluator load() throws CompileException {
        ScriptEvaluator scriptEvaluator = new ScriptEvaluator();
        scriptEvaluator.setParameters(new String[]{"storedRecord"}, new Class<?>[]{StoredRecord.class});
        String javaCode = tryHeader +
                code +
                catchHeader +
                exceptionHandler +
                finallyHeader +
                finallyHandler +
                finallyFooter;
        System.out.println(javaCode);
        scriptEvaluator.cook(javaCode);
        return scriptEvaluator;
    }

    @Override
    public void execute(StoredRecord storedRecord) {
        try {
            load().evaluate(new StoredRecord[]{storedRecord});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getExceptionHandler() {
        return exceptionHandler;
    }

    public String getFinallyHandler() {
        return finallyHandler;
    }

    public int getOrder() {
        return order;
    }

    public String getMethodHeader() {
        return methodHeader;
    }

    public String getTryHeader() {
        return tryHeader;
    }

    public String getCatchHeader() {
        return catchHeader;
    }

    public String getFinallyHeader() {
        return finallyHeader;
    }

    public String getFinallyFooter() {
        return finallyFooter;
    }

    public String getMethodFooter() {
        return methodFooter;
    }
}
