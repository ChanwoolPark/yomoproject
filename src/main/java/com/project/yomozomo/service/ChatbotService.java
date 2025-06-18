// src/main/java/com/project/yomozomo/service/ChatbotService.java
package com.project.yomozomo.service;

import com.project.yomozomo.dto.ChatbotResponse;
import com.project.yomozomo.entity.ChatbotAnswer;
import com.project.yomozomo.entity.ChatbotKeyword;
import com.project.yomozomo.entity.ChatbotOption;
import com.project.yomozomo.entity.ChatbotQuestion;
import com.project.yomozomo.repository.ChatbotAnswerRepository;
import com.project.yomozomo.repository.ChatbotKeywordRepository;
import com.project.yomozomo.repository.ChatbotOptionRepository;
import com.project.yomozomo.repository.ChatbotQuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private final ChatbotQuestionRepository chatbotQuestionRepository;
    private final ChatbotAnswerRepository chatbotAnswerRepository;
    private final ChatbotOptionRepository chatbotOptionRepository;
    private final ChatbotKeywordRepository chatbotKeywordRepository;

    private static final Long INITIAL_QUESTION_ID = 1L;

    public ChatbotService(ChatbotQuestionRepository chatbotQuestionRepository,
                          ChatbotAnswerRepository chatbotAnswerRepository,
                          ChatbotOptionRepository chatbotOptionRepository,
                          ChatbotKeywordRepository chatbotKeywordRepository) {
        this.chatbotQuestionRepository = chatbotQuestionRepository;
        this.chatbotAnswerRepository = chatbotAnswerRepository;
        this.chatbotOptionRepository = chatbotOptionRepository;
        this.chatbotKeywordRepository = chatbotKeywordRepository;
    }

    public ChatbotResponse getInitialMessage() {
        Optional<ChatbotQuestion> initialQuestionOpt = chatbotQuestionRepository.findById(INITIAL_QUESTION_ID);
        if (initialQuestionOpt.isPresent()) {
            ChatbotQuestion initialQuestion = initialQuestionOpt.get();
            List<String> suggestedKeywords = getSuggestedKeywordsFromInitialOptions();
            // ChatbotResponse 생성자 수정 (질문 유형은 question, answer는 null)
            return new ChatbotResponse("QUESTION", null, initialQuestion.getContent(), null, suggestedKeywords);
        }
        // 에러 응답도 적절한 생성자를 사용하도록 변경
        return new ChatbotResponse("ERROR", "챗봇을 시작할 수 없습니다.");
    }

    public ChatbotResponse processMessage(String message) {
        Optional<ChatbotKeyword> matchedKeywordOpt = chatbotKeywordRepository.findByKeyword(message);

        if (matchedKeywordOpt.isPresent()) {
            ChatbotKeyword matchedKeyword = matchedKeywordOpt.get();
            Optional<ChatbotAnswer> answerOpt = chatbotAnswerRepository.findById(matchedKeyword.getAnswerId());
            if (answerOpt.isPresent()) {
                // *** 이 부분 수정 ***
                // ChatbotResponse("ANSWER", ChatbotAnswer 엔티티) 생성자를 호출합니다.
                // ChatbotResponse 내부에서 ChatbotAnswer를 ChatbotAnswerResponseDto로 변환합니다.
                return new ChatbotResponse("ANSWER", answerOpt.get()); // question, errorMessage, suggestedKeywords는 null로 초기화될 것임
            }
        }

        // 2. 옵션 매칭 시도
        // ChatbotOption 엔티티에도 @ManyToOne(fetch = FetchType.LAZY) 설정이 되어 있다면
        // nextQuestion과 answer 필드에 접근할 때 주의해야 합니다.
        // 현재 ChatbotOption의 nextQuestion과 answer 필드에 @Getter가 있다면 문제가 없을 수도 있습니다.
        // 하지만 만약 nextQuestion이나 answer가 null인데 .getId()나 다른 메서드를 호출하면 NPE가 발생할 수 있습니다.
        Optional<ChatbotOption> matchedOptionOpt = chatbotOptionRepository.findByContent(message);
        if (matchedOptionOpt.isPresent()) {
            ChatbotOption matchedOption = matchedOptionOpt.get();

            // 다음 질문으로 이동하는 경우
            if (matchedOption.getNextQuestion() != null) { // ID로 먼저 체크
                Optional<ChatbotQuestion> nextQuestionOpt = chatbotQuestionRepository.findById(matchedOption.getNextQuestion().getId());
                if(nextQuestionOpt.isPresent()) {
                    ChatbotQuestion nextQuestion = nextQuestionOpt.get();
                    List<String> suggestedKeywords = getSuggestedKeywordsFromQuestionOptions(nextQuestion.getId());
                    return new ChatbotResponse("QUESTION", null, nextQuestion.getContent(), null, suggestedKeywords);
                }
            }
            // 최종 답변으로 이동하는 경우
            else if (matchedOption.getAnswer() != null) { // ID로 먼저 체크
                Optional<ChatbotAnswer> answerOpt = chatbotAnswerRepository.findById(matchedOption.getAnswer().getId());
                if (answerOpt.isPresent()) {
                    List<String> suggestedKeywords = getSuggestedKeywordsFromInitialOptions(); // 답변 후 초기 옵션 추천
                    // *** 이 부분 수정 ***
                    return new ChatbotResponse("ANSWER", answerOpt.get());
                }
            }
        }

        // 3. 어떤 것도 매칭되지 않았을 경우
        List<String> suggestedKeywords = getSuggestedKeywordsFromInitialOptions();
        return new ChatbotResponse("ERROR", "죄송합니다. 이해하지 못했습니다. 다음 키워드를 이용해 보세요.", suggestedKeywords);
    }

    private List<String> getSuggestedKeywordsFromInitialOptions() {
        return chatbotOptionRepository.findByQuestionId(INITIAL_QUESTION_ID).stream()
                .map(ChatbotOption::getContent)
                .collect(Collectors.toList());
    }

    private List<String> getSuggestedKeywordsFromQuestionOptions(Long questionId) {
        return chatbotOptionRepository.findByQuestionId(questionId).stream()
                .map(ChatbotOption::getContent)
                .collect(Collectors.toList());
    }
}