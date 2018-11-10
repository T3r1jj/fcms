package io.github.t3r1jj.fcms.backend.model.code;

import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import org.jetbrains.annotations.NotNull;

public class OnReplicationCallback extends Code {

    public OnReplicationCallback(String name, String code, String exceptionHandler, String finallyHandler) {
        super(name, code, exceptionHandler, finallyHandler);
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
        public OnReplicationCallback build() {
            return new OnReplicationCallback(name, code, exceptionHandler, finallyHandler);
        }
    }
}
