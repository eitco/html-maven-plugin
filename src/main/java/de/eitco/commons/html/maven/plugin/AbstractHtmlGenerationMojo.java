/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 12.03.2021
 *
 */
package de.eitco.commons.html.maven.plugin;

import com.google.common.base.Strings;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.asciidoctor.maven.AsciidoctorMaven;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractHtmlGenerationMojo extends AbstractMojo {

    public static final String DEFAULT_TARGET_DIRECTORY = "${project.build.directory}/html";

    public static final String STYLE_SHEET_REFERENCE_PRE = "\t\t<link rel=\"stylesheet\" href=\"";
    public static final String STYLE_SHEET_REFERENCE_POST = "\">\n";
    public static final char ORDERING_SEPARATOR = '.';
    protected static final String HTML_HEADER = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n<html>\t\n<head>\t\t\n<meta charset=\"UTF-8\">\n";
    protected static final String HTML_CLOSE_HEAD_PRE = "\t</head>\n\t<body class=\"";
    protected static final String HTML_CLOSE_HEAD_POST = "\">\n";
    protected static final String STYLE_OPEN = "\t\t<style>\n";
    protected static final String STYLE_CLOSE = "\n\t\t</style>\n";
    protected static final String HTML_FOOTER = "\n\t</body>\n</html>";
    public static final String SCRIPT_PRE = "<script src=\"";
    public static final String SCRIPT_POST = "\"></script>\n";
//    public static final String JQUERY_REFERENCE = "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js\"></script>\n";

    @Parameter(property = AsciidoctorMaven.PREFIX + "outputDirectory", defaultValue = DEFAULT_TARGET_DIRECTORY)
    protected File outputDirectory;

    @Parameter
    protected String defaultStyle = null;

    @Parameter
    protected List<String> styleSheetReferences = new ArrayList<>();

    @Parameter
    protected List<String> scriptReferences = new ArrayList<>();

    @Parameter
    protected List<String> bodyClasses = new ArrayList<>();

    @Parameter(defaultValue = "${project.build.sourceEncoding}", readonly = true)
    protected String sourceEncoding;

    @Component
    protected HtmlArtifactRegistry htmlArtifactRegistry;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    private static Integer parseNumber(String string) {

        try {

            return Integer.valueOf(string);

        } catch (NumberFormatException e) {

            return null;
        }
    }

    protected void writeHtml(OutputStream target, @NotNull String htmlBody) throws IOException {

        StringBuilder html = new StringBuilder();

        html.append(HTML_HEADER);

//        html.append(JQUERY_REFERENCE);
//
        scriptReferences.forEach(reference -> {

            html.append(SCRIPT_PRE);
            html.append(reference);
            html.append(SCRIPT_POST);
        });

        if (defaultStyle == null) {

            defaultStyle = loadDefaultStyle();
        }

        if (!Strings.isNullOrEmpty(defaultStyle)) {

            html.append(STYLE_OPEN);

            html.append(defaultStyle);

            html.append(STYLE_CLOSE);
        }

        styleSheetReferences.forEach(link -> {

            html.append(STYLE_SHEET_REFERENCE_PRE);
            html.append(link);
            html.append(STYLE_SHEET_REFERENCE_POST);
        });

        html.append(HTML_CLOSE_HEAD_PRE);

        html.append(String.join(" ", bodyClasses));

        html.append(HTML_CLOSE_HEAD_POST);

        html.append(htmlBody);

        html.append(HTML_FOOTER);

        byte[] htmlBytes = html.toString().getBytes(sourceEncoding);

        IOUtil.copy(htmlBytes, target);
    }

    private String loadDefaultStyle() throws IOException {

        try (InputStream resourceAsStream = AbstractHtmlGenerationMojo.class.getClassLoader().getResourceAsStream("default.css")) {

            if (resourceAsStream == null) {

                try (InputStream resourceAsStream2 = AbstractHtmlGenerationMojo.class.getClassLoader().getResourceAsStream("/default.css")) {

                    if (resourceAsStream2 == null) {

                        throw new IOException("default.css not found");
                    }

                    return IOUtil.toString(resourceAsStream2);
                }
            }

            return IOUtil.toString(resourceAsStream);
        }
    }

    protected void attachNewHtmlFile(String fileName, String bodyContent) throws MojoFailureException {

        File indexFile = new File(outputDirectory, fileName);

        try {

            FileUtils.forceMkdir(indexFile.getParentFile());

        } catch (IOException e) {

            throw new MojoFailureException(e.getMessage(), e);
        }

        try (FileOutputStream target = new FileOutputStream(indexFile)) {

            writeHtml(target, bodyContent);

            htmlArtifactRegistry.attachArtifact(project, indexFile);

        } catch (IOException e) {

            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    protected void forEachFile(File directory, OrderedFileHandler handler) throws MojoFailureException {

        try {

            if (!directory.exists()) {

                return;
            }

            if (directory.isFile()) {

                handler.onFile(OrderedFile.fromFile(directory));

                return;
            }

            File[] subDirectories = directory.listFiles(File::isDirectory);

            List<OrderedFile> directories = orderFiles(subDirectories);

            for (OrderedFile orderedFile : directories) {

                handler.beforeDirectory(orderedFile);

                forEachFile(orderedFile.file, handler);

                handler.afterDirectory(orderedFile);
            }

            File[] containedFiles = directory.listFiles(File::isFile);

            List<OrderedFile> files = orderFiles(containedFiles);

            for (OrderedFile file : files) {
                handler.onFile(file);
            }

        } catch (IOException e) {

            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    private List<OrderedFile> orderFiles(File[] containedFiles) {

        if (containedFiles == null) {

            return List.of();
        }

        List<OrderedFile> result = Arrays.stream(containedFiles)
                .map(OrderedFile::fromFile)
                .sorted(OrderedFile::compareTo)
                .collect(Collectors.toList());

        return result;
    }

    protected interface OrderedFileHandler {

        void beforeDirectory(OrderedFile orderedFile) throws IOException;

        void afterDirectory(OrderedFile orderedFile) throws IOException;

        void onFile(OrderedFile orderedFile) throws IOException;
    }

    protected static class OrderedFile implements Comparable<OrderedFile> {

        @NotNull
        private final String orderKey;

        @NotNull
        protected final String parsedName;

        @NotNull
        protected final File file;

        private OrderedFile(@NotNull String orderKey, @NotNull String parsedName, @NotNull File file) {
            this.orderKey = orderKey;
            this.parsedName = parsedName;
            this.file = file;
        }

        public static OrderedFile fromFile(@NotNull File file) {

            String name = file.getName();

            int pos = name.indexOf(ORDERING_SEPARATOR);

            if (pos < 0) {

                return new OrderedFile(name, name, file);
            }

            int secondPos = name.indexOf(ORDERING_SEPARATOR, pos + 1);

            if (secondPos < 0) {

                return new OrderedFile(name, name.substring(0, pos), file);
            }

            return new OrderedFile(name.substring(0, pos), name.substring(pos + 1, secondPos), file);
        }

        @Override
        public int compareTo(@NotNull AbstractHtmlGenerationMojo.OrderedFile other) {

            Integer integer1 = AbstractHtmlGenerationMojo.parseNumber(this.orderKey);

            Integer integer2 = AbstractHtmlGenerationMojo.parseNumber(other.orderKey);

            if (integer1 == null && integer2 == null) {

                return this.parsedName.compareTo(other.parsedName);
            }

            if (integer2 == null) {

                return -1;
            }

            if (integer1 == null) {

                return 1;
            }

            return integer1.compareTo(integer2);
        }
    }
}
