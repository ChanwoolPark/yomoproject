// src/main/java/com/project/yomozomo/service/ChatbotService.java
package com.project.yomozomo.service;

import com.project.yomozomo.entity.ChatbotAnswer;
import com.project.yomozomo.entity.ChatbotOption;
import com.project.yomozomo.entity.ChatbotQuestion;
import com.project.yomozomo.repository.ChatbotAnswerRepository;
import com.project.yomozomo.repository.ChatbotOptionRepository;
import com.project.yomozomo.repository.ChatbotQuestionRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// Lombok Import 추가
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor; // 필요하다면 추가
import lombok.AllArgsConstructor; // 필요하다면 추가

@Service
public class ChatbotService {

    private final ChatbotQuestionRepository questionRepository;
    private final ChatbotOptionRepository optionRepository;
    private final ChatbotAnswerRepository answerRepository;

    @Autowired
    public ChatbotService(ChatbotQuestionRepository questionRepository,
                          ChatbotOptionRepository optionRepository,
                          ChatbotAnswerRepository answerRepository) {
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.answerRepository = answerRepository;
    }

    @Transactional(readOnly = true)
    public Optional<ChatbotQuestion> getInitialQuestion() {
        return questionRepository.findById(1L);
    }

    @Transactional(readOnly = true)
    public ChatbotResponse processOptionSelection(Long optionId) {
        Optional<ChatbotOption> optionalOption = optionRepository.findById(optionId);

        if (optionalOption.isEmpty()) {
            return ChatbotResponse.error("선택하신 옵션을 찾을 수 없습니다.");
        }

        ChatbotOption selectedOption = optionalOption.get();

        if (selectedOption.getNextQuestion() != null) {
            return ChatbotResponse.nextQuestion(selectedOption.getNextQuestion());
        } else if (selectedOption.getAnswer() != null) {
            return ChatbotResponse.answer(selectedOption.getAnswer());
        } else {
            return ChatbotResponse.error("챗봇 흐름 설정에 오류가 있습니다.");
        }
    }

    // 챗봇 응답을 위한 내부 DTO 클래스 (Service와 Controller 간 데이터 전달용)
    // Lombok 어노테이션을 클래스 레벨에 적용
    @Getter // ChatbotResponse 클래스의 모든 필드에 대한 Getter 자동 생성
    @Setter // ChatbotResponse 클래스의 모든 필드에 대한 Setter 자동 생성
    @NoArgsConstructor // Lombok의 기본 생성자 (필요한 경우)
    @AllArgsConstructor // Lombok의 모든 필드를 인자로 받는 생성자 (필요한 경우)
    public static class ChatbotResponse {
        private String type; // "question" or "answer" or "error"
        private ChatbotQuestion question; // type이 "question"일 경우
        private ChatbotAnswer answer;     // type이 "answer"일 경우
        private String errorMessage;      // type이 "error"일 경우

        // static 팩토리 메서드들은 그대로 유지
        public static ChatbotResponse nextQuestion(ChatbotQuestion question) {
            ChatbotResponse response = new ChatbotResponse();
            response.setType("question");
            response.setQuestion(question);
            return response;
        }

        public static ChatbotResponse answer(ChatbotAnswer answer) {
            ChatbotResponse response = new ChatbotResponse();
            response.setType("answer");
            response.setAnswer(answer);
            return response;
        }

        public static ChatbotResponse error(String errorMessage) {
            ChatbotResponse response = new ChatbotResponse();
            response.setType("error");
            response.setErrorMessage(errorMessage);
            return response;
        }

        // 수동으로 작성했던 Getter/Setter 메서드들은 모두 삭제 (Lombok이 자동으로 생성)
        // private String getType() { return type; } // 삭제
        // private void setType(String type) { this.type = type; } // 삭제
        // ... (나머지 Getter/Setter도 모두 삭제)
    }

    // --- 챗봇 데이터 초기화를 위한 메서드 (개발/테스트용) ---
    @PostConstruct // 이 어노테이션은 ChatbotController로 이동하는 것이 더 적절합니다.
    // 서비스에서 직접 호출하면 트랜잭션 등 예상치 못한 문제가 발생할 수 있습니다.
    // 아래 코드는 테스트/개발용으로 유지합니다.
    @Transactional
    public void initializeChatbotData() {
        // 기존 데이터 삭제 (테스트용)
        optionRepository.deleteAll();
        questionRepository.deleteAll();
        answerRepository.deleteAll();

        // 1. 질문 생성
        ChatbotQuestion q1 = new ChatbotQuestion(null, "토스에서 진행한 결제에 대해 궁금하신 내용을 선택해주세요.", null);
        questionRepository.save(q1);

        ChatbotQuestion q2 = new ChatbotQuestion(null, "토스페이(온라인 결제)에 대해 궁금하신 내용을 선택해주세요.", null);
        questionRepository.save(q2);

        ChatbotQuestion q3 = new ChatbotQuestion(null, "오프라인 결제에 대해 궁금하신 내용을 선택해주세요.", null);
        questionRepository.save(q3);

        // 2. 답변 생성
        ChatbotAnswer a1 = new ChatbotAnswer(null, "결제 후 혜택은 다음 달 N일에 지급됩니다. 자세한 내용은 토스 앱의 혜택 페이지를 참고해주세요.");
        answerRepository.save(a1);

        ChatbotAnswer a2 = new ChatbotAnswer(null, "결제 금액이 다르게 표시되었다면, 토스 고객센터로 문의주시거나 영수증을 첨부하여 신고해주세요.");
        answerRepository.save(a2);

        ChatbotAnswer a3 = new ChatbotAnswer(null, "결제 내역은 토스 앱 > 전체 > 내 결제 내역에서 확인하실 수 있습니다.");
        answerRepository.save(a3);

        ChatbotAnswer a4 = new ChatbotAnswer(null, "환불 문의는 구매처에 직접 문의하시는 것이 가장 빠릅니다. 판매자와 연락이 어렵다면 토스 고객센터로 문의해주세요.");
        answerRepository.save(a4);

        ChatbotAnswer a5 = new ChatbotAnswer(null, "후불 결제 서비스에 대한 정보는 토스 앱 > 전체 > 후불결제 메뉴에서 확인하실 수 있습니다.");
        answerRepository.save(a5);

        // 3. 옵션 생성 및 연결
        // Q1의 옵션
        ChatbotOption opt1_1 = new ChatbotOption(null, "온라인 결제", 1, q1, q2, null);
        optionRepository.save(opt1_1);

        ChatbotOption opt1_2 = new ChatbotOption(null, "오프라인 결제", 2, q1, q3, null);
        optionRepository.save(opt1_2);

        // Q2의 옵션 (온라인 결제)
        ChatbotOption opt2_1 = new ChatbotOption(null, "결제 후 혜택 못 받았어요", 1, q2, null, a1);
        optionRepository.save(opt2_1);

        ChatbotOption opt2_2 = new ChatbotOption(null, "돈이 더 결제됐어요", 2, q2, null, a2);
        optionRepository.save(opt2_2);

        ChatbotOption opt2_3 = new ChatbotOption(null, "결제 내역 보기", 3, q2, null, a3);
        optionRepository.save(opt2_3);

        ChatbotOption opt2_4 = new ChatbotOption(null, "환불 문의", 4, q2, null, a4);
        optionRepository.save(opt2_4);

        ChatbotOption opt2_5 = new ChatbotOption(null, "후불 결제", 5, q2, null, a5);
        optionRepository.save(opt2_5);

        // Q3의 옵션 (오프라인 결제) - 예시로 Q2의 옵션과 동일하게 연결
        ChatbotOption opt3_1 = new ChatbotOption(null, "결제 후 혜택 못 받았어요", 1, q3, null, a1);
        optionRepository.save(opt3_1);

        ChatbotOption opt3_2 = new ChatbotOption(null, "돈이 더 결제됐어요", 2, q3, null, a2);
        optionRepository.save(opt3_2);

        ChatbotOption opt3_3 = new ChatbotOption(null, "결제 내역 보기", 3, q3, null, a3);
        optionRepository.save(opt3_3);

        ChatbotOption opt3_4 = new ChatbotOption(null, "환불 문의", 4, q3, null, a4);
        optionRepository.save(opt3_4);
    }
}