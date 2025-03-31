package com.uniovi.entities;

import com.uniovi.entities.abstracts.AbstractQuestion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Question extends AbstractQuestion<Answer> {

    public Question(String statement, List<Answer> options, Answer correctAnswer, Category category, String language) {
        super(statement, options, correctAnswer, category, language);
    }

    public Question(String statement, List<Answer> options, Answer correctAnswer, Category category, Language language) {
        super(statement, options, correctAnswer, category, language);
    }

    @Override
    protected void doOptionsAssociation(List<Answer> options) {
        Associations.QuestionAnswers.addAnswer(this, options);
    }

    @Override
    public String toString() {
        return "Question{" +
                "statement='" + getStatement() + '\'' +
                ", options=" + getOptions().toString() +
                ", correctAnswer=" + getCorrectAnswer().toString() +
                ", category=" + getCategory() +
                '}';
    }
}
