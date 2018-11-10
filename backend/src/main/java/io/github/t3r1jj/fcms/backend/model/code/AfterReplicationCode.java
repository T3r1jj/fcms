package io.github.t3r1jj.fcms.backend.model.code;

import io.github.t3r1jj.fcms.backend.service.RecordService;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class AfterReplicationCode extends Code {
    public AfterReplicationCode(String name, String code, String exceptionHandler, String finallyHandler) {
        super(name, code, exceptionHandler, finallyHandler);
    }

    public void execute(RecordService recordService) {
        try {
            compile().evaluate(new Object[]{recordService});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public Class<?>[] getParamClasses() {
        return new Class[]{RecordService.class};
    }

    @NotNull
    @Override
    public String[] getParamNames() {
        return new String[]{"recordService"};
    }

    public static class Builder extends Code.Builder<Builder> {
        @Override
        public AfterReplicationCode build() {
            return new AfterReplicationCode(name, code, exceptionHandler, finallyHandler);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface Callback {
    }
}
