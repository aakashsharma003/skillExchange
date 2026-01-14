package com.oscc.skillexchange.service;

import com.oscc.skillexchange.domain.entity.ChatMessage;
import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.repository.ChatMessageRepository;
import com.oscc.skillexchange.repository.UserRepository;
import com.oscc.skillexchange.util.TranscriptRedactionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Learning-Audit-Service: Processes chat transcripts using Spring AI (OpenAI)
 * Analyzes if transcripts are 'Educational' or 'Social'
 * Extracts topics mastered and assigns proficiency increases
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningAuditService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatClient chatClient;

    /**
     * Process chat logs for a specific chat room
     * Sends last 50 messages to OpenAI for analysis
     */
    @Transactional
    public void processChatTranscript(String chatRoomId, String learnerId) {
        log.info("Processing chat transcript for room: {}, learner: {}", chatRoomId, learnerId);

        try {
            // Get last 50 messages
            List<ChatMessage> messages = chatMessageRepository
                    .findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)
                    .stream()
                    .limit(50)
                    .collect(Collectors.toList());

            if (messages.isEmpty()) {
                log.warn("No messages found for chat room: {}", chatRoomId);
                return;
            }

            // Build transcript
            String transcript = buildTranscript(messages);

            // Redact sensitive information
            String redactedTranscript = TranscriptRedactionUtil.redact(transcript);

            // Analyze with AI
            LearningAnalysis analysis = analyzeTranscript(redactedTranscript);

            if (analysis.isEducational() && analysis.getProficiencyIncrease() > 0) {
                // Update learner's progress
                updateLearningProgress(learnerId, analysis);
                log.info("Updated learning progress for learner: {}", learnerId);
            }

        } catch (Exception e) {
            log.error("Error processing chat transcript for room: {}", chatRoomId, e);
        }
    }

    /**
     * Build transcript from messages
     */
    private String buildTranscript(List<ChatMessage> messages) {
        Collections.reverse(messages); // Reverse to get chronological order
        return messages.stream()
                .map(msg -> String.format("[%s]: %s", msg.getSenderEmail(), msg.getContent()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Analyze transcript using OpenAI
     */
    private LearningAnalysis analyzeTranscript(String transcript) {
        String prompt = String.format("""
                Analyze this chat transcript and determine:
                1. Is this conversation 'Educational' or 'Social'? (Respond with Educational or Social)
                2. What topics/skills were mastered? (List them)
                3. What proficiency increase should be assigned? (0.1 to 5.0)
                
                Transcript:
                %s
                
                Respond in JSON format:
                {
                  "type": "Educational" or "Social",
                  "topics": ["topic1", "topic2"],
                  "proficiencyIncrease": 2.5
                }
                """, transcript);

        PromptResponse response = chatClient.call(new Prompt(prompt));
        String content = response.getResult().getOutput().getContent();

        return parseAnalysis(content);
    }

    /**
     * Parse AI response
     */
    private LearningAnalysis parseAnalysis(String aiResponse) {
        try {
            // Simple JSON parsing (in production, use Jackson)
            boolean isEducational = aiResponse.contains("\"type\":\"Educational\"") 
                    || aiResponse.contains("Educational");
            
            // Extract topics (simplified - use proper JSON parser in production)
            List<String> topics = new ArrayList<>();
            if (aiResponse.contains("\"topics\"")) {
                // Extract topics array (simplified parsing)
                String topicsSection = aiResponse.substring(aiResponse.indexOf("\"topics\""));
                // Add parsing logic here
            }

            // Extract proficiency increase
            double proficiencyIncrease = 1.0; // Default
            if (aiResponse.contains("\"proficiencyIncrease\"")) {
                String proficiencySection = aiResponse.substring(
                        aiResponse.indexOf("\"proficiencyIncrease\""));
                // Extract number (simplified)
                try {
                    String numStr = proficiencySection
                            .replaceAll("[^0-9.]", "")
                            .substring(0, Math.min(4, proficiencySection.length()));
                    proficiencyIncrease = Double.parseDouble(numStr);
                    proficiencyIncrease = Math.min(5.0, Math.max(0.1, proficiencyIncrease));
                } catch (Exception e) {
                    log.warn("Could not parse proficiency increase", e);
                }
            }

            return new LearningAnalysis(isEducational, topics, proficiencyIncrease);
        } catch (Exception e) {
            log.error("Error parsing AI response", e);
            return new LearningAnalysis(false, new ArrayList<>(), 0.0);
        }
    }

    /**
     * Update learner's learning progress in MongoDB
     */
    @Transactional
    public void updateLearningProgress(String learnerId, LearningAnalysis analysis) {
        User learner = userRepository.findById(learnerId)
                .orElseThrow(() -> new RuntimeException("Learner not found: " + learnerId));

        Map<String, Double> progress = learner.getLearningProgress() != null
                ? new HashMap<>(learner.getLearningProgress())
                : new HashMap<>();

        for (String topic : analysis.getTopics()) {
            double currentProgress = progress.getOrDefault(topic, 0.0);
            double newProgress = Math.min(100.0, currentProgress + analysis.getProficiencyIncrease());
            progress.put(topic, newProgress);
        }

        learner.setLearningProgress(progress);
        userRepository.save(learner);
    }

    /**
     * Scheduled task to process chat logs (runs every hour)
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void processPendingChatLogs() {
        log.info("Running scheduled chat log processing");
        // Implementation: Find chat rooms with new messages and process them
        // This is a placeholder - implement based on your requirements
    }

    /**
     * Learning analysis result
     */
    public static class LearningAnalysis {
        private final boolean educational;
        private final List<String> topics;
        private final double proficiencyIncrease;

        public LearningAnalysis(boolean educational, List<String> topics, double proficiencyIncrease) {
            this.educational = educational;
            this.topics = topics;
            this.proficiencyIncrease = proficiencyIncrease;
        }

        public boolean isEducational() {
            return educational;
        }

        public List<String> getTopics() {
            return topics;
        }

        public double getProficiencyIncrease() {
            return proficiencyIncrease;
        }
    }
}
