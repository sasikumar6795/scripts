package com.automationGenerator.services;

import com.automationGenerator.models.GitRequest;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;


@Service
public class CloneRepoService {

    public ResponseEntity<String> cloneRepo(GitRequest gitRequest) {
        String repositoryUrl = gitRequest.getRepositoryUrl();
        String destinationPath = gitRequest.getDestinationPath();

        Git git;
        try {
            FileUtils.deleteDirectory(new File("src/main/resources/cloneRepos"));


            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(repositoryUrl)
                    .setDirectory(new File(destinationPath));

            git = cloneCommand.call();
            git.close();
            return ResponseEntity.ok("Repo clone successfully");
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
