package ru.sejapoe.tinkab.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.sejapoe.tinkab.domain.WorkerLogEntity;

@Repository
public interface WorkerLogRepository extends CrudRepository<WorkerLogEntity, WorkerLogEntity> {
}
