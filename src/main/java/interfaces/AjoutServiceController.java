package interfaces;

import models.Service;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import java.math.BigDecimal;

public class AjoutServiceController {

    @FXML private TextField nomField;
    @FXML private ComboBox<Service.TypeService> typeCombo;
    @FXML private TextField tarifField;
    @FXML private ComboBox<Service.Frequence> frequenceCombo;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<Service.StatutService> statutCombo;

    private Service service;

    @FXML
    public void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList(Service.TypeService.values()));
        frequenceCombo.setItems(FXCollections.observableArrayList(Service.Frequence.values()));
        statutCombo.setItems(FXCollections.observableArrayList(Service.StatutService.values()));
        statutCombo.setValue(Service.StatutService.actif);
    }

    public Service getService() {
        try {
            Service s = new Service();
            s.setNomService(nomField.getText());
            s.setTypeService(typeCombo.getValue());
            s.setTarif(new BigDecimal(tarifField.getText()));
            s.setFrequence(frequenceCombo.getValue());
            s.setDateDebut(dateDebut.getValue());
            s.setDateFin(dateFin.getValue());
            s.setStatut(statutCombo.getValue());
            return s;
        } catch (Exception e) {
            return null;
        }
    }

    public void setService(Service service) {
        this.service = service;
        if (service != null) {
            nomField.setText(service.getNomService());
            typeCombo.setValue(service.getTypeService());
            tarifField.setText(service.getTarif().toString());
            frequenceCombo.setValue(service.getFrequence());
            dateDebut.setValue(service.getDateDebut());
            dateFin.setValue(service.getDateFin());
            statutCombo.setValue(service.getStatut());
        }
    }
}