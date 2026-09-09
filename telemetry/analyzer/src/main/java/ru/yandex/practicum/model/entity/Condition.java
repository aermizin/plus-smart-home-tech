package ru.yandex.practicum.model.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.model.type.ConditionType;
import ru.yandex.practicum.model.type.ConditionOperation;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "conditions")
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ConditionType type;

    @Enumerated(EnumType.STRING)
    private ConditionOperation operation;

    private Integer value;
}
