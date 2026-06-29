package com.farm2future.farm2future_backend.model.fram.Controller;

import com.farm2future.farm2future_backend.model.fram.dto.FarmDataSubmitRequest;
import com.farm2future.farm2future_backend.model.fram.dto.FarmDataSubmitResponse;
import com.farm2future.farm2future_backend.model.fram.service.FarmDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/farms")
@RequiredArgsConstructor
public class FarmDataController {
    private final FarmDataService farmDataService;

    @PostMapping("/{farmId}/data")
    public FarmDataSubmitResponse submitFarmData(
            @PathVariable String farmId,
            @Valid @RequestBody FarmDataSubmitRequest request
    ) {
        return farmDataService.submitFarmData(farmId, request);
    }
}
