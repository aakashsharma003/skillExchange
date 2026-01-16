package com.oscc.skillexchange.service;

import com.oscc.skillexchange.domain.entity.ChatMessage;
import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.repository.ChatMessageRepository;
import com.oscc.skillexchange.repository.UserRepository;
import com.oscc.skillexchange.util.TranscriptRedactionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Scheduled;
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

    /**
     * Scheduled task to audit chat logs every hour
     * Processes transcripts and updates learning progress
     * Note: This is a background worker that processes completed chat sessions
     */
    @Scheduled(fixedRate = 3600000) // Run every hour (3600000 ms)
    @Transactional
    public void auditChatLogs() {
        log.info("Starting scheduled audit of chat logs");
        try {
            // Note: In a production system, you'd query for chat rooms with recent activity
            // or process sessions that have been marked as complete
            // For now, this method can be called manually or triggered by events
            log.info("Scheduled audit completed (manual processing recommended via processChatTranscript)");
        } catch (Exception e) {
            log.error("Error in scheduled audit: {}", e.getMessage(), e);
        }
    }

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

            // AI analysis
            LearningAnalysis analysis = analyzeTranscript(redactedTranscript);

            if (analysis != null && analysis.educational() && analysis.proficiencyIncrease() > 0) {
                // If learnerId is null, skip progress update (scheduled audit without specific learner)
                if (learnerId != null) {
                    updateLearningProgress(learnerId, analysis);
                } else {
                    log.debug("LearnerId is null for chat room {}, skipping progress update", chatRoomId);
                }
            }

        } catch (Exception e) {
            log.error("Error processing transcript for room: {}", chatRoomId, e);
        }
    }

    private String buildTranscript(List<ChatMessage> messages) {
        Collections.reverse(messages);
        return messages.stream()
                .map(msg -> String.format("[%s]: %s", msg.getSenderEmail(), msg.getContent()))
                .collect(Collectors.joining("\n"));
    }

    private LearningAnalysis analyzeTranscript(String transcript) {
        // Spring AI automatically JSON ko record mein convert kar dega
        return chatClient.prompt()
                .user(u -> u.text("""
                        Analyze this chat transcript. 
                        1. Is it educational? 
                        2. What technical topics were discussed? 
                        3. How much did proficiency increase (0.0 to 5.0)?
                        
                        Transcript:
                        %s
                        """.formatted(transcript)))
                .call()
                .entity(LearningAnalysis.class);
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
            progress.put(topic, Math.min(100.0, current + analysis.proficiencyIncrease()));
        }

        learner.setLearningProgress(progress);
        userRepository.save(learner);
        log.info("Updated progress for learner {}: {}", learnerId, analysis.topics());
    }

    // AI Response Structure
    public record LearningAnalysis(
            boolean educational,
            List<String> topics,
            double proficiencyIncrease
    ) {}
}