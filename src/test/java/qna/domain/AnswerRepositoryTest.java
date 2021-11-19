package qna.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import qna.CannotDeleteException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
public class AnswerRepositoryTest {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    private Answer answer;
    private Question question;
    private User user;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        user = userRepository.save(UserTest.JAVAJIGI);
        question = questionRepository.save(new Question("title1", "contents1").writeBy(user));
        answer = answerRepository.save(new Answer(user, question, "Answers Contents1"));
    }


    @DisplayName("삭제되지않은 answer 찾기")
    @Test
    void findByIdAndDeletedFalseTest() {
        // when
        Answer answerFromRepository = answerRepository.findByIdAndDeletedFalse(answer.getId())
                .orElseThrow(NoSuchElementException::new);

        // then
        assertEquals(answerFromRepository, answer);
    }

    @DisplayName("삭제한 answer 확인")
    @Test
    void findByIdAndDeletedTrueTest() throws CannotDeleteException {
        // when
        answer.delete(user);

        // then
        assertThatThrownBy(() -> {
            answerRepository.findByIdAndDeletedFalse(answer.getId())
                    .orElseThrow(NoSuchElementException::new);
        }).isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("question id 기준으로 삭제되지않은 answer 목록 찾기")
    @Test
    void findByQuestionIdAndDeletedFalseTest() {
        // when
        List<Answer> noneDeletedAnswers = answerRepository.findByQuestionIdAndDeletedFalse(question.getId());

        // then1
        assertThat(noneDeletedAnswers.size()).isEqualTo(1);

        // when
        Answer answerFromRepository = noneDeletedAnswers.get(0);

        // then2
        assertEquals(answerFromRepository, answer);
    }

    @DisplayName("question id 기준으로 삭제된 answer 목록 확인")
    @Test
    void findByQuestionIdAndDeletedTrueTest() throws CannotDeleteException {
        // when
        answer.delete(user);
        List<Answer> noneDeletedAnswers = answerRepository.findByQuestionIdAndDeletedFalse(question.getId());

        // then
        assertThat(noneDeletedAnswers.size()).isZero();
    }

    @DisplayName("answer 삭제")
    @Test
    void removeAnswerTest() {
        // 기본 조건 테스트
        assertThat(answerRepository.findAll().size()).isEqualTo(1);

        // when
        Answer actual = answerRepository.findAll().get(0);
        question.removeAnswer(actual);

        // then
        assertThat(answer).isSameAs(actual);
        assertThat(answerRepository.findAll().size()).isZero();
    }

    @DisplayName("answer delete with user 테스트")
    @Test
    void removeAnswerWithUserTest() throws CannotDeleteException {
        // 기본 조건 테스트
        assertThat(answerRepository.findAll().size()).isEqualTo(1);

        // when
        answer.delete(user);

        // then
        assertThat(answerRepository.findByIdAndDeletedFalse(answer.getId())).isEqualTo(Optional.empty());
    }

    @AfterEach
    void beforeFinish() {
        answerRepository.flush();
        questionRepository.flush();
        userRepository.flush();
    }
}
