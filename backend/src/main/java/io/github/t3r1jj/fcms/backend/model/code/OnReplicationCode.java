package io.github.t3r1jj.fcms.backend.model.code;

import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class OnReplicationCode extends Code {

    public OnReplicationCode(String code, String exceptionHandler, String finallyHandler) {
        super(code, exceptionHandler, finallyHandler);
    }

    public void execute(StoredRecord storedRecord) {
        try {
            compile().evaluate(new StoredRecord[]{storedRecord});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public Class<?>[] getParamClasses() {
        return new Class[]{StoredRecord.class};
    }

    @NotNull
    @Override
    public String[] getParamNames() {
        return new String[]{"storedRecord"};
    }

    public static class Builder extends Code.Builder<Builder> {
        @Override
        public OnReplicationCode build() {
            return new OnReplicationCode(code, exceptionHandler, finallyHandler);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface Callback {
    }
}
