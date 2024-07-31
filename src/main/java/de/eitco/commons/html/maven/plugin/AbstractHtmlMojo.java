package de.eitco.commons.html.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.asciidoctor.maven.AsciidoctorMaven;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractHtmlMojo extends AbstractMojo {
    /**
     * This parameter specifies where to put the generated html file(s).
     */
    @Parameter(property = AsciidoctorMaven.PREFIX + "outputDirectory", defaultValue = AbstractHtmlGenerationMojo.DEFAULT_TARGET_DIRECTORY)
    protected File outputDirectory;
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

    protected void forEachFile(File directory, AbstractHtmlGenerationMojo.OrderedFileHandler handler) throws MojoFailureException {

        try {

            if (!directory.exists()) {

                return;
            }

            if (directory.isFile()) {

                handler.onFile(AbstractHtmlMojo.OrderedFile.fromFile(directory));

                return;
            }

            File[] subDirectories = directory.listFiles(File::isDirectory);

            List<AbstractHtmlGenerationMojo.OrderedFile> directories = orderFiles(subDirectories);

            for (AbstractHtmlGenerationMojo.OrderedFile orderedFile : directories) {

                handler.beforeDirectory(orderedFile);

                forEachFile(orderedFile.file, handler);

                handler.afterDirectory(orderedFile);
            }

            File[] containedFiles = directory.listFiles(File::isFile);

            List<AbstractHtmlGenerationMojo.OrderedFile> files = orderFiles(containedFiles);

            for (AbstractHtmlGenerationMojo.OrderedFile file : files) {
                handler.onFile(file);
            }

        } catch (IOException e) {

            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    protected interface OrderedFileHandler {

        void beforeDirectory(OrderedFile orderedFile) throws IOException;

        void afterDirectory(OrderedFile orderedFile) throws IOException;

        void onFile(OrderedFile orderedFile) throws IOException;
    }

    protected static class OrderedFile implements Comparable<AbstractHtmlGenerationMojo.OrderedFile> {

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

            int pos = name.indexOf(AbstractHtmlGenerationMojo.ORDERING_SEPARATOR);

            if (pos < 0) {

                return new OrderedFile(name, name, file);
            }

            int secondPos = name.indexOf(AbstractHtmlGenerationMojo.ORDERING_SEPARATOR, pos + 1);

            if (secondPos < 0) {

                return new OrderedFile(name, name.substring(0, pos), file);
            }

            return new OrderedFile(name.substring(0, pos), name.substring(pos + 1, secondPos), file);
        }

        @Override
        public int compareTo(@NotNull AbstractHtmlMojo.OrderedFile other) {

            Integer integer1 = parseNumber(this.orderKey);

            Integer integer2 = parseNumber(other.orderKey);

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



}
