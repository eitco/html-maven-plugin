/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 08.03.2021
 *
 */
package de.eitco.commons.html.maven.plugin;

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.xwiki.macros.MacroExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.IOUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;

@Mojo(name = "from-markdown", defaultPhase = LifecyclePhase.COMPILE)
public class MarkdownToHtmlMojo extends AbstractHtmlGenerationMojo {

    @Parameter(defaultValue = "${project.basedir}/src/main/markdown", property = "freemarker.target.directory")
    protected File markdownDirectory;

    public void convert(InputStream source, OutputStream target) throws IOException {

        MutableDataSet options = new MutableDataSet();

        //TODO: check available options
        options.set(Parser.EXTENSIONS, Arrays.asList(
                TablesExtension.create(),
                StrikethroughExtension.create(),
                MacroExtension.create()
        ));

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        String markdown = new String(IOUtil.toByteArray(source), getSourceEncoding());

        Node document = parser.parse(markdown);

        writeHtml(target, renderer.render(document));
    }


    @Override
    public void execute() throws MojoFailureException {

        try {

            traverseDirectory(markdownDirectory);

        } catch (IOException e) {

            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    private void traverseDirectory(File currentDirectory) throws IOException {

        traverseDirectory(currentDirectory, currentDirectory.toPath());
    }

    private void traverseDirectory(File currentDirectory, Path rootDirectory) throws IOException {

        File[] files = currentDirectory.listFiles();

        if (files == null) {

            return;
        }

        for (File file : files) {

            if (file.isDirectory()) {

                traverseDirectory(file, rootDirectory);
            }

            if (!file.getName().matches(".*\\.(md|markdown)\\z")) {

                continue;
            }

            String relativePath = rootDirectory.relativize(currentDirectory.toPath()).toString();

            File targetFile = new File(new File(outputDirectory, relativePath), changeExtension(file.getName(), "html"));

            FileUtils.forceMkdir(targetFile.getParentFile());

            try (
                    InputStream source = new FileInputStream(file);
                    OutputStream target = new FileOutputStream(targetFile)
            ) {

                convert(source, target);

                htmlArtifactRegistry.attachArtifact(project, targetFile);
            }
        }
    }

    @NotNull
    private String changeExtension(String name, final String newExtension) {

        int pos = name.indexOf('.');

        if (pos < 0) {

            return name + "." + newExtension;
        }

        return name.substring(0, pos) + "." + newExtension;
    }
}
