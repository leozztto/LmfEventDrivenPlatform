package com.lmf.platform.messaging;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);

    /**
     * Trava as linhas pendentes com {@code FOR UPDATE SKIP LOCKED} — várias instâncias do relay podem
     * rodar em paralelo sem publicar o mesmo evento duas vezes. Deve ser chamado dentro de uma
     * transação.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("select o from OutboxEvent o where o.status = :status order by o.createdAt asc")
    List<OutboxEvent> lockPending(@Param("status") OutboxStatus status, Limit limit);
}
