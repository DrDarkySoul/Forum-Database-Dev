package Controllers;

import Models.ServiceModel;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Rishat_Valitov on 16.06.17.
 */

@RestController
@RequestMapping("api/service/")
public class ServiceController {
    private final ServiceModel serviceModel;

    public ServiceController(JdbcTemplate jdbcTemplate) {
        this.serviceModel = new ServiceModel(jdbcTemplate);
    }

    @RequestMapping(path = "/clear", method = RequestMethod.POST)
    public ResponseEntity<String> clear() {
        return (serviceModel.clear());
    }

    @RequestMapping(path = "/status", method = RequestMethod.GET)
    public ResponseEntity<String> getStatus() {
        return (serviceModel.getInfo());
    }
}
