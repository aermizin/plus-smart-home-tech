package ru.yandex.practicum.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sensors")
public class Sensor {

    @Id
    private String id;

    @JoinColumn(name = "hub_id")
    private String hubId;
}
