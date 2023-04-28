package rmartin.ctf.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rmartin.ctf.search.db.SearchDataDB;
import rmartin.ctf.search.db.SearchDataDBRepository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Service
public class SearchService {

    private static final Logger log = Logger.getLogger(SearchService.class.getName());

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Autowired
    DataSource dataSource;

    @Autowired
    SearchDataDBRepository searchDataDBRepository;

    @Value("${challenge.flag}")
    String flag;

    public boolean search(String id, SearchRequest searchRequest) {
        log.info(searchRequest.toString());
        Pattern p;
        try {
            p = Pattern.compile(searchRequest.getSearch());
        } catch (Exception e){
            log.info("Failed to compile pattern: " + searchRequest.getSearch());
            return false;
        }

        var future = executor.submit(() ->
                p.matcher(searchRequest.getText()).matches()
        );
        try {
            Boolean result = future.get(2000, TimeUnit.MILLISECONDS);
            log.info("Modern save");
            modernSave(id, searchRequest);
            return result;
        } catch (TimeoutException | RegexTimeout | ExecutionException | InterruptedException e) {
            log.info("Legacy save");
            legacySave(id, searchRequest);
            return false;
        }
    }

    private void modernSave(String id, SearchRequest searchRequest) {
        var searchDataDB = new SearchDataDB(id, searchRequest.getText(), searchRequest.getSearch());
        this.searchDataDBRepository.save(searchDataDB);
    }

    private void legacySave(String id, SearchRequest searchRequest){
        String query = "UPDATE search_datadb SET text=?, regex=CONCAT(??) WHERE ip=?";
        query = query.replace("??", "'" + searchRequest.getSearch() + "'");
        log.info(query);
        try(var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(query)){

            // Usamos prepared statements para bloquear inyecciones
            statement.setString(1, searchRequest.getText());
            statement.setString(2, id);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @PostConstruct
    public void initializeSQL(){
        log.info("Loaded flag: " + flag);
        var searchResult = new SearchDataDB("127.0.0.1", flag, flag);
        searchDataDBRepository.save(searchResult);
        log.info("SQL initialization complete");
    }


    public List<String> getSearchesForClient(String id) {
        var list = new ArrayList<String>();
        for (var i : searchDataDBRepository.findAllByIp(id)) {
            list.add(i.getRegex());
        }
        return list;
    }
}
