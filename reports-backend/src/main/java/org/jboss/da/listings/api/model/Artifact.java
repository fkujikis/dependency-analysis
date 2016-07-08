package org.jboss.da.listings.api.model;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Jozef Mrazek <jmrazek@redhat.com>
 *
 */
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true)
@ToString
@MappedSuperclass
public class Artifact extends GenericEntity {

    @Setter
    @Getter
    @NonNull
    @ManyToOne
    private GA ga;

    @Setter
    @Getter
    @NonNull
    private String version;
}
