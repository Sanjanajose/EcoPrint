package com.ecoprint.printmanagement.service;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.dto.MaintenanceRequestDTO;
import com.ecoprint.printmanagement.dto.UserDTO;
import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.model.MaintenanceRequest;
import com.ecoprint.printmanagement.model.MaintenanceStatus;
import com.ecoprint.printmanagement.model.Printer;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.repository.MaintenanceRequestRepository;
import com.ecoprint.printmanagement.repository.PrinterRepository;
import com.ecoprint.printmanagement.repository.UserRepository;

@Service
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final PrinterRepository printerRepository;
    private final UserRepository userRepository;
    
    @Autowired
    private  UserService userService;
  
    

    public MaintenanceRequestService(MaintenanceRequestRepository maintenanceRequestRepository,
                                     PrinterRepository printerRepository,
                                     UserRepository userRepository) {
        this.maintenanceRequestRepository = maintenanceRequestRepository;
        this.printerRepository = printerRepository;
        this.userRepository = userRepository;
    }
    
    
  
    
    public MaintenanceRequestDTO toDTO(MaintenanceRequest request) {
        // Get printer name or null if printer is not set
        String printerName = (request.getPrinter() != null) ? request.getPrinter().getName() : null;
        
        // Convert User to UserDTO
        UserDTO submittedByDTO = (request.getSubmittedBy() != null)
                ? new UserDTO(request.getSubmittedBy().getId(), request.getSubmittedBy().getUsername())
                : null;

        return new MaintenanceRequestDTO(
                request.getId(),
                request.getDescription(),
                printerName,
                submittedByDTO,
                request.getStatus()
        );
    }


    

    public MaintenanceRequest createMaintenanceRequest(Long printerId, String description, User submittedBy) {
        Printer printer = printerRepository.findById(printerId)
                .orElseThrow(() -> new ResourceNotFoundException("Printer", "id", printerId));
        
        MaintenanceRequest request = new MaintenanceRequest();
        request.setPrinter(printer);
        request.setDescription(description);
        request.setSubmittedBy(submittedBy);
        request.setStatus(MaintenanceStatus.PENDING); // Default status
        return maintenanceRequestRepository.save(request);
    }
    
    public Page<MaintenanceRequestDTO> getRequests(MaintenanceStatus status, Pageable pageable) {
        Page<MaintenanceRequest> requests = maintenanceRequestRepository.findByStatus(status, pageable);

        // Force initialization of Printer entity
        requests.forEach(request -> Hibernate.initialize(request.getPrinter()));

        return requests.map(this::toDTO);
    }
    
    
    public MaintenanceRequestDTO updateStatus(Long requestId, String status) {
        MaintenanceRequest request = maintenanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRequest", "id", requestId));

        // Convert the status String to MaintenanceStatus enum
        MaintenanceStatus maintenanceStatus = MaintenanceStatus.valueOf(status.toUpperCase());
        request.setStatus(maintenanceStatus);

        MaintenanceRequest updatedRequest = maintenanceRequestRepository.save(request);

        // Convert User to UserDTO
        UserDTO submittedByDTO = new UserDTO(
                updatedRequest.getSubmittedBy().getId(),
                updatedRequest.getSubmittedBy().getUsername()
        );

        // Convert MaintenanceRequest to DTO
        return new MaintenanceRequestDTO(
                updatedRequest.getId(),
                updatedRequest.getDescription(),
                updatedRequest.getPrinter().getName(),
                submittedByDTO,
                updatedRequest.getStatus()
        );
    }

}
