package gr.athenarc.n4160.mailer.dao;

import gr.athenarc.n4160.mailer.domain.LogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogEntityDao extends JpaRepository<LogEntity, Long>{
}
