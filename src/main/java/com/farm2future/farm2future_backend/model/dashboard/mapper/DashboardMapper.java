package com.farm2future.farm2future_backend.model.dashboard.mapper;

import com.farm2future.farm2future_backend.model.dashboard.dto.DashboardAlertRow;
import com.farm2future.farm2future_backend.model.dashboard.dto.DashboardChartPointRow;
import com.farm2future.farm2future_backend.model.dashboard.dto.DashboardStatsRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DashboardMapper {
    @Select("""
            SELECT
                COALESCE(ROUND(AVG(s.total_score), 0), 0) AS overall,
                COALESCE(ROUND(AVG(s.environmental_score), 0), 0) AS environmental,
                COALESCE(ROUND(AVG(s.social_score), 0), 0) AS social,
                COALESCE(ROUND(AVG(s.governance_score), 0), 0) AS governance
            FROM esg_score s
            LEFT JOIN farm f ON s.farm_id = f.id
            WHERE s.deleted = 0
              AND f.deleted = 0
              AND s.period = #{period}
              AND (#{farm} = 'All Farms' OR f.farm_name = #{farm})
            """)
    DashboardStatsRow selectStatsByMonth(
            @Param("farm") String farm,
            @Param("period") String period
    );

    @Select("""
            SELECT
                s.period AS period,
                COALESCE(ROUND(AVG(s.total_score), 0), 0) AS value
            FROM esg_score s
            LEFT JOIN farm f ON s.farm_id = f.id
            WHERE s.deleted = 0
              AND f.deleted = 0
              AND s.period >= #{startPeriod}
              AND s.period <= #{endPeriod}
              AND (#{farm} = 'All Farms' OR f.farm_name = #{farm})
            GROUP BY s.period
            ORDER BY s.period ASC
            """)
    List<DashboardChartPointRow> selectChartByMonthRange(
            @Param("farm") String farm,
            @Param("startPeriod") String startPeriod,
            @Param("endPeriod") String endPeriod
    );

    @Select("""
            SELECT
                id,
                title,
                entity,
                severity,
                alert_time AS alertTime
            FROM dashboard_alert
            WHERE deleted = 0
              AND (#{farm} = 'All Farms' OR entity = #{farm})
            ORDER BY alert_time DESC
            LIMIT 5
            """)
    List<DashboardAlertRow> selectRecentAlerts(@Param("farm") String farm);

    @Select("""
            SELECT farm_name
            FROM farm
            WHERE deleted = 0
            ORDER BY farm_name ASC
            """)
    List<String> selectFarmNames();
}
