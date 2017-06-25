package Models;

import DAO.ServiceDAO;
import Entities.ServiceEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by Rishat_Valitov on 16.06.17.
 */

@Component("ServiceModel")
public class ServiceModel {
    @Autowired
    private ServiceDAO serviceDAO;

    public ServiceModel(JdbcTemplate jdbcTemplate) {
//        this.serviceDAO = new ServiceDAO(jdbcTemplate);
    }

    public ResponseEntity<String> getInfo() {
        final ServiceEntity serviceEntity = serviceDAO.getInfo();
        return new ResponseEntity<>(serviceEntity.getJSONString(), HttpStatus.OK);
    }

    public ResponseEntity<String> clear() {
        return new ResponseEntity<>(serviceDAO.clear().getJSONString(), HttpStatus.OK);
    }
}
