package com.edelmetalle.notifier.pricemonitor.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "notification_templates",
        indexes = {
                @Index(name = "idx_title", columnList = "title")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @JsonManagedReference
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Rule> rules = new HashSet<>();

    @Builder.Default
    @JsonManagedReference
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Recipient> recipients = new HashSet<>();

    public void addRule(Rule rule) {
        if (this.rules == null) {
            this.rules = new HashSet<>();
        }
        this.rules.add(rule);
        rule.setTemplate(this);
    }

    public void addRecipient(Recipient recipient) {
        if (this.recipients == null) {
            this.recipients = new HashSet<>();
        }
        this.recipients.add(recipient);
        recipient.setTemplate(this);
    }

}