package io.github.t3r1jj.fcms.backend.aspect;

import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import io.github.t3r1jj.fcms.backend.model.code.AfterReplicationCode;
import io.github.t3r1jj.fcms.backend.model.code.Code;
import io.github.t3r1jj.fcms.backend.model.code.OnReplicationCode;
import io.github.t3r1jj.fcms.backend.repository.CodeRepository;
import io.github.t3r1jj.fcms.backend.service.RecordService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ReplicationAspect {

    private final CodeRepository codeRepository;
    private final RecordService recordService;

    @Autowired
    public ReplicationAspect(CodeRepository codeRepository, RecordService recordService) {
        this.codeRepository = codeRepository;
        this.recordService = recordService;
    }

    @Around(value = "@annotation(io.github.t3r1jj.fcms.backend.model.code.OnReplicationCode.Callback) && args(recordToReplicate, primary)",
            argNames = "proceedingJoinPoint,recordToReplicate,primary")
    Object onReplication(ProceedingJoinPoint proceedingJoinPoint, StoredRecord recordToReplicate, boolean primary) throws Throwable {
        Object proceed = proceedingJoinPoint.proceed();
        codeRepository.findById(Code.Type.OnReplicationCode.getId())
                .map(c -> (OnReplicationCode) c)
                .ifPresent(c -> c.execute(recordToReplicate));
        return proceed;
    }

    @After("@annotation(io.github.t3r1jj.fcms.backend.model.code.AfterReplicationCode.Callback)")
    void afterReplication() {
        codeRepository.findById(Code.Type.OnReplicationCode.getId())
                .map(c -> (AfterReplicationCode) c)
                .ifPresent(c -> c.execute(recordService));
    }

}
