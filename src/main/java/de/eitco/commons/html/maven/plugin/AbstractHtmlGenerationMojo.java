/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 12.03.2021
 *
 */
package de.eitco.commons.html.maven.plugin;

import com.google.common.base.Strings;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.IOUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHtmlGenerationMojo extends AbstractHtmlMojo {

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

    /**
     * This parameter specifies inline css code to be added to generated html files.
     */
    @Parameter
    protected String defaultStyle = null;

    /**
     * This parameter specifies a list of style sheet references to be added to be added to generated html files.
     */
    @Parameter
    protected List<String> styleSheetReferences = new ArrayList<>();

    /**
     * This parameter specifies a list of script references to be added to generated html files.
     */
    @Parameter
    protected List<String> scriptReferences = new ArrayList<>();

    /**
     * This parameter specifies a list of classes to be added to the bodies of generated html files.
     */
    @Parameter
    protected List<String> bodyClasses = new ArrayList<>();

    protected void writeHtml(OutputStream target, @NotNull String htmlBody) throws IOException {

        StringBuilder html = new StringBuilder();

        html.append(HTML_HEADER);

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

        byte[] htmlBytes = html.toString().getBytes(getSourceEncoding());

        IOUtil.copy(htmlBytes, target);
    }

    private Charset sourcCharset = null;

    protected Charset getSourceEncoding() {

        if (sourcCharset != null) {

            return sourcCharset;
        }

        return sourcCharset = Charset.forName(sourceEncoding != null ? sourceEncoding : "UTF-8");
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

}
