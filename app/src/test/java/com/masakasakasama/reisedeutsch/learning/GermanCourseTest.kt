package com.masakasakasama.reisedeutsch.learning

import com.masakasakasama.reisedeutsch.data.ContentPack
import com.masakasakasama.reisedeutsch.data.GermanCourse
import org.junit.Assert.*
import org.junit.Test

class GermanCourseTest {
    @Test fun everyExerciseHasOneAcceptedChoiceAndRejectsItsDistractors() {
        GermanCourse.units.forEach { unit ->
            assertEquals(3, unit.questions.size)
            assertTrue(unit.sections.isNotEmpty() && unit.examples.isNotEmpty())
            unit.cards().zip(unit.questions).forEach { (card, question) ->
                assertTrue(question.explanation.isNotBlank())
                assertEquals(1, question.choices.count { AnswerGrader.matches(card, it) })
                assertTrue(AnswerGrader.matches(card, question.answer))
                assertEquals(question, GermanCourse.question(card.id))
            }
        }
    }

    @Test fun allCoursePracticeCardsResolveInTheSharedContentAndHaveUniqueIds() {
        val base = ContentPack(emptyList(), emptyList(), emptyList(), emptyList())
        val content = GermanCourse.extend(base)
        assertEquals(14, content.lessons.size)
        assertEquals(42, content.cards.size)
        assertEquals(content.cards.size, content.cards.map { it.id }.distinct().size)
        content.lessons.forEach { lesson ->
            assertEquals(3, lesson.cardIds.size)
            lesson.cardIds.forEach { assertNotNull(content.card(it)) }
        }
    }

    @Test fun nounCapitalizationExercisesDoNotAcceptIncorrectLowercase() {
        val unit = GermanCourse.units.first()
        val card = unit.cards()[1]
        assertTrue(AnswerGrader.matches(card, "  Das ist ein Hotel! "))
        assertFalse(AnswerGrader.matches(card, "Das ist ein hotel."))
        assertFalse(AnswerGrader.matches(card, "Das ist Ein Hotel."))
    }
}
