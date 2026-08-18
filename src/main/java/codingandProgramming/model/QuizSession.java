package codingandProgramming.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Selects and scores the five questions in one quiz attempt.
 */
public final class QuizSession {

	public static final int SESSION_LENGTH = 5;

	private final List<Question> selectedQuestions;
	private final List<QuizResult> results = new ArrayList<>();
	private int score;

	public QuizSession(QuestionRepository repository, Random random) {
		Objects.requireNonNull(repository, "repository");
		Objects.requireNonNull(random, "random");

		Map<Integer, Question> uniqueQuestionsById = new LinkedHashMap<>();
		for (Question question : repository.findAllQuestions()) {
			uniqueQuestionsById.putIfAbsent(question.getId(), question);
		}

		List<Question> candidates = new ArrayList<>(uniqueQuestionsById.values());
		if (candidates.size() < SESSION_LENGTH) {
			throw new IllegalArgumentException("A quiz session requires at least five distinct questions");
		}

		for (int index = 0; index < SESSION_LENGTH; index++) {
			int selectedIndex = index + random.nextInt(candidates.size() - index);
			Question selected = candidates.get(selectedIndex);
			candidates.set(selectedIndex, candidates.get(index));
			candidates.set(index, selected);
		}
		selectedQuestions = List.copyOf(candidates.subList(0, SESSION_LENGTH));
	}

	public List<Question> getSelectedQuestions() {
		return selectedQuestions;
	}

	public Question getCurrentQuestion() {
		if (isComplete()) {
			throw new IllegalStateException("The quiz session is complete");
		}
		return selectedQuestions.get(results.size());
	}

	public QuizResult submitAnswer(String selectedAnswer) {
		Question question = getCurrentQuestion();
		String recordedAnswer = selectedAnswer == null ? "" : selectedAnswer;
		boolean correct = recordedAnswer.equalsIgnoreCase(question.getCorrectAnswer());
		QuizResult result = new QuizResult(question, recordedAnswer, correct);
		results.add(result);
		if (correct) {
			score++;
		}
		return result;
	}

	public int getScore() {
		return score;
	}

	public boolean isComplete() {
		return results.size() == SESSION_LENGTH;
	}

	public QuizReport getReport() {
		return new QuizReport(results, SESSION_LENGTH);
	}
}
