package com.que;

public class Question {
  private String questionText;
  private String[] options;
  private int answerIdx;

  public Question(String questionText, String[] options, int answerIdx) {
    this.questionText = questionText;
    this.options = options;
    this.answerIdx = answerIdx;
  }

  public String getQuestionText() {
    return questionText;
  }

  public String[] getOptions() {
    return options;
  }

  public int getCorrectAnswer() {
    return answerIdx;
  }

  public boolean isCorrect(int answerIdx) {
    return this.answerIdx == answerIdx;
  }
}
