package com.test.restapi.entity.jpa.security;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@MappedSuperclass
@Converter( name = "uuid", converterClass = UUIDConverter.class )
@EqualsAndHashCode( of = "id" )
// @UuidGenerator(name = "uuid") - lead to NPE from time to time
public abstract class AbstractEntity
{
    @Id
    // @GeneratedValue(generator = "uuid")
    @Column( name = "id", updatable = false, nullable = false )
    @Convert( "uuid" )
    private UUID id;

    @PrePersist
    public void generateId()
    {
        if ( this.id == null )
        {
            this.id = UUID.randomUUID();
        }
    }
}
