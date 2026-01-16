package com.oscc.skillexchange.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private Long totalConnections;
    private Long skillsExchanged;
    private Long badgesEarned;
    private Long activeSessions;
    private List<RecentConnection> recentConnections;
    private String weekChange; // e.g., "+3 from last week"

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentConnection {
        private String userId;
        private String name;
        private String skill;
        private String timeAgo;
    }
}
