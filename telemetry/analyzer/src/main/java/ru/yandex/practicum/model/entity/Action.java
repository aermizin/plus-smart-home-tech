package ru.yandex.practicum.model.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.model.type.ActionType;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "actions")
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ActionType type;

    private Integer value;
}
