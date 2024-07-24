/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 12.03.2021
 *
 */
package de.eitco.commons.html.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "web-resources", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class WebResourcesMojo extends AbstractHtmlGenerationMojo {

    @Parameter(defaultValue = "${project.basedir}/src/main/web-resources")
    protected File resourceDirectory;

    @Parameter
    protected List<String> includes = new ArrayList<>();

    @Parameter
    protected List<String> excludes = new ArrayList<>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        forEachFile(resourceDirectory, new OrderedFileHandler() {
            @Override
            public void beforeDirectory(OrderedFile orderedFile) throws IOException {

                FileUtils.forceMkdir(orderedFile.file);
            }

            @Override
            public void afterDirectory(OrderedFile orderedFile) {

            }

            @Override
            public void onFile(OrderedFile orderedFile) throws IOException {

                if (!isSelected(orderedFile.file)) {

                    return;
                }

                File targetFile = outputDirectory.toPath().resolve(resourceDirectory.toPath().relativize(orderedFile.file.toPath())).toFile();
                FileUtils.copyFile(orderedFile.file, targetFile);

                htmlArtifactRegistry.attachArtifact(project, targetFile);
            }
        });
    }

    private boolean isSelected(File file) {

        return isIncluded(file) && !isExcluded(file);
    }

    private boolean isIncluded(File file) {

        if (includes.isEmpty()) {

            return true;
        }

        for (String include : includes) {

            if (file.getPath().matches(include)) {

                return true;
            }
        }

        return false;
    }

    private boolean isExcluded(File file) {

        if (excludes.isEmpty()) {

            return false;
        }

        for (String exclude : excludes) {

            if (file.getPath().matches(exclude)) {

                return true;
            }
        }

        return false;
    }
}
