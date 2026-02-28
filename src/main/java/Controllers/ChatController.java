package Controllers;

import Models.Message;
import Models.Role;
import Models.User;
import Services.GroqApiService;
import Services.JdbcMessageDao;
import Services.JdbcUserDao;
import Services.ProfanityFilterService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import Controllers.AppState;
import utils.Db;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    @FXML
    private ListView<User> contactsListView;
    @FXML
    private Label chatTitleLabel;
    @FXML
    private Label statusIconLabel;
    @FXML
    private VBox messagesContainer;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private TextField messageInput;
    @FXML
    private Button aiReformulateBtn;
    @FXML
    private ProgressIndicator aiLoadingIndicator;

    private final GroqApiService groqService = new GroqApiService();

    private User currentUser;
    private User selectedContact;
    private JdbcMessageDao messageDao;
    private JdbcUserDao userDao;
    private Timeline pollingTimeline;
    private final ProfanityFilterService profanityFilter = new ProfanityFilterService();

    private ObservableList<User> contactsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = AppState.getCurrentUser();
        if (currentUser == null) {
            System.err.println("ChatController: Aucun utilisateur connecté.");
            return;
        }

        try {
            Connection connection = Db.getConnection();
            messageDao = new JdbcMessageDao(connection);
            userDao = new JdbcUserDao(connection);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        setupContactsList();
        loadContacts();

        // Configurer le polling pour rafraîchir les messages (toutes les 2 secondes)
        pollingTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> refreshMessages()));
        pollingTimeline.setCycleCount(Timeline.INDEFINITE);
        // On ne le lance que quand un contact est sélectionné
    }

    private void setupContactsList() {
        contactsListView.setItems(contactsList);
        contactsListView.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getFullName() + " (" + item.getRole() + ")");
                }
            }
        });

        contactsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedContact = newVal;
                chatTitleLabel.setText("Chat avec " + newVal.getFullName());
                statusIconLabel.setVisible(true);
                loadMessages();
                pollingTimeline.play();
            } else {
                pollingTimeline.stop();
            }
        });
    }

    private void loadContacts() {
        contactsList.clear();
        try {
            // Si on est admin, on récupère un mix entre contacts récents et tous les
            // clients
            // Si on est client, on récupère les contacts récents et tous les admins
            List<User> allUsers = userDao.findAll();
            for (User u : allUsers) {
                if (u.getId().equals(currentUser.getId()))
                    continue; // pas soi-même
                if (currentUser.getRole() == Role.ADMIN) {
                    // L'admin voit les clients ou les autres admins
                    contactsList.add(u);
                } else {
                    // Le client voit uniquement les admins
                    if (u.getRole() == Role.ADMIN) {
                        contactsList.add(u);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMessages() {
        refreshMessages();
        // Scroll automatique tout en bas dès le chargement
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }

    private void refreshMessages() {
        if (selectedContact == null)
            return;
        try {
            List<Message> messages = messageDao.getConversation(currentUser.getId(), selectedContact.getId());

            // Marquer comme lus les messages reçus de ce contact
            boolean hasUnread = messages.stream()
                    .anyMatch(m -> !m.isRead() && m.getReceiverId().equals(currentUser.getId()));
            if (hasUnread) {
                messageDao.markAsRead(selectedContact.getId(), currentUser.getId());
            }

            Platform.runLater(() -> {
                messagesContainer.getChildren().clear();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

                for (Message m : messages) {
                    boolean isMe = m.getSenderId().equals(currentUser.getId());

                    HBox messageBox = new HBox();
                    messageBox.setPadding(new Insets(5, 10, 5, 10));
                    messageBox.setMaxWidth(Double.MAX_VALUE);

                    VBox bubble = new VBox();
                    bubble.setPadding(new Insets(10));
                    bubble.setSpacing(5);
                    bubble.setStyle("-fx-background-radius: 15;");

                    Label contentLabel = new Label(m.getContent());
                    contentLabel.setWrapText(true);
                    contentLabel.setMaxWidth(300); // Limite de largeur pour la bulle

                    Label timeLabel = new Label(m.getTimestamp().format(formatter));
                    timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");

                    if (isMe) {
                        // Mon message (à droite)
                        messageBox.setAlignment(Pos.CENTER_RIGHT);
                        bubble.setStyle("-fx-background-color: #fca5a5; -fx-background-radius: 15 15 0 15;"); // Orange
                                                                                                              // pâle du
                                                                                                              // thème
                        contentLabel.setStyle("-fx-text-fill: #7c2d12;"); // Texte foncé
                        bubble.getChildren().addAll(contentLabel, timeLabel);
                        timeLabel.setAlignment(Pos.CENTER_RIGHT);
                    } else {
                        // Message de l'autre (à gauche)
                        messageBox.setAlignment(Pos.CENTER_LEFT);
                        bubble.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 15 15 15 0;"); // Gris
                                                                                                              // clair
                        contentLabel.setStyle("-fx-text-fill: #374151;");
                        bubble.getChildren().addAll(contentLabel, timeLabel);
                        timeLabel.setAlignment(Pos.CENTER_LEFT);
                    }

                    messageBox.getChildren().add(bubble);
                    messagesContainer.getChildren().add(messageBox);
                }

                // Autoscroll to bottom if new messages appeared
                // Un check plus fin serait de voir si le count a changé
                chatScrollPane.setVvalue(1.0);
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onSendMessage(ActionEvent e) {
        if (selectedContact == null) {
            return;
        }

        String content = messageInput.getText();
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        // Vérification des mots impolis
        if (profanityFilter.containsProfanity(content)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ALERTE SÉCURITÉ");
            alert.setHeaderText("COMPTE BANNI IMMÉDIATEMENT");
            alert.setContentText(
                    "En raison de l'utilisation d'un langage inapproprié, votre accès à FinTrack est révoqué.");
            alert.showAndWait();

            // Bannir l'utilisateur en BDD
            currentUser.setBanned(true);
            try (Connection cn = utils.Db.getConnection()) {
                new Services.JdbcUserDao(cn).update(currentUser);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            // Déconnexion forcée
            AppState.clear();
            SceneNavigator.goLogin();
            return;
        }

        Message msg = new Message(currentUser.getId(), selectedContact.getId(), content.trim());
        try {
            messageDao.insert(msg);
            messageInput.clear();
            loadMessages();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void onAiReformulate(ActionEvent e) {
        String input = messageInput.getText();
        if (input == null || input.isBlank()) {
            return;
        }

        aiLoadingIndicator.setVisible(true);
        aiReformulateBtn.setDisable(true);

        groqService.getFintechSuggestion(input)
                .thenAccept(suggestion -> Platform.runLater(() -> {
                    messageInput.setText(suggestion);
                    aiLoadingIndicator.setVisible(false);
                    aiReformulateBtn.setDisable(false);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        aiLoadingIndicator.setVisible(false);
                        aiReformulateBtn.setDisable(false);
                    });
                    return null;
                });
    }

    public void stopPolling() {
        if (pollingTimeline != null) {
            pollingTimeline.stop();
        }
    }
}
