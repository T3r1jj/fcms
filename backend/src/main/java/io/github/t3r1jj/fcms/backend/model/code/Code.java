package io.github.t3r1jj.fcms.backend.model.code;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ScriptEvaluator;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import static one.util.streamex.EntryStream.zip;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OnReplicationCode.class, name = "OnReplicationCode"),
        @JsonSubTypes.Type(value = AfterReplicationCode.class, name = "AfterReplicationCode")
})
abstract public class Code {
    private String name;
    private String code;
    private String exceptionHandler;
    private String finallyHandler;

    @Id
    private final String id = getClass().getSimpleName();

    public Code(String name, String code, String exceptionHandler, String finallyHandler) {
        this.name = emptyIfNull(name);
        this.code = emptyIfNull(code);
        this.exceptionHandler = emptyIfNull(exceptionHandler);
        this.finallyHandler = emptyIfNull(finallyHandler);
    }

    private String emptyIfNull(String param) {
        return (param == null) ? "" : param;
    }

    private transient String tryHeader = "try {\n";
    private transient String catchHeader = "\n} catch (Exception e) {\n";
    private transient String finallyHeader = "} finally {\n";
    private transient String finallyFooter = "}\n";
    private transient String methodFooter = "}\n";

    public final ScriptEvaluator compile() throws CompileException {
        ScriptEvaluator scriptEvaluator = new ScriptEvaluator();
        scriptEvaluator.setParameters(getParamNames(), getParamClasses());
        String javaCode = tryHeader +
                code +
                catchHeader +
                exceptionHandler +
                finallyHeader +
                finallyHandler +
                finallyFooter;
        scriptEvaluator.cook(javaCode);
        return scriptEvaluator;
    }

    @NotNull
    public Class<?>[] getParamClasses() {
        return new Class<?>[]{StoredRecord.class};
    }

    @NotNull
    public String[] getParamNames() {
        return new String[]{"storedRecord"};
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

    public final String getMethodHeader() {
        String params = zip(getParamClasses(), getParamNames())
                .map(it -> it.getKey().getSimpleName() + " " + it.getValue())
                .joining(",");
        return " public void execute(" + params + ") {\n";
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

    public String getId() {
        return id;
    }

    @JsonIgnore
    @Transient
    public boolean isEmpty() {
        return (this.code == null || this.code.isEmpty()) &&
                (this.exceptionHandler == null || this.exceptionHandler.isEmpty()) &&
                (this.finallyHandler == null || this.finallyHandler.isEmpty());
    }

    public static abstract class Builder<B extends Builder> {
        String name;
        String code;
        String exceptionHandler;
        String finallyHandler;

        public B setName(String name) {
            this.name = name;
            return self();
        }

        public B setCode(String code) {
            this.code = code;
            return self();
        }

        public B setExceptionHandler(String exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
            return self();
        }

        public B setFinallyHandler(String finallyHandler) {
            this.finallyHandler = finallyHandler;
            return self();
        }

        @SuppressWarnings("unchecked")
        final B self() {
            return (B) this;
        }

        abstract public Code build();
    }

    public enum Type {
        OnReplicationCode, AfterReplicationCode;

        public String getId() {
            return createBuilder().build().getId();
        }

        public Builder createBuilder() {
            if (this == Type.OnReplicationCode) {
                return new OnReplicationCode.Builder();
            }
            return new AfterReplicationCode.Builder();
        }
    }
}
