package Models;

import DAO.ServiceDAO;
import Entities.ServiceEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by Rishat_Valitov on 16.06.17.
 */

public class ServiceModel {
    private final ServiceDAO serviceDAO;

    public ServiceModel(JdbcTemplate jdbcTemplate) {
        this.serviceDAO = new ServiceDAO(jdbcTemplate);
    }

    public ResponseEntity<String> getInfo() {
        final ServiceEntity serviceEntity = serviceDAO.getInfo();
        return new ResponseEntity<>(serviceEntity.getJSONString(), HttpStatus.OK);
    }

    public ResponseEntity<String> clear() {
        return new ResponseEntity<>(serviceDAO.clear().getJSONString(), HttpStatus.OK);
    }
}
