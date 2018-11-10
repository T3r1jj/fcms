package io.github.t3r1jj.fcms.backend.controller;

import io.github.t3r1jj.fcms.backend.controller.exception.ResourceNotFoundException;
import io.github.t3r1jj.fcms.backend.controller.exception.UnprocessableException;
import io.github.t3r1jj.fcms.backend.model.code.Code;
import io.github.t3r1jj.fcms.backend.repository.CodeRepository;
import org.codehaus.commons.compiler.CompileException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code")
public class CodeController {
    private final CodeRepository codeRepository;

    @Autowired
    public CodeController(CodeRepository codeRepository) {
        this.codeRepository = codeRepository;
    }

    @GetMapping
    public void check(Code.Type type) {
        findCodeAndTryCompiling(type.getId());
    }

    @PostMapping
    public void update(@RequestBody Code code) {
        codeRepository.save(code);
    }

    @DeleteMapping
    public void delete(Code.Type type) {
        codeRepository.deleteById(type.getId());
    }

    private void findCodeAndTryCompiling(String id) {
        Code code = codeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("On replication code not found"));
        tryCompiling(code);
    }

    private void tryCompiling(Code code) {
        try {
            code.compile();
        } catch (CompileException e) {
            throw new UnprocessableException(e);
        }
    }
}
