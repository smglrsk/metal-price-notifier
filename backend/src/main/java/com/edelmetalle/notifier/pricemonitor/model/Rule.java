package com.edelmetalle.notifier.pricemonitor.model;

import com.edelmetalle.notifier.pricemonitor.enums.RuleOperator;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleOperator operator;

    @Column(nullable = false)
    private String operand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    @JsonBackReference
    private NotificationTemplate template;
}