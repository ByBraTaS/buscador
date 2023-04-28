package rmartin.ctf.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.logging.Logger;

@Controller
public class WebController {
    private static final Logger log = Logger.getLogger(WebController.class.getName());

    @Autowired
    SearchService searchService;

    @GetMapping("/")
    ModelAndView index(HttpServletRequest request) {
        var session = request.getSession();
        var mv = new ModelAndView("index");
        mv.addObject("text", "");
        mv.addObject("search", "");
        mv.addObject("historic", searchService.getSearchesForClient(session.getId()));
        return mv;
    }

    @PostMapping("/")
    ModelAndView doSearch(@RequestParam String serializedJSON, HttpServletRequest request) {
        var session = request.getSession();
        log.info(String.format("Processing request for client %s:%s, search %s ", request.getRemoteAddr(), session.getId(), serializedJSON));

        var mv = new ModelAndView();

        if (serializedJSON.length() > 100_000) {
            mv.setViewName("error");
            mv.addObject("reason", "Too big");
            return mv;
        }

        // Step 1: Filter bad chars
        if (containsBadChars(serializedJSON)) {
            log.info("Request blocked");
            mv.setViewName("error");
            mv.addObject("reason", "Failed string validation");
            return mv;
        }

        // Step 2: Try to deserilize, may fail
        SearchRequest searchRequest;
        try {
            var deserializer = new ObjectMapper();
            searchRequest = deserializer.readValue(serializedJSON, SearchRequest.class);
            log.info("Request deserialized");
        } catch (JsonProcessingException e) {
            log.info("Request deserialization failed:" + e);
            mv.setViewName("error");
            mv.addObject("reason", "JSON deserialization failed: " + e);
            return mv;
        }

        // Step 3: Search
        boolean searchResult = searchService.search(session.getId(), searchRequest);
        log.info("Search results: " + searchResult);

        log.info("Request search ok");
        mv.setViewName("index");
        mv.addObject("text", searchRequest.getText());
        mv.addObject("search", searchRequest.getSearch());
        List<String> searchesForClient = searchService.getSearchesForClient(session.getId());
        mv.addObject("historic", searchesForClient);
        log.info("Searches for ip: " + searchesForClient);
        return mv;
    }

    private boolean containsBadChars(String serializedJSON) {
        String[] blacklist = new String[]{"'", "´", "`",};
        for (String block : blacklist) {
            if (serializedJSON.contains(block)) {
                return true;
            }
        }
        return false;
    }
}
