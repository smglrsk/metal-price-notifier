package com.edelmetalle.notifier.pricemonitor.repository;

import com.edelmetalle.notifier.pricemonitor.model.NotificationTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Page<NotificationTemplate> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @EntityGraph(attributePaths = {"rules", "recipients"})
    @Query("SELECT DISTINCT t FROM NotificationTemplate t")
    List<NotificationTemplate> findAllWithRulesAndRecipients();

    @EntityGraph(attributePaths = {"rules", "recipients"})
    @Query("SELECT DISTINCT t FROM NotificationTemplate t WHERE t.id IN :ids")
    List<NotificationTemplate> findAllWithRulesAndRecipientsByIds(@Param("ids") List<Long> ids);
}