package com.disruptioncompanion.infrastructure.adapter.in.rest;

import com.disruptioncompanion.application.port.in.AssessDisruption;
import com.disruptioncompanion.infrastructure.adapter.in.rest.dto.*;
import com.disruptioncompanion.infrastructure.adapter.in.rest.mapper.AssessmentMapper;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {
  private final AssessDisruption assess;

  public AssessmentController(AssessDisruption assess) {
    this.assess = assess;
  }

  @PostMapping
  public ResponseEntity<AssessmentResponse> assess(@Valid @RequestBody AssessmentRequest request) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(
            AssessmentMapper.toResponse(
                assess.assess(
                    request.bookingId(), Boolean.TRUE.equals(request.compensationRequested()))));
  }
}
