package com.farm2future.farm2future_backend.model.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.farm2future.farm2future_backend.model.dashboard.dto.DashboardOverviewDTO;
import com.farm2future.farm2future_backend.model.fram.entity.EsgScore;
import com.farm2future.farm2future_backend.model.fram.entity.Farm;
import com.farm2future.farm2future_backend.model.fram.entity.TokenRecord;
import com.farm2future.farm2future_backend.model.fram.entity.TransactionRecord;
import com.farm2future.farm2future_backend.model.fram.mapper.EsgScoreMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.FarmMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.TokenRecordMapper;
import com.farm2future.farm2future_backend.model.fram.mapper.TransactionRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashBoardService {
    private final FarmMapper farmMapper;
    private final TokenRecordMapper tokenRecordMapper;
    private final TransactionRecordMapper transactionRecordMapper;
    private final EsgScoreMapper esgScoreMapper;
    public DashboardOverviewDTO getOverview(){
        DashboardOverviewDTO dto = new DashboardOverviewDTO();
        Long totalFarms = farmMapper.selectCount(
                new QueryWrapper<Farm>().eq("deleted", 0)
        );

        Map<String, Object> tokenResult = tokenRecordMapper.selectMaps(
                new QueryWrapper<TokenRecord>()
                        .select("COALESCE(SUM(token_amount), 0) AS totalTokens")
                        .eq("deleted", 0)
        ).get(0);

        Map<String, Object> avgEsgResult = esgScoreMapper.selectMaps(
                new QueryWrapper<EsgScore>()
                        .select("COALESCE(AVG(total_score), 0) AS averageEsgScore")
                        .eq("deleted", 0)
        ).get(0);

        Long totalTransactions = transactionRecordMapper.selectCount(
                new QueryWrapper<TransactionRecord>()
                        .eq("deleted", 0)
        );

        BigDecimal totalTokens = new BigDecimal(tokenResult.get("totalTokens").toString());
        BigDecimal averageEsgScore = new BigDecimal(avgEsgResult.get("averageEsgScore").toString());

        dto.setTotalFarms(totalFarms);
        dto.setTotalTokens(totalTokens);
        dto.setTotalTransactions(totalTransactions);
        dto.setAverageEsgScore(averageEsgScore);

        return dto;
    }
}
