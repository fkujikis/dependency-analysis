package org.jboss.da.reports.api;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@RequiredArgsConstructor
@NoArgsConstructor
public class SCMLocator {

    @Getter
    @NonNull
    private String scmUrl;

    @Getter
    @NonNull
    private String revision;

    @Getter
    @NonNull
    private String pomPath;

    @Getter
    @NonNull
    private List<String> repositories = Collections.emptyList();

    public SCMLocator(String scmUrl, String revision, String pomPath, List<String> repositories) {
        this.scmUrl = Objects.requireNonNull(scmUrl);
        this.revision = Objects.requireNonNull(revision);
        this.pomPath = Objects.requireNonNull(pomPath);
        if (repositories != null) {
            this.repositories = repositories;
        }
    }

}
