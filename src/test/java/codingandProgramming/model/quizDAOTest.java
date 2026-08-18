package codingandProgramming.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class quizDAOTest {

	@Test
	void questionIdsFortyNineAndFiftyRemainIneligibleToCharacterizeTheLegacyRandomBound() {
		assertTrue(quizDAO.isLegacyEligibleQuestionId(1));
		assertTrue(quizDAO.isLegacyEligibleQuestionId(48));
		assertFalse(quizDAO.isLegacyEligibleQuestionId(49));
		assertFalse(quizDAO.isLegacyEligibleQuestionId(50));
	}
}
