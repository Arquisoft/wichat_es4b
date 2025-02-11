package com.uniovi.validators;

import com.uniovi.dto.AnswerDto;
import com.uniovi.dto.QuestionDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class QuestionValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return QuestionDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        QuestionDto question = (QuestionDto) target;

        if (question.getStatement() == null || question.getStatement().isEmpty()) {
            errors.rejectValue("statement", null,
                    "The statement of the question cannot be empty");
        }

        if (question.getOptions() == null || question.getOptions().size() != 4) {
            errors.rejectValue("options", null,
                    "The question must have 4 options");
        }

        if (question.getOptions().stream().anyMatch(option -> option.getText() == null || option.getText().isEmpty())) {
            errors.rejectValue("options", null,
                    "The text of the options cannot be empty");
        }

        if (question.getOptions().stream().filter(AnswerDto::isCorrect).count() != 1) {
            errors.rejectValue("options", null,
                    "The question must have exactly one correct option");
        }


        if (!question.getOptions().contains(question.getCorrectAnswer()))
            errors.rejectValue("correctAnswer", null,
                    "The correct answer must be one of the options");


        if (question.getCategory() == null || question.getCategory().getName() == null || question.getCategory().getName().isEmpty()) {
            errors.rejectValue("category", null,
                    "The question must have a category");
        }
    }
}
