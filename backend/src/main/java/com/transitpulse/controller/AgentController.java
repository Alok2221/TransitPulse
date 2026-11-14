package com.transitpulse.controller;

import com.transitpulse.dto.AgentAdviseRequest;
import com.transitpulse.dto.AgentAdviseResponse;
import com.transitpulse.service.AiAgentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AiAgentService aiAgentService;

    public AgentController(AiAgentService aiAgentService) {
        this.aiAgentService = aiAgentService;
    }

    @PostMapping("/advise")
    public AgentAdviseResponse advise(@Valid @RequestBody AgentAdviseRequest request) {
        return aiAgentService.advise(request);
    }
}
