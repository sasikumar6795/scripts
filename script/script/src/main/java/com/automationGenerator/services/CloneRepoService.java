package com.automationGenerator.services;

import com.automationGenerator.models.GitRequest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.*;
import io.restassured.matcher.RestAssuredMatchers.*;


@Service
@Slf4j
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

    public boolean cloneRepos(GitRequest gitRequest) {
        boolean isCloneExtracted = false;
        String repositoryUrl = gitRequest.getRepositoryUrl();
        String destinationDirectory = gitRequest.getDestinationPath();

        try {
            FileUtils.deleteDirectory(new File("src/main/resources/cloneRepos"));
            cloneRepository(repositoryUrl, destinationDirectory);
            log.info("Repo clone successfully");
            List<String> apiPaths = extractApiPaths(destinationDirectory);
            System.out.println("Extracted API paths:");
            for (String apiPath : apiPaths) {
                System.out.println(apiPath);
                verifyApiResponse(apiPath);
            }
            isCloneExtracted = true;
            return isCloneExtracted;
        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to clone repository: " + e.getMessage());
        }
        return isCloneExtracted;
    }

    private static void cloneRepository(String repositoryUrl, String destinationDirectory)
            throws IOException, InterruptedException {
        String[] command = {"git", "clone", repositoryUrl, destinationDirectory};
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File(System.getProperty("user.dir")));

        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Failed to clone repository. Exit code: " + exitCode);
        }
    }

    private static List<String> extractApiPaths(String projectDirectory) throws IOException {
        List<String> apiPaths = new ArrayList<>();
        File projectDir = new File(projectDirectory);

        if (projectDir.exists() && projectDir.isDirectory()) {
            searchJavaFiles(projectDir, apiPaths);
        }

        return apiPaths;
    }

    private static void searchJavaFiles(File directory, List<String> apiPaths) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".java")) {
                    extractApiPathsFromFile(file, apiPaths);
                } else if (file.isDirectory()) {
                    searchJavaFiles(file, apiPaths);
                }
            }
        }
    }

    private static void extractApiPathsFromFile(File javaFile, List<String> apiPaths) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(javaFile));
        String line;

        Pattern mappingPattern = Pattern.compile("@(GetMapping|PostMapping|PutMapping|DeleteMapping)\\(\"(.*?)\"\\)");
        //Pattern mappingPattern = Pattern.compile("@RequestMapping\\(value\\s*=\\s*\"(.*?)\",\\s*method\\s*=\\s*RequestMethod\\.(GET|POST|PUT|DELETE)\\)");

        while ((line = reader.readLine()) != null) {
            Matcher mappingMatcher = mappingPattern.matcher(line);
            if (mappingMatcher.find()) {
                String methodPath = mappingMatcher.group(2);
                if (!methodPath.isEmpty()) {
                    apiPaths.add(methodPath);
                }
            }
        }

        reader.close();
    }

    private static void verifyApiResponse(String apiPath) {
        Response response = get("http://localhost:8080" + apiPath);

        System.out.println("API Path: " + apiPath);
        System.out.println("Response Status Code: " + response.getStatusCode());

        response.then().assertThat().statusCode(200); // Verify the expected status code

        // Extract values from the JSON response and perform assertions
        int id = response.path("id");
        String name = response.path("name");

        System.out.println("ID: " + id);
        System.out.println("Name: " + name);

        // Perform assertions on the extracted values
        assert id > 0 : "Invalid ID";
        assert name.equals("John Doe") : "Invalid name";

        System.out.println("API response validation completed");
        System.out.println("----------------------------------------");
    }
}
