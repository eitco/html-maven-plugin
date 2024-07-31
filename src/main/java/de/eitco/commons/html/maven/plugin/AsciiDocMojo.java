/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 07.03.2021
 *
 */
package de.eitco.commons.html.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.maven.AsciidoctorMaven;
import org.asciidoctor.maven.AsciidoctorMojo;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This goal generates html files from asciidoc source files. It is built on top of the
 * {@code AsciidoctorMojo} from the <a href="https://github.com/asciidoctor/asciidoctor-maven-plugin">asciidoctor maven plugin</a>
 * and adapts it to align with the defined build lifecycle.
 */
@Mojo(name = "from-asciidoc", defaultPhase = LifecyclePhase.COMPILE)
public class AsciiDocMojo extends AsciidoctorMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /**
     * This parameter specifies where to put the generated html file(s).
     */
    @Parameter(property = AsciidoctorMaven.PREFIX + "outputDirectory", defaultValue = AbstractHtmlGenerationMojo.DEFAULT_TARGET_DIRECTORY)
    protected File outputDirectory;

    /**
     * This parameter specifies whether to preserve the directory structure in the output.
     */
    @Parameter(property = AsciidoctorMaven.PREFIX + "preserveDirectories", defaultValue = "true")
    protected boolean preserveDirectories = true;


    private File currentTargetFile;

    @Component
    private HtmlArtifactRegistry htmlArtifactRegistry;

    @Override
    public File setDestinationPaths(File sourceFile, OptionsBuilder optionsBuilder, File sourceDirectory, AsciidoctorMojo configuration) throws MojoExecutionException {

        File result = super.setDestinationPaths(sourceFile, optionsBuilder, sourceDirectory, configuration);

        String extension = FileUtils.getExtension(result.getName());

        currentTargetFile = new File(result.getParentFile(), result.getName().replaceAll(Pattern.quote(extension) + "\\z", "html"));

        return result;
    }

    @Override
    protected void convertFile(Asciidoctor asciidoctor, Map<String, Object> options, File f) {

        super.convertFile(asciidoctor, options, f);

        getLog().info("attaching artifact " + currentTargetFile);
        htmlArtifactRegistry.attachArtifact(project, currentTargetFile);
    }
}
