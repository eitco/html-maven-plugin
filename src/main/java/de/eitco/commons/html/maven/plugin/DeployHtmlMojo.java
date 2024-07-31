/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 07.03.2021
 *
 */
package de.eitco.commons.html.maven.plugin;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.asciidoctor.maven.AsciidoctorMaven;
import org.codehaus.mojo.wagon.AbstractSingleWagonMojo;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * This goal deploys the html files to a remote repository. It does use mavens wagon api to upload the
 * generated html (and resource) files to a specified location. This is intended for quick and dirty
 * - but up to date - documentation deployment - to a nexus 'raw' repository for example.
 */
@Mojo(name = "deploy-html", defaultPhase = LifecyclePhase.DEPLOY)
public class DeployHtmlMojo extends AbstractSingleWagonMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /**
     * This parameter specifies where to put the generated html file(s).
     */
    @Parameter(property = AsciidoctorMaven.PREFIX + "outputDirectory", defaultValue = AbstractHtmlGenerationMojo.DEFAULT_TARGET_DIRECTORY)
    protected File outputDirectory;

    /**
     * This parameter specified whether to skip the deployment of the html file(s).
     */
    @Parameter(defaultValue = "false")
    protected boolean skipHtmlDeploy = false;

    @Component
    private HtmlArtifactRegistry htmlArtifactRegistry;

    @Override
    protected void execute(Wagon wagon) throws WagonException {

        if (skip || skipHtmlDeploy) {

            getLog().info("skipping html deployment");
            return;
        }

        Path buildDirectory = outputDirectory.toPath();

        Pair<MavenProject, String> rootProjectAndPath = getRootProject(project);

        List<File> artifacts = htmlArtifactRegistry.getArtifacts(project);

        for (File artifactFile : artifacts) {

            Path artifactPath = artifactFile.toPath();

            Path relativePath = buildDirectory.relativize(artifactPath);

            String fileName = getRelativePath(rootProjectAndPath, relativePath.toString());

            getLog().info("Uploading: " + artifactFile + " " + wagon.getRepository().getUrl() + "/" + fileName);

            wagon.put(artifactFile, fileName);
        }
    }

    private Pair<MavenProject, String> getRootProject(MavenProject project) {

        MavenProject result = project;

        Stack<String> relativePath = new Stack<>();

        while (result.getParent() != null) {

            if (!parentIsOnFileSystem(result)) {

                return Pair.of(result, stackToString(relativePath));
            }

            relativePath.push(result.getModel().getProjectDirectory().getName());
            result = result.getParent();
        }

        return Pair.of(result, stackToString(relativePath));
    }

    private boolean parentIsOnFileSystem(MavenProject project) {

        String relativePath = project.getModel().getParent().getRelativePath();

        if (!relativePath.matches("[./]+(pom.xml)?")) {

            return false;
        }

        if (!relativePath.endsWith("/pom.xml")) {

            relativePath = relativePath + "/pom.xml";
        }

        return new File(project.getModel().getProjectDirectory(), relativePath).exists();
    }

    private String stackToString(Stack<String> stack) {

        List<String> list = new ArrayList<>();

        while (!stack.isEmpty()) {

            list.add(stack.pop());
        }

        return String.join("/", list);
    }

    private String getRelativePath(Pair<MavenProject, String> projectAndPath, String relativePath) {

        MavenProject project = projectAndPath.getKey();

        String modulePath = projectAndPath.getValue().isEmpty() ? "" : "/" + projectAndPath.getValue();

        return project.getGroupId() + "/" + project.getArtifactId() + "/" + project.getVersion() + modulePath + "/" + relativePath;
    }
}
