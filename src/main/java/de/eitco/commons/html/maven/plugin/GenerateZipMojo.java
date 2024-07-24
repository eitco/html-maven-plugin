/*
 * Copyright (c) 2023 EITCO GmbH
 * All rights reserved.
 *
 * Created on 30.03.2023
 *
 */
package de.eitco.commons.html.maven.plugin;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.asciidoctor.maven.AsciidoctorMaven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Mojo(name = "zip-html", defaultPhase = LifecyclePhase.PACKAGE)
public class GenerateZipMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/${project.artifactId}-${project.version}.zip")
    private File zipName;

    @Parameter(property = AsciidoctorMaven.PREFIX + "outputDirectory", defaultValue = AbstractHtmlGenerationMojo.DEFAULT_TARGET_DIRECTORY)
    protected File outputDirectory;

    @Component
    private HtmlArtifactRegistry htmlArtifactRegistry;

    @Override
    public void execute() throws MojoExecutionException {

        List<File> artifacts = htmlArtifactRegistry.getArtifacts(project);

        Path buildDirectory = outputDirectory.toPath();

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipName))) {

            for (File file : artifacts) {

                Path artifactPath = file.toPath();

                Path relativePath = buildDirectory.relativize(artifactPath);

                ZipEntry zipEntry = new ZipEntry(relativePath.toString());

                try (FileInputStream source = new FileInputStream(file)) {

                    zipOutputStream.putNextEntry(zipEntry);

                    IOUtils.copy(source, zipOutputStream);

                } catch (IOException e) {

                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }

        } catch (IOException e) {

            throw new MojoExecutionException(e.getMessage(), e);
        }

        project.getArtifact().setFile(zipName);
    }
}
