package com.que;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Quiz extends Application {
  private final QuizData quizData = new QuizData();
  private final Label questionLabel = new Label();
  private final Button[] optionButtons = new Button[4];
  private final ProgressBar progressBar = new ProgressBar(0.0);
  private final Label progressLabel = new Label();
  private final Button addQuestionButton = new Button();

  private final BorderPane mainLayout = new BorderPane();
  private final VBox centerContainer = new VBox(20);

  @Override
  public void start(Stage stage) {
    stage.setTitle("Quiz");
    setupMainLayout();

    Scene scene = new Scene(mainLayout, 700, 530);
    scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

    stage.setOnCloseRequest(
        event -> {
          quizData.saveQuestions();
        });

    if (quizData.getTotalQuestions() == 0) {
      displayNoQuestionsMessage();
    }

    stage.setScene(scene);
    stage.setResizable(false);
    stage.show();
  }

  private void displayNoQuestionsMessage() {
    mainLayout.getChildren().clear();
    centerContainer.getChildren().clear();

    addQuestionButton.setText("+");
    addQuestionButton.setTooltip(new Tooltip("Add New Questions"));
    addQuestionButton.getStyleClass().add("add-icon-button");
    addQuestionButton.setOnAction(e -> displayAddQuestionForm());

    BorderPane topBar = new BorderPane();
    topBar.setPadding(new Insets(15, 15, 0, 15));

    HBox buttonWrapper = new HBox(addQuestionButton);
    buttonWrapper.setAlignment(Pos.TOP_RIGHT);
    topBar.setRight(buttonWrapper);

    Label progressPlaceholder = new Label("Quiz not started");
    ProgressBar progressBar = new ProgressBar(0.0);
    progressBar.setPrefWidth(400);

    VBox progressContainer = new VBox(5, progressPlaceholder, progressBar);
    progressContainer.setAlignment(Pos.CENTER);
    topBar.setCenter(progressContainer);

    Label message = new Label("No quiz questions found!");
    message.getStyleClass().add("label-bold");

    Label prompt = new Label("Please use the \'+\' button or click to add new question");

    Button goToAddButton = new Button("Add a question now");
    goToAddButton.getStyleClass().add("submit-button");
    goToAddButton.setOnAction(e -> displayAddQuestionForm());

    VBox messageLayout = new VBox(20);
    messageLayout.setAlignment(Pos.CENTER);
    messageLayout.setPadding(new Insets(50));
    messageLayout.getChildren().addAll(message, prompt, goToAddButton);

    mainLayout.setTop(topBar);
    mainLayout.setCenter(messageLayout);
  }

  private void setupMainLayout() {
    mainLayout.getChildren().clear();
    centerContainer.getChildren().clear();

    centerContainer.setPadding(new Insets(30));
    centerContainer.setAlignment(Pos.CENTER);

    progressBar.setPrefWidth(400);

    VBox progressContainer = new VBox(5, progressLabel, progressBar);
    progressContainer.setAlignment(Pos.CENTER);
    progressContainer.setPadding(new Insets(15, 0, 0, 0));

    questionLabel.setWrapText(true);
    questionLabel.getStyleClass().add("label-bold");

    if (optionButtons[0] == null) {
      for (int i = 0; i < 4; ++i) {
        final int optionIdx = i;
        optionButtons[i] = new Button();
        optionButtons[i].getStyleClass().add("option-button");
        optionButtons[i].setPrefWidth(400);
        optionButtons[i].setOnAction(e -> handleAnswer(optionIdx));
      }
    }

    addQuestionButton.setText("+");
    addQuestionButton.setTooltip(new Tooltip("Add New Question"));
    addQuestionButton.getStyleClass().add("add-icon-button");
    addQuestionButton.setOnAction(e -> displayAddQuestionForm());

    BorderPane topBar = new BorderPane();
    topBar.setPadding(new Insets(15, 15, 0, 15));

    topBar.setCenter(progressContainer);

    HBox buttonWrapper = new HBox(addQuestionButton);
    buttonWrapper.setAlignment(Pos.TOP_RIGHT);
    topBar.setRight(buttonWrapper);

    centerContainer.getChildren().add(questionLabel);
    for (Button button : optionButtons) {
      centerContainer.getChildren().add(button);
    }

    mainLayout.setTop(topBar);
    mainLayout.setCenter(centerContainer);

    if (quizData.getTotalQuestions() > 0) {
      displayNextQuestion();
    }
  }

  private void displayAddQuestionForm() {
    mainLayout.getChildren().clear();
    centerContainer.getChildren().clear();

    Label title = new Label("Add a New Quiz Question");
    title.getStyleClass().add("label-bold");

    Label addedCountLabel = new Label("Questions Added: " + quizData.getTotalQuestions());
    addedCountLabel.getStyleClass().add("label-bold");

    TextField questionText = new TextField();
    questionText.setPromptText("Enter the question text");

    TextField[] optionFields = new TextField[4];
    for (int i = 0; i < 4; ++i) {
      optionFields[i] = new TextField();
      optionFields[i].setPromptText("Option " + (char) ('A' + i));
    }

    ComboBox<String> correctAnswerCombo = new ComboBox<>();
    correctAnswerCombo.getItems().addAll("A", "B", "C", "D");
    correctAnswerCombo.setPromptText("Correct Answer");

    Button saveAndBackButton = new Button("Save and Back to Quiz");
    saveAndBackButton.getStyleClass().add("submit-button");

    Button addNextButton = new Button("Add Next Question");
    addNextButton.getStyleClass().add("submit-button");

    Runnable saveQuestionLogic =
        () -> {
          String qText = questionText.getText().trim();
          String[] options = new String[4];
          int correctIdx = -1;

          if (qText.isEmpty() || correctAnswerCombo.getValue() == null) {
            new Alert(AlertType.ERROR, "Question text and Correct Answer are required.")
                .showAndWait();
            return;
          }

          for (int i = 0; i < 4; ++i) {
            String option = optionFields[i].getText().trim();
            if (option.isEmpty()) {
              new Alert(AlertType.ERROR, "All four options must be provided.").showAndWait();
              return;
            }
            options[i] = option;
          }

          correctIdx = correctAnswerCombo.getValue().charAt(0) - 'A';
          try {
            quizData.addQuestion(qText, options, correctIdx);

            questionText.clear();
            for (TextField field : optionFields) {
              field.clear();
            }

            correctAnswerCombo.getSelectionModel().clearSelection();
            correctAnswerCombo.setPromptText("Correct Answer");

            addedCountLabel.setText("Questions Added: " + quizData.getTotalQuestions());
          } catch (Exception ex) {
            new Alert(AlertType.ERROR, "Failed to save question: " + ex.getMessage()).showAndWait();
          }
        };

    addNextButton.setOnAction(
        e -> {
          saveQuestionLogic.run();
        });

    saveAndBackButton.setOnAction(
        e -> {
          saveQuestionLogic.run();
          if (quizData.getTotalQuestions() > 0) {
            setupMainLayout();
          } else {
            displayNoQuestionsMessage();
          }
        });

    Button backButton = new Button("Back to Quiz");
    backButton.getStyleClass().add("back-button");
    backButton.setOnAction(
        e -> {
          if (quizData.getTotalQuestions() == 0) {
            displayNoQuestionsMessage();
          } else {
            setupMainLayout();
          }
        });

    HBox submitButtonContainer = new HBox(10, saveAndBackButton, addNextButton);
    submitButtonContainer.setAlignment(Pos.CENTER);

    VBox formLayout = new VBox(10);
    formLayout.setAlignment(Pos.CENTER);
    formLayout.setPadding(new Insets(30));
    formLayout.setPrefWidth(450);

    formLayout
        .getChildren()
        .addAll(
            title,
            addedCountLabel,
            questionText,
            optionFields[0],
            optionFields[1],
            optionFields[2],
            optionFields[3],
            correctAnswerCombo,
            submitButtonContainer,
            backButton);

    centerContainer.getChildren().add(formLayout);
    mainLayout.setCenter(centerContainer);
  }

  private void displayNextQuestion() {
    if (quizData.hasMoreQuestions()) {
      Question question = quizData.getCurrentQuestion();
      int current = quizData.getCurrentQuestionIdx();
      int total = quizData.getTotalQuestions();

      questionLabel.setText(
          "Question "
              + (quizData.getCurrentQuestionIdx() + 1)
              + "/"
              + quizData.getTotalQuestions()
              + ":\n"
              + quizData.getCurrentQuestion().getQuestionText());

      double progress = (double) current / total;
      progressBar.setProgress(progress);

      progressLabel.setText(String.format("Progress: %d of %d", current, total));

      String options[] = question.getOptions();
      for (int i = 0; i < 4; ++i) {
        optionButtons[i].setText((char) ('A' + i) + ". " + options[i]);
        optionButtons[i].setDisable(false);
      }
    } else {
      progressBar.setProgress(1.0);
      progressLabel.setText(
          String.format(
              "Progress: %d of %d",
              quizData.getCurrentQuestionIdx(), quizData.getTotalQuestions()));
      displayFinalScore();
    }
  }

  private void handleAnswer(int selectedIdx) {
    quizData.submitAnswer(selectedIdx);
    displayNextQuestion();
  }

  private void displayFinalScore() {
    centerContainer.getChildren().clear();
    mainLayout.getChildren().clear();

    Label scoreTitle = new Label("Quiz Complete!");
    scoreTitle.setId("score-title");

    Label scoreLabel =
        new Label(
            "Your final score is "
                + quizData.getScore()
                + " out of "
                + quizData.getTotalQuestions());
    scoreLabel.setId("score-label");

    Button replayButton = new Button("Start Over");
    replayButton.getStyleClass().add("replay-button");

    replayButton.setOnAction(
        e -> {
          quizData.reset();
          setupMainLayout();
        });

    centerContainer.getChildren().addAll(scoreTitle, scoreLabel, replayButton);
    mainLayout.setCenter(centerContainer);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
