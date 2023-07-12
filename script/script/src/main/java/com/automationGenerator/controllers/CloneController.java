package com.automationGenerator.controllers;

import com.automationGenerator.models.GitRequest;
import com.automationGenerator.services.CloneRepoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

@RestController
@Slf4j
@RequestMapping("api/v1/cloneRepos")
@RequiredArgsConstructor
public class CloneController {

    private final CloneRepoService cloneRepoService;


    @PostMapping
    public ResponseEntity<String> cloneRepo(@RequestBody GitRequest gitRequest) {
        boolean clonedRepos = cloneRepoService.cloneRepos(gitRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }




}
