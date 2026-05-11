package com.edelmetalle.notifier.pricemonitor.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recipients")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Recipient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    @JsonBackReference
    private NotificationTemplate template;
}