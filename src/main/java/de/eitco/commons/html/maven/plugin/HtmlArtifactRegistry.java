/*
 * Copyright (c) 2021 EITCO GmbH
 * All rights reserved.
 *
 * Created on 09.03.2021
 *
 */
package de.eitco.commons.html.maven.plugin;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

@Component(role = HtmlArtifactRegistry.class)
public class HtmlArtifactRegistry {

    private final Map<String, List<File>> artifactsByModule = new HashMap<>();

    public void attachArtifact(MavenProject project, File artifact) {

        get(project).add(artifact);
    }

    @NotNull
    private List<File> get(MavenProject project) {

        String key = projectKey(project);

        return artifactsByModule.computeIfAbsent(key, x -> new ArrayList<>());
    }

    public List<File> getArtifacts(MavenProject project) {

        return Collections.unmodifiableList(get(project));
    }

    private String projectKey(MavenProject project) {

        return project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion();
    }
}
