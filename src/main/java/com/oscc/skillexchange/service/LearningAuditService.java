package com.oscc.skillexchange.service;

import com.oscc.skillexchange.domain.entity.ChatMessage;
import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.repository.ChatMessageRepository;
import com.oscc.skillexchange.repository.UserRepository;
import com.oscc.skillexchange.util.TranscriptRedactionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningAuditService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatClient chatClient;

    @Transactional
    public void processChatTranscript(String chatRoomId, String learnerId) {
        try {
            List<ChatMessage> messages = chatMessageRepository
                    .findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)
                    .stream()
                    .limit(50)
                    .collect(Collectors.toList());

            if (messages.isEmpty()) return;

            String transcript = buildTranscript(messages);
            String redactedTranscript = TranscriptRedactionUtil.redact(transcript);

            LearningAnalysis analysis = analyzeTranscript(redactedTranscript);

            // ✅ FIXED: record accessors
            if (analysis.educational() && analysis.proficiencyIncrease() > 0) {
                updateLearningProgress(learnerId, analysis);
            }

        } catch (Exception e) {
            log.error("Error processing transcript", e);
        }
    }

    private String buildTranscript(List<ChatMessage> messages) {
        Collections.reverse(messages);
        return messages.stream()
                .map(msg -> String.format("[%s]: %s", msg.getSenderEmail(), msg.getContent()))
                .collect(Collectors.joining("\n"));
    }

    private LearningAnalysis analyzeTranscript(String transcript) {
        String response = chatClient.prompt()
                .user(u -> u.text("""
                        Analyze the following chat transcript and respond ONLY in JSON.

                        Expected format:
                        {
                          "educational": true,
                          "topics": ["Spring Boot", "JWT"],
                          "proficiencyIncrease": 2.5
                        }

                        Transcript:
                        %s
                        """.formatted(transcript)))
                .call()
                .content();

        return parseAnalysis(response);
    }

    private LearningAnalysis parseAnalysis(String aiResponse) {
        // ⚠️ Simplified parser (replace with Jackson in production)
        boolean educational = aiResponse.contains("\"educational\": true");

        List<String> topics = new ArrayList<>();
        if (aiResponse.contains("Spring")) topics.add("Spring");
        if (aiResponse.contains("JWT")) topics.add("JWT");

        double proficiencyIncrease = 1.0;

        return new LearningAnalysis(educational, topics, proficiencyIncrease);
    }

    @Transactional
    public void updateLearningProgress(String learnerId, LearningAnalysis analysis) {
        User learner = userRepository.findById(learnerId)
                .orElseThrow(() -> new IllegalArgumentException("Learner not found"));

        Map<String, Double> progress = learner.getLearningProgress() == null
                ? new HashMap<>()
                : new HashMap<>(learner.getLearningProgress());

        for (String topic : analysis.topics()) {
            double current = progress.getOrDefault(topic, 0.0);
            progress.put(
                    topic,
                    Math.min(100.0, current + analysis.proficiencyIncrease())
            );
        }

        learner.setLearningProgress(progress);
        userRepository.save(learner);
    }
    public record LearningAnalysis(
            boolean educational,
            List<String> topics,
            double proficiencyIncrease
    ) {}
}
