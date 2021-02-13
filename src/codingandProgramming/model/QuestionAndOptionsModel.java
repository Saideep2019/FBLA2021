package codingandProgramming.model;

import java.util.ArrayList;
import java.util.List;

public class QuestionAndOptionsModel {
	private String question;
	private List<String> options = new ArrayList<String>();
	private int displayType = 0;
	private int questionId = 0;
	public String questionParttwo;

	
	
	
	
	
	public String getQuestionParttwo() {
		return questionParttwo;
	}

	public void setQuestionParttwo(String questionParttwo) {
		this.questionParttwo = questionParttwo;
	}



	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	private String rightAnswer = "";




	public String getRightAnswer() {
		return rightAnswer;
	}

	public void setRightAnswer(String rightAnswer) {
		this.rightAnswer = rightAnswer;
	}

	public int getDisplayType() {
		return displayType;
	}

	public void setDisplayType(int displayType) {
		this.displayType = displayType;
	}

	public List<String> getOptions() {
		return options;
	}

	public void setOptions(List<String> options) {
		this.options = options;
	}

	public void addOption(String option) {
		options.add(option);

	}

	

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	@Override
	public String toString() {
		return question + " ---" + options.toString();

	}



}
