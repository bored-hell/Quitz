package com.que;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.Reader;
import java.io.FileReader;
import java.io.Writer;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class QuizData {
  private static final String filename = "questions.json";
  private final List<Question> questions = new ArrayList<>();
  private int currentQuestionIdx;
  private int score;

  public QuizData() {
    loadQuestions();
    Collections.shuffle(questions);

    currentQuestionIdx = 0;
    score = 0;
  }

  public void reset() {
    currentQuestionIdx = 0;
    score = 0;
  }

  private void loadQuestions() {
  if (!Files.exists(Paths.get(filename))) {
      return;
    }

     try (Reader reader = new FileReader(filename);
         JsonReader jsonReader = new JsonReader(reader)) {

      jsonReader.beginArray();
      while (jsonReader.hasNext()) {
        jsonReader.beginObject();
        String qText = null;
        String[] qOptions = new String[4];
        int qCorrectIdx = -1;

        while (jsonReader.hasNext()) {
          String name = jsonReader.nextName();
          if (name.equals("questionText")) {
            qText = jsonReader.nextString();
          } else if (name.equals("options")) {
            jsonReader.beginArray();
            int i = 0;
            while (jsonReader.hasNext()) {
              qOptions[i++] = jsonReader.nextString();
            }
            i = 0;
            jsonReader.endArray();
          } else if (name.equals("correctAnswerIdx")) {
            qCorrectIdx = jsonReader.nextInt();
          } else {
            jsonReader.skipValue();
          }
        }
        jsonReader.endObject();

        if (qText != null && qCorrectIdx != -1) {
          questions.add(new Question(qText, qOptions, qCorrectIdx));
        }
      }

      jsonReader.endArray();
    } catch (Exception e) {
      System.err.println("couldn't read file: " + e.getMessage());
    }

  }

  public void saveQuestions() {
    if (questions.isEmpty()) {
      return;
    }

    try (Writer fileWriter = new FileWriter(filename);
         JsonWriter writer = new JsonWriter(fileWriter)) {
      writer.setIndent(" ");
      writer.beginArray();
      for (var question : questions) {
        writer.beginObject();
        writer.name("questionText").value(question.getQuestionText());
        writer.name("options");
        writer.beginArray();
        for (var option : question.getOptions()) {
          writer.value(option);
        }
        writer.endArray();
        writer.name("correctAnswerIdx").value(question.getCorrectAnswer());
        writer.endObject();
      }
      writer.endArray();
    } catch (Exception e) {
      System.err.println("couldn't write to file: " + e.getMessage());
    }
  }

  public Question getCurrentQuestion() {

    if (currentQuestionIdx < questions.size())
      return questions.get(currentQuestionIdx);
    return null;
  }

  public void addQuestion(String text, String[] options, int correctIdx) {
    Question question = new Question(text, options, correctIdx);
    questions.add(question);
  }

  public boolean hasMoreQuestions() {
    return currentQuestionIdx < questions.size();
  }

  public void submitAnswer(int selectedIdx) {
    Question question = getCurrentQuestion();
    if (question != null && question.isCorrect(selectedIdx))
      ++score;
    ++currentQuestionIdx;
  }

  public int getScore() {
    return score;
  }

  public int getCurrentQuestionIdx() {
    return currentQuestionIdx;
  }

  public int getTotalQuestions() {
    return questions.size(); 
  }
}
