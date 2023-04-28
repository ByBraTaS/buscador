package rmartin.ctf.search.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchDataDBRepository extends JpaRepository<SearchDataDB, Integer> {
    List<SearchDataDB> findAllByIp(String ip);
}
