package codingandProgramming.model;

import java.util.List;

/**
 * Supplies quiz questions without deciding which questions a session uses.
 */
public interface QuestionRepository {

	List<Question> findAllQuestions();
}
