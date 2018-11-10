package io.github.t3r1jj.fcms.backend.model.code;

import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import org.jetbrains.annotations.NotNull;

public class AfterReplicationCallback extends Code {
    public AfterReplicationCallback(String name, String code, String exceptionHandler, String finallyHandler) {
        super(name, code, exceptionHandler, finallyHandler);
    }

    public void execute(StoredRecord[] storedRecords) {
        try {
            compile().evaluate(new Object[]{storedRecords});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public Class<?>[] getParamClasses() {
        return new Class[]{StoredRecord[].class};
    }

    @NotNull
    @Override
    public String[] getParamNames() {
        return new String[]{"storedRecords"};
    }

    public static class Builder extends Code.Builder<Builder> {
        @Override
        public AfterReplicationCallback build() {
            return new AfterReplicationCallback(name, code, exceptionHandler, finallyHandler);
        }
    }
}
