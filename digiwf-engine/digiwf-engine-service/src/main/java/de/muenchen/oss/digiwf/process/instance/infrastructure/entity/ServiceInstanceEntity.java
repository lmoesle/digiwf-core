/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik der Landeshauptstadt München, 2020
 */

package de.muenchen.oss.digiwf.process.instance.infrastructure.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Entity representation of a form.
 *
 * @author externer.dl.horn
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ServiceInstance")
@Table(name = "DWF_PROCESS_INSTANCE_INFO")
public class ServiceInstanceEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id_", unique = true, nullable = false, length = 36)
    private String id;

    @Column(name = "processinstanceid_")
    private String instanceId;

    @Column(name = "processname_", nullable = false)
    private String definitionName;

    @Column(name = "processdefinitionkey_", nullable = false)
    private String definitionKey;

    @Column(name = "description_")
    private String description;

    @Column(name = "status_")
    private String status;

    @Column(name = "statuskey_")
    private String statusKey;

    @Column(name = "starttime_")
    private Date startTime;

    @Column(name = "endtime_")
    private Date endTime;

    @Column(name = "removaltime_")
    private Date removalTime;
}
