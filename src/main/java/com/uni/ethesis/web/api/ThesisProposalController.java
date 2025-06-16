package com.uni.ethesis.web.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uni.ethesis.data.dto.ThesisProposalDto;
import com.uni.ethesis.service.ThesisProposalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/thesis-proposals")
@RequiredArgsConstructor
public class ThesisProposalController {

    private final ThesisProposalService thesisProposalService;

    @GetMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<List<ThesisProposalDto>> getAllThesisProposals() {
        List<ThesisProposalDto> proposals = thesisProposalService.getAllThesisProposals();
        return ResponseEntity.ok(proposals);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<ThesisProposalDto> getThesisProposal(@PathVariable UUID id) {
        ThesisProposalDto proposal = thesisProposalService.getThesisProposalById(id);
        return ResponseEntity.ok(proposal);
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ThesisProposalDto> createThesisProposal(
            @RequestBody ThesisProposalDto proposalDto,
            Authentication authentication) {
        
        // The JWT token is automatically validated by Spring Security
        // The user info is available in the Authentication object
        String userEmail = authentication.getName();
        
        ThesisProposalDto created = thesisProposalService.createThesisProposal(proposalDto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ThesisProposalDto> updateThesisProposal(
            @PathVariable UUID id,
            @RequestBody ThesisProposalDto proposalDto) {
        
        ThesisProposalDto updated = thesisProposalService.updateThesisProposal(id, proposalDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteThesisProposal(@PathVariable UUID id) {
        thesisProposalService.deleteThesisProposal(id);
        return ResponseEntity.noContent().build();
    }
}
