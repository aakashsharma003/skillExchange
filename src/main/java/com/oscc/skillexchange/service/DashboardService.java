package com.oscc.skillexchange.service;

import com.oscc.skillexchange.domain.entity.*;
import com.oscc.skillexchange.dto.response.DashboardStatsResponse;
import com.oscc.skillexchange.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final SessionFeedbackRepository sessionFeedbackRepository;
    private final ScheduledSessionRepository scheduledSessionRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    public DashboardStatsResponse getDashboardStats(String userId) {
        log.info("Fetching dashboard stats for user: {}", userId);

        // Total Connections = Accepted exchange requests
        Long totalConnections = (long) exchangeRequestRepository
                .findByUserIdAndStatusIn(userId, List.of(ExchangeRequest.RequestStatus.ACCEPTED))
                .size();

        // Skills Exchanged = Count of feedbacks where skillExchanged = true
        Long skillsExchanged = sessionFeedbackRepository
                .findBySessionUserIdAndSkillExchangedTrue(userId)
                .stream()
                .distinct() // Avoid counting same skill multiple times
                .count();

        // Badges Earned = Calculate based on milestones
        // For now: 1 badge per 5 skills exchanged, 1 badge per 10 connections
        Long badgesEarned = calculateBadges(skillsExchanged, totalConnections);

        // Active Sessions = Scheduled sessions for today with status SCHEDULED or IN_PROGRESS
        List<ScheduledSession> allUserSessions = scheduledSessionRepository.findByUserId(userId);
        LocalDate today = LocalDate.now();
        
        long activeSessions = allUserSessions.stream()
                .filter(session -> {
                    if (session.getScheduledDateTime() == null) return false;
                    LocalDate sessionDate = session.getScheduledDateTime().toLocalDate();
                    boolean isToday = sessionDate.equals(today);
                    boolean isActiveStatus = session.getStatus() == ScheduledSession.SessionStatus.SCHEDULED
                            || session.getStatus() == ScheduledSession.SessionStatus.IN_PROGRESS;
                    return isToday && isActiveStatus;
                })
                .count();

        // Recent Connections (last 5 accepted requests)
        List<ExchangeRequest> recentAccepted = exchangeRequestRepository
                .findByUserIdAndStatusIn(userId, List.of(ExchangeRequest.RequestStatus.ACCEPTED))
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .collect(Collectors.toList());

        List<DashboardStatsResponse.RecentConnection> recentConnections = recentAccepted.stream()
                .map(req -> {
                    String otherUserId = req.getSenderId().equals(userId) ? req.getReceiverId() : req.getSenderId();
                    User otherUser = userRepository.findById(otherUserId).orElse(null);
                    String timeAgo = formatTimeAgo(req.getCreatedAt());
                    return DashboardStatsResponse.RecentConnection.builder()
                            .userId(otherUserId)
                            .name(otherUser != null ? otherUser.getFullName() : "Unknown")
                            .skill(req.getRequestedSkill())
                            .timeAgo(timeAgo)
                            .build();
                })
                .collect(Collectors.toList());

        // Week change calculation
        Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        Long connectionsWeekAgo = (long) exchangeRequestRepository
                .findByUserIdAndStatusIn(userId, List.of(ExchangeRequest.RequestStatus.ACCEPTED))
                .stream()
                .filter(req -> req.getCreatedAt().isBefore(weekAgo))
                .count();

        long weekChange = totalConnections - connectionsWeekAgo;
        String weekChangeStr = weekChange >= 0
                ? String.format("+%d from last week", weekChange)
                : String.format("%d from last week", weekChange);

        return DashboardStatsResponse.builder()
                .totalConnections(totalConnections)
                .skillsExchanged(skillsExchanged)
                .badgesEarned(badgesEarned)
                .activeSessions(activeSessions)
                .recentConnections(recentConnections)
                .weekChange(weekChangeStr)
                .build();
    }

    private Long calculateBadges(Long skillsExchanged, Long totalConnections) {
        long badgesFromSkills = skillsExchanged / 5; // 1 badge per 5 skills
        long badgesFromConnections = totalConnections / 10; // 1 badge per 10 connections
        return Math.max(badgesFromSkills + badgesFromConnections, 0L);
    }

    private String formatTimeAgo(Instant instant) {
        long seconds = ChronoUnit.SECONDS.between(instant, Instant.now());
        if (seconds < 60) return seconds + "s ago";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        return days + "d ago";
    }
}
